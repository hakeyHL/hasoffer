package hasoffer.job.worker;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.updater.PtmProductUpdater;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IFetchService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/6/3.
 */
public class CmpSkuVisitUpdateWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(CmpSkuVisitUpdateWorker.class);

    private static final String Q_SKU_PRODUCTID = "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ";

    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> logQueue;
    private IFetchService fetchService;
    private ICmpSkuService cmpSkuService;

    public CmpSkuVisitUpdateWorker(IDataBaseManager dbm, IFetchService fetchService, ICmpSkuService cmpSkuService, ConcurrentLinkedQueue<SrmSearchLog> logQueue) {
        this.dbm = dbm;
        this.fetchService = fetchService;
        this.logQueue = logQueue;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        while (true) {

            SrmSearchLog log = logQueue.poll();

            if (log == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                    logger.debug("CmpSkuVisitUpdateWorker get null sleep 1 min");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            long productId = log.getPtmProductId();

            if (productId == 0) {
                continue;
            }

            PtmProduct ptmProduct = dbm.get(PtmProduct.class, productId);

            if (ptmProduct == null) {
                continue;
            }

            if (ptmProduct.getUpdateTime() != null) {

                long lastUpdateTime = ptmProduct.getUpdateTime().getTime();

                if (lastUpdateTime > TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_DAY) {
                    continue;
                }

            } else {

                PtmProductUpdater updater = new PtmProductUpdater(productId);

                updater.getPo().setUpdateTime(TimeUtils.nowDate());

                dbm.update(updater);

            }

            List<PtmCmpSku> skus = dbm.query(Q_SKU_PRODUCTID, Arrays.asList(productId));

            for (PtmCmpSku sku : skus) {

                Website website = sku.getWebsite();

                //如果不是需要更新的网站，跳过
                if (!WebsiteHelper.DEFAULT_WEBSITES.contains(website)) {
                    continue;
                }

                FetchedProduct fetchedProduct = null;
                try {

                    if (TimeUtils.now() - sku.getUpdateTime().getTime() < TimeUtils.MILLISECONDS_OF_1_DAY) {
                        continue;
                    }

                    fetchedProduct = fetchService.udpateSkuInAnyWay(sku.getUrl(), sku.getWebsite());

                    cmpSkuService.updateCmpSkuBySummaryProduct(sku.getId(), fetchedProduct);

                } catch (Exception e) {
                    logger.debug("update sku fail for [" + sku.getId() + "]");
                    logger.debug(e.toString());
                }
            }
        }
    }
}
