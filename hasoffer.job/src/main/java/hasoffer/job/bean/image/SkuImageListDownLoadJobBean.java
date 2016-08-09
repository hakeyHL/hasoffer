package hasoffer.job.bean.image;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2016/8/9.
 */
public class SkuImageListDownLoadJobBean extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SkuImageListDownLoadJobBean.class);

    @Resource
    IDataBaseManager dbm;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        final int page = 1, PAGE_SIZE = 1000;

        List<PtmCmpSkuImage> skuImageList = dbm.query("SELECT t FROM PtmCmpSkuImage t WHERE t.checked = 0", page, PAGE_SIZE);

        final LinkedBlockingQueue<PtmCmpSkuImage> cmpSkuQueue = new LinkedBlockingQueue<PtmCmpSkuImage>();
//        cmpSkuQueue.addAll(skus);

        ExecutorService es = Executors.newCachedThreadPool();

        int processCount = 20;
        final AtomicInteger processorCount = new AtomicInteger(0);

        for (int i = 0; i < processCount; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    processorCount.addAndGet(1);

                    while (true) {
//                        PtmCmpSku t = cmpSkuQueue.poll();
//
//                        if (t == null) {
//                            break;
//                        }

//                        cmpSkuService.downloadImage2(t);
                    }

                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                break;
            }

            if (cmpSkuQueue.size() > 0) {
                logger.info("queue size = " + cmpSkuQueue.size());
                continue;
            }

            if (processorCount.get() > 0) {
                logger.info("processorCount = " + processorCount.get());
                continue;
            }

            break;
        }

        logger.info("All jobs finished.");

    }
}
