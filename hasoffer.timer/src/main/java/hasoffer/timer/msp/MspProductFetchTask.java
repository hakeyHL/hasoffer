package hasoffer.timer.msp;

import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.msp.MspProductJob;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.product.IFetchService;
import hasoffer.core.system.ITimerService;
import hasoffer.timer.msp.worker.SaveProductWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//@Component
public class MspProductFetchTask {

    private final static String Q_CATEGORY =
            "SELECT t FROM MspCategory t ORDER BY t.id ASC";
    //			"SELECT t FROM MspCategory t ORDER BY t.parentId ASC, t.id DESC";
    /*private final static String Q_PRODUCT_JOB =
			"SELECT t FROM MspProductJob t ORDER BY t.id ASC ";*/
    private final static String Q_PRODUCT_JOB_BY_CATEGORY =
            "SELECT t FROM MspProductJob t WHERE t.categoryId = ?0 ORDER BY t.ptmProductId ASC";
    private final static String Q_COUNT_PRODUCT =
            "SELECT COUNT(t.id) FROM PtmProduct t WHERE t.categoryId = ?0 ";
    private final static String Q_COUNT_PRODUCTJOB =
            "SELECT COUNT(t.id) FROM MspProductJob t WHERE t.categoryId = ?0 ";
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMspService mspService;
    @Resource
    IFetchService fetchService;
    @Resource
    ITimerService timerService;
    private Logger logger = LoggerFactory.getLogger(MspProductFetchTask.class);

    //	@Scheduled(cron = "0 30 8 * * ?")
    @Scheduled(cron = "0 0/1 * * * ?")
    public void f() {
        SysTimerTaskLog log = timerService.createTaskLog("MspProductFetchTask");

        logger.debug("......................MSP FETCHING START.........................");
        final List<MspCategory> categories = dbm.query(Q_CATEGORY);
        for (MspCategory category : categories) {
            int proCount = category.getProCount();
            if (proCount == 0) {
                continue;
            }

            long proCount2 = dbm.querySingle(Q_COUNT_PRODUCT, Arrays.asList(category.getPtmCategoryId()));
            long proCount3 = dbm.querySingle(Q_COUNT_PRODUCTJOB, Arrays.asList(category.getId()));

            logger.debug(category.getName() + "\t" + proCount2 + "/" + proCount3 + "/" + proCount);

            fetchProducts(category.getId());
        }
//		fetchProducts(3);
        logger.debug("......................MSP FETCH END.........................");

        timerService.updateTaskLog(log.getId(), "");
    }

    public void fetchProducts(long category) {
        List<MspProductJob> productJobs = dbm.query(Q_PRODUCT_JOB_BY_CATEGORY, Arrays.asList(category));

        final ConcurrentLinkedQueue<MspProductJob> jobs = new ConcurrentLinkedQueue<MspProductJob>();
        final ConcurrentLinkedQueue<MspProductJob> jobs2 = new ConcurrentLinkedQueue<MspProductJob>();

        AtomicInteger saveCount = new AtomicInteger(0);

        jobs.addAll(productJobs);

        ExecutorService es = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++) {
            es.execute(new SaveProductWorker(jobs, jobs2, saveCount, mspService));
        }
        for (int i = 0; i < 2; i++) {
            es.execute(new SaveProductWorker(jobs2, null, saveCount, mspService));
        }

        int count = 0;
        while (true) {
            logger.debug(String.format("category %d. queue size : %d(%d).", category, jobs.size(), jobs2.size()));
            try {
                TimeUnit.SECONDS.sleep(10);
                if (jobs.size() == 0 && jobs2.size() == 0) {
                    count++;
                    if (count >= 5) {
                        break;
                    }
                } else {
                    count = 0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }

        es.shutdown();
    }
}
