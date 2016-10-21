package hasoffer.job.bean.image;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
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
    private Logger logger = LoggerFactory.getLogger(SkuImageDownloadJobBean.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        final LinkedBlockingQueue<PtmCmpSku> cmpSkuQueue = new LinkedBlockingQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new Runnable() {
            @Override
            public void run() {
                loadImageDownLoadTasks(cmpSkuQueue);
            }
        });

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

        logger.info("All jobs finished.");
    }

    @DataSource(value = DataSourceType.Slave)
    private void loadImageDownLoadTasks(LinkedBlockingQueue<PtmCmpSku> cmpSkusQueue) {
        final int PAGE_SIZE = 1000;

        PageableResult<PtmCmpSku> pSkus = dbm.queryPage(Q_SKU_IMAGE, 1, PAGE_SIZE);

        long totalPage = pSkus.getTotalPage();
        long page = totalPage;

        while (page >= 1) {
            List<PtmCmpSku> skus = null;
            if (page == 1) {
                skus = pSkus.getData();
            } else {
                skus = dbm.query(Q_SKU_IMAGE, (int) page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(skus)) {
                for (PtmCmpSku sku : skus) {
                    if (StringUtils.isEmpty(sku.getOriImageUrl())) {
                        continue;
                    }
                    if (sku.isFailLoadImage()) {
                        continue;
                    }

                    cmpSkusQueue.add(sku);
                }
            }

            page--;
        }
    }
}
