package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuDubboUpdateWorker implements Runnable {

    private static final String Q_PTMCMPSKU_BYPRODUCTID = "SELECT t FROM PtmCmpSku t WHERE t.productid = ?0 ";
    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdateWorker.class);
    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> queue;
    private ICmpSkuService cmpSkuService;
    private IFetchDubboService fetchService;

    public CmpSkuDubboUpdateWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue, ICmpSkuService cmpSkuService, IFetchDubboService fetchService) {
        this.dbm = dbm;
        this.queue = queue;
        this.cmpSkuService = cmpSkuService;
        this.fetchService = fetchService;
    }

    @Override
    public void run() {
        while (true) {
            SrmSearchLog searchLog = queue.poll();

            if (searchLog == null) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.debug("task update get null sleep 3 seconds");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

//  for test暂时注释掉               判断，如果该sku 当天更新过价格, 直接跳过
//                Date updateTime = sku.getUpdateTime();
//                if (updateTime != null) {
//                    if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
//                        continue;
//                    }
//                }

            long productId = searchLog.getPtmProductId();
            List<PtmCmpSku> skuList = dbm.query(Q_PTMCMPSKU_BYPRODUCTID, Arrays.asList(productId));

            for (PtmCmpSku sku : skuList) {
                updatePtmCmpSku(sku, searchLog);
            }
        }
    }

    private void updatePtmCmpSku(PtmCmpSku sku, SrmSearchLog searchLog) {
        // try update sku
        String url = sku.getUrl();
        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            logger.debug(" parse website get null for [" + sku.getId() + "]");
            return;
        }

        FetchUrlResult fetchedResult = null;

        try {
            fetchedResult = fetchService.getProductsByUrl(website, url);
        } catch (HttpFetchException e) {
            logger.debug("HttpFetchException for [" + sku.getId() + "]");
        } catch (ContentParseException e) {
            logger.debug("ContentParseException for [" + sku.getId() + "]");
        }

        TaskStatus taskStatus = fetchedResult.getTaskStatus();

        FetchedProduct fetchedProduct = null;

        //如果返回结果状态为running，那么将sku返回队列
        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
            queue.add(searchLog);
            logger.debug("taskstatus RUNNING for [" + sku.getId() + "]");
            return;
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.debug("taskstatus STOPPED for [" + sku.getId() + "]");
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.debug("taskstatus EXCEPTION for [" + sku.getId() + "]");
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.debug("taskstatus FINISH for [" + sku.getId() + "]");
            fetchedProduct = fetchedResult.getFetchProduct();
        }

//        此处是FK、SD正常更新逻辑放弃对title字段的更新，该有另外的task统一维护
//        切换新的更新模式，采用页面更新的方式，所有可以不用考虑title
//        if (fetchedProduct != null) {
//            if (Website.FLIPKART.equals(fetchedProduct.getWebsite()) || Website.SNAPDEAL.equals(fetchedProduct.getWebsite())) {
//                fetchedProduct.setTitle(null);
//            }
//        }

        try {
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(sku.getId(), fetchedProduct);
            logger.debug("fetch success for [" + sku.getId() + "]");
        } catch (Exception e) {
            logger.debug(e.toString());
            if (fetchedProduct != null) {
                logger.debug("title:" + fetchedProduct.getTitle());
            }
        }
    }
}
