package hasoffer.job.bean.image;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.job.manager.ProductManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SkuImageDownloadJobBean extends QuartzJobBean {

    /**
     * 取ptmimage 逻辑：未下载下来的图片，按照失败次数从小到大排
     */
    private static final String Q_SKU_IMAGE =
            "SELECT t FROM PtmCmpSku t WHERE t.imagePath IS NULL";
    //            "SELECT t FROM PtmCmpSku t WHERE t.imagePath IS NULL AND t.oriImageUrl IS NOT NULL AND t.failLoadImage = 0";
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ProductManager productManager;
    private Logger logger = LoggerFactory.getLogger(SkuImageDownloadJobBean.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        final LinkedBlockingQueue<PtmCmpSku> cmpSkuQueue = new LinkedBlockingQueue<PtmCmpSku>();

        final AtomicBoolean loadTaskFinished = new AtomicBoolean(false);

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new Runnable() {
            @Override
            public void run() {
                productManager.loadImageDownLoadTasks(cmpSkuQueue);
                loadTaskFinished.set(true);
            }
        });

        int processCount = 20;
        final AtomicInteger processorCount = new AtomicInteger(0);

        for (int i = 0; i < processCount; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    processorCount.addAndGet(1);

                    //标记线程的起始时间
                    long startTime = TimeUtils.now();

                    while (true) {

                        //该任务每隔俩个小时启动一次，设置100分钟线程自动结束
                        if (TimeUtils.now() - startTime > TimeUtils.MILLISECONDS_OF_1_MINUTE * 100) {
                            logger.info("sku image download job bean live above 100 minutes");
                            System.out.println("sku image download job bean live above 100 minutes");
                            break;
                        }

                        PtmCmpSku t = cmpSkuQueue.poll();

                        if (t == null) {
                            break;
                        }

                        cmpSkuService.downloadImage2(t);
                    }

                    processorCount.addAndGet(-1);
                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                break;
            }

//            为了保证线程正常结束，注册掉该部分
//            if (cmpSkuQueue.size() > 0) {
//                logger.info("queue size = " + cmpSkuQueue.size());
//                continue;
//            }

            if (processorCount.get() > 0) {
                logger.info("processorCount = " + processorCount.get());
                continue;
            }

            if (!loadTaskFinished.get()) {
                continue;
            }

            break;
        }

        logger.info("sku image download job bean all jobs finished.");
    }

}
