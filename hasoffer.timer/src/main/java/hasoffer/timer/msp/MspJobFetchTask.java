package hasoffer.timer.msp;

import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.system.ITimerService;
import hasoffer.fetch.model.ListJob;
import hasoffer.fetch.model.PageModel;
import hasoffer.fetch.sites.mysmartprice.MspListProcessor;
import hasoffer.timer.msp.worker.SaveJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by on 2015/12/21.
 */
//@Component
public class MspJobFetchTask {

	private final static String Q_CATEGORY =
			"SELECT t FROM MspCategory t";

	private final static String Q_CATEGORY_PROCOUNT =
			" SELECT new map(t.categoryId, COUNT(t.id)) " +
			"   FROM MspProductJob t " +
			"  GROUP BY t.categoryId ";

	private static Logger logger = LoggerFactory.getLogger(MspJobFetchTask.class);

	@Resource
	IDataBaseManager dbm;
	@Resource
	IMspService mspService;
	@Resource
	ITimerService timerService;

//	@Scheduled(cron = "0 0/1 * * * ?")
	@Scheduled(cron = "0 20 1 * * ?")
	public void fetchProductList() {
		SysTimerTaskLog log = timerService.createTaskLog("MspJobFetchTask");

		logger.debug("------------------FETCH PRODUCT LIST------------------------");
		final ConcurrentLinkedQueue<ListJob> listQueue = new ConcurrentLinkedQueue<ListJob>();

		final Map<Long, Long> cateJobCountMap = getJobCountMap();

		final List<MspCategory> categories = dbm.query(Q_CATEGORY);

		List<String> existingProIdList = dbm.query("select t.sourceId from MspProductJob t");
		final Set<String> existingProIds = new HashSet<String>(existingProIdList);

		final AtomicBoolean listTaskFinished = new AtomicBoolean(false);

		final Runnable list = new Runnable() {
			@Override
			public void run() {
				MspListProcessor listProcessor = new MspListProcessor();
				MspCategory cate = null;
				for (int i = 0, size = categories.size(); i < size; i++) {
					cate = categories.get(i);

					logger.debug("fetch product list. category : " + cate.getName());
					if (cate.getParentId() == 0) {
						continue;
					}

					Long jobCount = cateJobCountMap.get(cate.getId());
					if (jobCount != null) {
						if (cate.getProCount() >= jobCount) {
							continue;
						}
					}

					PageModel pageModel = listProcessor.getPageModel(cate.getUrl());
					int pageCount = pageModel.getPageCount();
					for (int p = 1; p < pageCount; p++) {
						String url = pageModel.getUrlTemplate().replace("{page}", String.valueOf(p));
						if (p == 1) {
							url = cate.getUrl();
						}
						ListJob listJob = new ListJob(null, url, String.valueOf(cate.getId()));
						listQueue.add(listJob);
					}

					if (listQueue.size() > 100) {
						try {
							TimeUnit.SECONDS.sleep(10);
							System.out.println("list queue has more than 100 jobs. go to sleep!");
						} catch (InterruptedException e) {
							break;
						}
					}
				}
				listTaskFinished.set(true);
			}
		};

		ExecutorService es = Executors.newCachedThreadPool();

		es.execute(list);
		for (int i = 0; i < 4; i++) {
			es.execute(new SaveJobWorker(existingProIds, listQueue, mspService));
		}

		while (true) {
			try {
				TimeUnit.SECONDS.sleep(8);

				int listQueueSize = listQueue.size();

				logger.debug("listQueue size : " + listQueueSize);

				if (listQueueSize == 0 && listTaskFinished.get()) {
					break;
				}
			} catch (InterruptedException e) {
				return;
			}
		}

		es.shutdown();

		timerService.updateTaskLog(log.getId(), "");
	}

	private Map<Long, Long> getJobCountMap() {
		Map<Long, Long> jobCountMap = new LinkedHashMap<Long, Long>();

		List<Map<String, Long>> jobCounts = dbm.query(Q_CATEGORY_PROCOUNT);

		for (Map<String, Long> map : jobCounts) {
			long cateId = map.get("0");
			long count = map.get("1");
			jobCountMap.put(cateId, count);
		}

		return jobCountMap;
	}

}
