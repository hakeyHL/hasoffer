package hasoffer.timer.msp;

import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.system.ITimerService;
import hasoffer.timer.msp.vo.MspUnCmpModel;
import hasoffer.timer.msp.worker.FetchProductTagNodesWorker;
import hasoffer.timer.msp.worker.ParseTagNodeToProductWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/2/19.
 */
//@Component
public class MspUnCmpFetchTask {

    //先查询一个分类的信息
    private final static String Q_CATEGORY_UNCMPS = "SELECT t FROM MspCategory t where t.parentId > 0 and t.compared = 0";
    private final static String Q_THDMSPPRODUCT_SOURCEID = "SElECT t.sourceId FROM ThdMspProduct t where t.sourceId is not null";

    private static Logger logger = LoggerFactory.getLogger(MspUnCmpFetchTask.class);

    @Resource
    IMspService mspService;
    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ITimerService timerService;

    //		@Scheduled(cron = "0 0/1 * * * ?")
    public void fetchMspUnCmp() {
        SysTimerTaskLog log = timerService.createTaskLog("MspUnCmpFetchTask");

        logger.debug("------------------FETCH MspUnCmpProduct LIST------------------------");
        final ConcurrentLinkedQueue<MspCategory> categoryListQueue = new ConcurrentLinkedQueue<MspCategory>();
        final ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue = new ConcurrentLinkedQueue<MspUnCmpModel>();
        // 拿到要抓取的列表页url集合
        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMPS);
        if (categories != null && categories.size() > 0) {// 集合中数据不为空
            //将遍历的结果放入队列
            for (MspCategory category : categories) {
                categoryListQueue.add(category);
            }
        }
        List<String> sourceIdList = dbm.query(Q_THDMSPPRODUCT_SOURCEID);

        //开启多个线程去抓取，需要手动控制线程个数
        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new FetchProductTagNodesWorker(categoryListQueue, mspUnCmpModelQueue, sourceIdList));
        es.execute(new ParseTagNodeToProductWorker(mspUnCmpModelQueue, mspService));


        while (true) {
            logger.debug("------------------FETCH MspUnCmpProduct LIST------------------------");
            try {
                TimeUnit.SECONDS.sleep(10);
                if (FetchProductTagNodesWorker.aliveThreadCount == 0 && ParseTagNodeToProductWorker.aliveThreadCount == 0) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }

        es.shutdown();

        timerService.updateTaskLog(log.getId(), "");
    }


}
