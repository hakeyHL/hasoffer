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
                    System.out.println("fetchUrlResult get null sleep 10 MINUTES StdPriceDubboUpdateWorker");
                    continue;
                }
                FetchUrlResult fetchUrlResult = JSONUtil.toObject(fetchUrlResultStr, FetchUrlResult.class);
//                popNumber--;
                if (fetchUrlResult.getUrl() == null) {
                    System.out.println("fetchUrlResult.getUrl() null StdPriceDubboUpdateWorker");
                    continue;
                }

                System.out.println("pop get StdPriceDubboUpdateWorker response success " + fetchUrlResult.getWebsite());
                String url = fetchUrlResult.getUrl();
                Website website = fetchUrlResult.getWebsite();

                TaskStatus taskStatus = fetchUrlResult.getTaskStatus();

                if (TaskStatus.FINISH.equals(taskStatus)) {
                    System.out.println("taskStatus is finish StdPriceDubboUpdateWorker " + website);
                    if (Website.FLIPKART.equals(website)) {
                        System.out.println("pop get StdPriceDubboUpdateWorker flipkart finish result");
                    }

                    String urlKey = HexDigestUtil.md5(url);
                    List<PtmStdPrice> stdPriceList = ptmStdPriceService.getPtmstdPriceListByUrlKey(urlKey);

                    if (stdPriceList == null || stdPriceList.size() == 0) {
                        System.out.println("urkKey StdPriceDubboUpdateWorker not found " + website + "url = " + url);
                    } else {
                        System.out.println("urkKey found StdPriceDubboUpdateWorker " + website + " skulist begin to update " + stdPriceList.size());
                        for (PtmStdPrice ptmStdPrice : stdPriceList) {
                            updatePtmCmpSku(ptmStdPrice, fetchUrlResult);
                            System.out.println("update success StdPriceDubboUpdateWorker for " + ptmStdPrice.getWebsite());
                        }
                    }
                } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
                    System.out.println("taskStatus is StdPriceDubboUpdateWorker exception " + website);
                } else {
                    System.out.println("taskStatus is StdPriceDubboUpdateWorker " + taskStatus + "_" + website);
                }
            } catch (Exception e) {
                System.out.println("StdPriceDubboUpdateWorker.run() exception.");
                e.printStackTrace();
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
            System.out.println("website StdPriceDubboUpdateWorker is null for _" + stdPriceId + "_");
            return;
        }

        FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

        System.out.println("fetch result is :" + fetchedProduct);

        try {
            //
            ptmStdPriceService.updatePtmStdPriceBySpiderFetchedProduct(stdPriceId, fetchedProduct);
        } catch (Exception e) {
            System.out.println("StdPriceDubboUpdateWorker updatePtmStdPriceBySpiderFetchedProduct fail " + stdPriceId);
            e.printStackTrace();
        }

        System.out.println("StdPriceDubboUpdateWorker success " + fetchedProduct.getWebsite() + "_" + fetchedProduct.getSkuStatus() + "_" + stdPriceId);

        try {
            ptmStdPriceService.createPtmStdPriceImage(stdPriceId, fetchedProduct);
        } catch (Exception e) {
            System.out.println("StdPriceDubboUpdateWorker createPtmStdPriceImage fail " + stdPriceId);
        }

//            如果降价且CommentsNumber 大于40写入队列，并且状态必须是onsale
        if (price > fetchedProduct.getPrice() && fetchedProduct.getCommentsNumber() > 40 && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
            redisListService.push(STDPRICE_DROP_SKUID_QUEUE, stdPriceId + "");
            System.out.println("price drop StdPriceDubboUpdateWorker add to queue success " + stdPriceId);
        }
    }
}
