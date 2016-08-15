package hasoffer.task.worker;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuDubboUpdateWorker implements Runnable {

    private static final String Q_PTMCMPSKU_BYPRODUCTID = "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ";
    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdateWorker.class);
    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> queue;
    private IFetchDubboService fetchService;
    private IProductService productService;
    private ICmpSkuService cmpSkuService;

    public CmpSkuDubboUpdateWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue, IFetchDubboService fetchService, IProductService productService, ICmpSkuService cmpSkuService) {
        this.dbm = dbm;
        this.queue = queue;
        this.fetchService = fetchService;
        this.productService = productService;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        while (true) {

            try {

                SrmSearchLog searchLog = queue.poll();

                if (searchLog == null) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        logger.info("task update get null sleep 3 seconds");
                    } catch (InterruptedException e) {
                        return;
                    }
                    continue;
                }

                long productId = searchLog.getPtmProductId();
                if (productId == 0) {
                    logger.info("task update get productId zero sleep 3 seconds");
                    continue;
                }

                List<PtmCmpSku> skuList = dbm.query(Q_PTMCMPSKU_BYPRODUCTID, Arrays.asList(productId));

                for (PtmCmpSku sku : skuList) {
                    //判断，如果该sku 当天更新过价格, 直接跳过
                    Date updateTime = sku.getUpdateTime();
                    if (updateTime != null) {
                        if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                            continue;
                        }
                    }

                    //更新商品的信息，写入多图数据，写入描述/参数
                    updatePtmCmpSku(sku, searchLog);
                }

                //更新商品的价格，同时修改updateTime字段
                if (skuList == null || skuList.size() == 0) {
                    continue;
                }

                productService.updatePtmProductPrice(productId);

            } catch (Exception e) {

            }
        }
    }

    private void updatePtmCmpSku(PtmCmpSku sku, SrmSearchLog searchLog) {
        // try update sku
        Long skuid = sku.getId();
        String url = sku.getUrl();
        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            return;
        }

        TaskStatus taskStatus = fetchService.getUrlTaskStatus(website, url);

        FetchUrlResult fetchUrlResult = null;

        //如果返回结果状态为running，那么将sku返回队列
        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
            queue.add(searchLog);
//            logger.info("taskstatus RUNNING for [" + skuid + "]");
            return;
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.info("taskstatus STOPPED for [" + skuid + "]");
            return;
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.info("taskstatus EXCEPTION for [" + skuid + "]");
            return;
        } else if (TaskStatus.NONE.equals(taskStatus)) {
            logger.info("taskstatus NONE for [" + skuid + "]");
            return;
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + skuid + "]");
            fetchUrlResult = fetchService.getProductsByUrl(skuid, sku.getWebsite(), sku.getUrl());

            FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

            System.out.println(JSONUtil.toJSON(fetchedProduct).toString());

            cmpSkuService.createDescription(sku, fetchedProduct);

            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuid, fetchedProduct);

            cmpSkuService.createPtmCmpSkuImage(skuid, fetchedProduct);
        }
    }
}
