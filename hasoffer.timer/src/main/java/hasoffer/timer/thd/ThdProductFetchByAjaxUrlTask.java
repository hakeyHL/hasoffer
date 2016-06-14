package hasoffer.timer.thd;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.persistence.po.thd.ThdFetchTask;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.system.ITimerService;
import hasoffer.core.thd.IThdService;
import hasoffer.core.thd.ThdHelper;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.ListProduct;
import hasoffer.timer.thd.worker.SaveThdProductWorker;
import hasoffer.timer.thd.worker.ThdProductFetchByAjaxUrlWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/2/25.
 */
@Component
public class ThdProductFetchByAjaxUrlTask {

    private final static String THDFETCHTASKS = "SELECT t FROM ThdFetchTask";
    private static Logger logger = LoggerFactory.getLogger(ThdProductFetchByAjaxUrlTask.class);
    @Resource
    IDataBaseManager dbm;
    @Resource
    IThdService thdService;
    @Resource
    ITimerService timerService;
    @Resource
    ICategoryService categoryService;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void fetchFlipkartProductByAjaxUrl() {

        SysTimerTaskLog log = timerService.createTaskLog("ThdProductFetchByAjaxUrlTask");

        List<ThdFetchTask> tasks = dbm.query(THDFETCHTASKS);

        for (ThdFetchTask thdFetchTask : tasks) {

            if (thdFetchTask.getLastProcessTime() != null) {
                if (thdFetchTask.getLastProcessTime().getTime() > TimeUtils.today()) {
                    continue;
                }
            }

            thdService.updateTask(thdFetchTask.getId(), new Date(), TaskStatus.RUNNING);

            ConcurrentLinkedQueue<HashMap<String, Long>> ajaxUrlAndCateIdQueue = new ConcurrentLinkedQueue<HashMap<String, Long>>();
            ConcurrentLinkedQueue<ListProduct> productQueue = new ConcurrentLinkedQueue<ListProduct>();

            String urlTemplate = thdFetchTask.getUrlTemplate();
            Website webSite = WebsiteHelper.getWebSite(urlTemplate);
            int pageSize = ThdHelper.getPageSize(webSite);
            int start = thdFetchTask.getStart();
            int size = thdFetchTask.getSize();
            long ptmCateId = thdFetchTask.getPtmCateId();
            int pageNum = (size + pageSize - 1) / pageSize;
            int count = 0;

            for (int i = 0; i < pageNum; i++) {
                if (i == 0) {
                    start = 0;
                } else {
                    start = i * pageSize + 1;
                }
                String ajaxUrl = urlTemplate.replace("startNum", start + "");
                HashMap<String, Long> map = new HashMap<String, Long>();
                map.put(ajaxUrl, ptmCateId);
                ajaxUrlAndCateIdQueue.add(map);
                count++;
            }

            ThdProductFetchByAjaxUrlWorker.ajaxUrlAndCateIdQueue = ajaxUrlAndCateIdQueue;
            ThdProductFetchByAjaxUrlWorker.productQueue = productQueue;

            ExecutorService es = Executors.newCachedThreadPool();

            for (int i = 0; i < 10; i++) {
                es.execute(new ThdProductFetchByAjaxUrlWorker());
            }

            es.execute(new SaveThdProductWorker(thdService));

            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    if (ThdProductFetchByAjaxUrlWorker.aliveThreadCount == 0 && SaveThdProductWorker.aliveThreadCount == 0) {
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            es.shutdown();
            thdService.updateTask(thdFetchTask.getId(), new Date(), TaskStatus.STOPPED);

            String result = thdFetchTask.getWebsite().name() + categoryService.getCategory(thdFetchTask.getPtmCateId()).getName() + "fetch" + count * pageNum + "product";
            timerService.updateTaskLog(log.getId(), result);

        }
    }
}



