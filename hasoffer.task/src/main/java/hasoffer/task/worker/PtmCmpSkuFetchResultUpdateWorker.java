package hasoffer.task.worker;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.product.ISummaryProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2016/4/27.
 */
public class PtmCmpSkuFetchResultUpdateWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(PtmCmpSkuFetchResultUpdateWorker.class);

    private static String Q_UPDATESKU_BYURL = "SELECT t FROM PtmCmpSku t WHERE t.url = ?0 ";
    private ConcurrentLinkedQueue<SummaryProduct> skuQueue;
    private AtomicLong count;
    private ISummaryProductService summaryProductService;
    private IDataBaseManager dbm;

    public PtmCmpSkuFetchResultUpdateWorker(ConcurrentLinkedQueue<SummaryProduct> skuQueue, ISummaryProductService summaryProductService, AtomicLong count, IDataBaseManager dbm) {
        this.skuQueue = skuQueue;
        this.summaryProductService = summaryProductService;
        this.count = count;
        this.dbm = dbm;
    }

    @Override
    public void run() {

        while (true) {

            SummaryProduct summaryProduct = skuQueue.poll();

            if (summaryProduct == null) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.debug("seimi update worker get null sleep 3 seconds");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            //只更新shopclues和ebay
            if (!Website.SHOPCLUES.equals(summaryProduct.getWebsite()) && !Website.EBAY.equals(summaryProduct.getWebsite())) {
                continue;
            }

            summaryProductService.updateCmpSkuByFetchResult(summaryProduct,summaryProduct.getId());

//            List<PtmCmpSku> skus = dbm.query(Q_UPDATESKU_BYURL, Arrays.asList(summaryProduct.getUrl()));
//
//            for (PtmCmpSku sku : skus) {
//
//                summaryProductService.updateCmpSkuByFetchResult(summaryProduct, sku.getId());
//                count.addAndGet(1);
//                logger.debug("update sku success for [" + sku.getId() + "]");
//                if (count.get() % 500 == 0) {
//                    logger.debug("update sku in all " + count);
//                }
//            }
        }
    }
}
