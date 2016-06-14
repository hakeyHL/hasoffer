package hasoffer.task.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.product.ISummaryProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2016/4/28.
 */
public class PtmCmpSkuFetchResultListWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(PtmCmpSkuFetchResultListWorker.class);
    private ISummaryProductService summaryProductService;
    private AtomicLong count;
    private ConcurrentLinkedQueue<SummaryProduct> skuQueue;

    public PtmCmpSkuFetchResultListWorker(ISummaryProductService summaryProductService, ConcurrentLinkedQueue<SummaryProduct> skuQueue, AtomicLong count) {
        this.summaryProductService = summaryProductService;
        this.skuQueue = skuQueue;
        this.count = count;
    }

    @Override
    public void run() {

        long startLongTime = TimeUtils.today();

        while (true) {

            if (skuQueue.size() > 6000) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            PageableResult<SummaryProduct> pageableResult = null;

            try {
                pageableResult = summaryProductService.getPagedSummaryProductByTime(startLongTime, true, 0, 1, 2000);
            } catch (Exception e) {
                continue;
            }

            List<SummaryProduct> summaryProductList = pageableResult.getData();

            if (summaryProductList.size() == 0) {
                try {
                    TimeUnit.MINUTES.sleep(5);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            } else {

                logger.debug("before startLongTime:" + startLongTime);
                logger.debug("pageQuery get " + summaryProductList.size() + " reslt ");
                count.addAndGet(summaryProductList.size());
                logger.debug("pageQuery get sum " + count + " result");
                skuQueue.addAll(summaryProductList);
                //更新起始时间为最后一个result的时间
                startLongTime = summaryProductList.get(summaryProductList.size() - 1).getlUpdateTime();
                logger.debug("new startLongTime = " + TimeUtils.parse(startLongTime, "yyyy-MM-dd HH:mm:ss"));
                logger.debug("after startLongTime:" + startLongTime);
            }
        }
    }
}
