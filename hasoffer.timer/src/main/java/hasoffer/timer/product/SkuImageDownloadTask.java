package hasoffer.timer.product;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/1/13
 * Function :
 */
@Component
public class SkuImageDownloadTask {

    /**
     * 取ptmimage 逻辑：未下载下来的图片，按照失败次数从小到大排
     */
    private static final String Q_SKU_IMAGE =
            "SELECT t FROM PtmCmpSku t WHERE t.imagePath IS NULL AND t.oriImageUrl IS NOT NULL AND t.failLoadImage = 0";
//            "SELECT t FROM PtmCmpSku t ORDER BY t.id ASC ";

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;

    private Logger logger = LoggerFactory.getLogger(SkuImageDownloadTask.class);

    @Scheduled(cron = "0 0/10 * * * ?")
    public void f2() {
        final int page = 1, PAGE_SIZE = 1000;

        List<PtmCmpSku> skus = dbm.query(Q_SKU_IMAGE, page, PAGE_SIZE);

        final LinkedBlockingQueue<PtmCmpSku> cmpSkuQueue = new LinkedBlockingQueue<PtmCmpSku>();
        cmpSkuQueue.addAll(skus);

        ExecutorService es = Executors.newCachedThreadPool();

        int processCount = 20;
        final AtomicInteger processorCount = new AtomicInteger(0);

        for (int i = 0; i < processCount; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    processorCount.addAndGet(1);

                    while (true) {
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

        logger.info("All images downloaded.");
    }

}
