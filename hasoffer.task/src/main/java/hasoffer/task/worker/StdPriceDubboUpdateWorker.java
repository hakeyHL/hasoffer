package hasoffer.task.worker;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class StdPriceDubboUpdateWorker implements Runnable {

    private static final String STDPRICE_DROP_SKUID_QUEUE = "STDPRICE_DROP_SKUID_QUEUE";

    private static Logger logger = LoggerFactory.getLogger(StdPriceDubboUpdateWorker.class);
    private IFetchDubboService fetchDubboService;
    private IRedisListService redisListService;
    private IPtmStdPriceService ptmStdPriceService;

    public StdPriceDubboUpdateWorker(IFetchDubboService fetchDubboService, IRedisListService redisListService, IPtmStdPriceService ptmStdPriceService) {
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.ptmStdPriceService = ptmStdPriceService;
    }

    @Override
    public void run() {

        while (true) {

            try {

                String fetchUrlResultStr = fetchDubboService.popFetchUrlResult(TaskTarget.STDPRICE_UPDATE);
                if (fetchUrlResultStr == null) {
                    TimeUnit.MINUTES.sleep(3);
                    logger.info("fetchUrlResult get null sleep 10 MINUTES StdPriceDubboUpdateWorker");
                    continue;
                }
                FetchUrlResult fetchUrlResult = JSONUtil.toObject(fetchUrlResultStr, FetchUrlResult.class);
//                popNumber--;
                if (fetchUrlResult.getUrl() == null) {
                    logger.info("fetchUrlResult.getUrl() null StdPriceDubboUpdateWorker");
                    continue;
                }

                logger.info("pop get StdPriceDubboUpdateWorker response success " + fetchUrlResult.getWebsite());
                String url = fetchUrlResult.getUrl();
                Website website = fetchUrlResult.getWebsite();

                TaskStatus taskStatus = fetchUrlResult.getTaskStatus();

                if (TaskStatus.FINISH.equals(taskStatus)) {
                    logger.info("taskStatus is finish StdPriceDubboUpdateWorker " + website);
                    if (Website.FLIPKART.equals(website)) {
                        logger.info("pop get StdPriceDubboUpdateWorker flipkart finish result");
                    }

                    String urlKey = HexDigestUtil.md5(url);
                    List<PtmStdPrice> stdPriceList = ptmStdPriceService.getPtmstdPriceListByUrlKey(urlKey);

                    if (stdPriceList == null || stdPriceList.size() == 0) {
                        logger.info("urkKey StdPriceDubboUpdateWorker not found " + website + "url = " + url);
                    } else {
                        logger.info("urkKey found StdPriceDubboUpdateWorker " + website + " skulist begin to update " + stdPriceList.size());
                        for (PtmStdPrice ptmStdPrice : stdPriceList) {
                            updatePtmCmpSku(ptmStdPrice, fetchUrlResult);
                            logger.info("update success StdPriceDubboUpdateWorker for " + ptmStdPrice.getWebsite());
                        }
                    }
                } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
                    logger.info("taskStatus is StdPriceDubboUpdateWorker exception " + website);
                } else {
                    logger.info("taskStatus is StdPriceDubboUpdateWorker " + taskStatus + "_" + website);
                }
            } catch (Exception e) {
                logger.info("StdPriceDubboUpdateWorker.run() exception.", e);
            }
        }
    }

    private void updatePtmCmpSku(PtmStdPrice stdPrice, FetchUrlResult fetchUrlResult) {
        // try update sku
        Long stdPriceId = stdPrice.getId();
        String url = stdPrice.getUrl();
        float price = stdPrice.getPrice();

        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            logger.info("website StdPriceDubboUpdateWorker is null for _" + stdPriceId + "_");
            return;
        }

        FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

        try {
            //
            ptmStdPriceService.updatePtmStdPriceBySpiderFetchedProduct(stdPriceId, fetchedProduct);
        } catch (Exception e) {
            logger.info("StdPriceDubboUpdateWorker updatePtmStdPriceBySpiderFetchedProduct fail " + stdPriceId);
            e.printStackTrace();
        }

        logger.info("StdPriceDubboUpdateWorker success " + fetchedProduct.getWebsite() + "_" + fetchedProduct.getSkuStatus() + "_" + stdPriceId);

        try {
            ptmStdPriceService.createPtmStdPriceImage(stdPriceId, fetchedProduct);
        } catch (Exception e) {
            logger.info("StdPriceDubboUpdateWorker createPtmStdPriceImage fail " + stdPriceId);
        }

//            如果降价且CommentsNumber 大于40写入队列，并且状态必须是onsale
        if (price > fetchedProduct.getPrice() && fetchedProduct.getCommentsNumber() > 40 && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
            redisListService.push(STDPRICE_DROP_SKUID_QUEUE, stdPriceId + "");
            logger.info("price drop StdPriceDubboUpdateWorker add to queue success " + stdPriceId);
        }
    }
}
