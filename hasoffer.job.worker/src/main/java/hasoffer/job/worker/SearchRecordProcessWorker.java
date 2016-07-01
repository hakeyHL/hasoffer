package hasoffer.job.worker;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.HasofferRegion;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function :
 */
public class SearchRecordProcessWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SearchRecordProcessWorker.class);

    private LinkedBlockingQueue<SrmSearchLog> searchLogQueue;
    private SearchProductService searchProductService;
    private IFetchDubboService fetchService;

    public SearchRecordProcessWorker(SearchProductService searchProductService, IFetchDubboService flipkartFetchService, LinkedBlockingQueue<SrmSearchLog> searchLogQueue) {
        this.searchProductService = searchProductService;
        this.searchLogQueue = searchLogQueue;
        this.fetchService = flipkartFetchService;
    }

    @Override
    public void run() {

        while (true) {
            try {
                SrmSearchLog searchLog = searchLogQueue.poll();
                if (searchLog == null || "".equals(searchLog.getKeyword())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SearchRecordProcessWorker. search-log-queue is null. go to sleep!");
                    }
                    TimeUnit.MINUTES.sleep(1);
                    continue;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("SearchRecordProcessWorker. search keyword {}. begin", searchLog);
                }
                SrmAutoSearchResult autoSearchResult = new SrmAutoSearchResult(searchLog);

                String keyword = autoSearchResult.getTitle();
                String webSite = autoSearchResult.getFromWebsite();
                keyword = StringUtils.getCleanWordString(keyword);
                String serRegion = AppConfig.get(AppConfig.SER_REGION);
                Map<Website, List<ListProduct>> listProductMap = new HashMap<Website, List<ListProduct>>();
                if (HasofferRegion.INDIA.toString().equals(serRegion)) {
                    FetchResult flipkartFetchResult = fetchService.getProductsKeyWord(Website.FLIPKART, keyword, 0, 10);
                    FetchResult amazonFetchResult = fetchService.getProductsKeyWord(Website.AMAZON, keyword, 0, 10);
                    FetchResult snapdealFetchResult = fetchService.getProductsKeyWord(Website.SNAPDEAL, keyword, 0, 10);
                    FetchResult shopcluesFetchResult = fetchService.getProductsKeyWord(Website.SHOPCLUES, keyword, 0, 10);
                    FetchResult paytmFetchResult = fetchService.getProductsKeyWord(Website.PAYTM, keyword, 0, 10);
                    FetchResult ebayFetchResult = fetchService.getProductsKeyWord(Website.EBAY, keyword, 0, 10);
                    FetchResult myntraFetchResult = fetchService.getProductsKeyWord(Website.MYNTRA, keyword, 0, 10);
                    FetchResult jabongFetchResult = fetchService.getProductsKeyWord(Website.JABONG, keyword, 0, 10);
                    FetchResult voonikFetchResult = fetchService.getProductsKeyWord(Website.VOONIK, keyword, 0, 10);
                    FetchResult homeShopResult = fetchService.getProductsKeyWord(Website.HOMESHOP18, keyword, 0, 10);
                    FetchResult limeRoadResult = fetchService.getProductsKeyWord(Website.LIMEROAD, keyword, 0, 10);
                    Boolean isFinish = isFinish(flipkartFetchResult);
                    isFinish = isFinish && isFinish(amazonFetchResult);
                    isFinish = isFinish && isFinish(snapdealFetchResult);
                    isFinish = isFinish && isFinish(shopcluesFetchResult);
                    isFinish = isFinish && isFinish(paytmFetchResult);
                    isFinish = isFinish && isFinish(ebayFetchResult);
                    isFinish = isFinish && isFinish(myntraFetchResult);
                    isFinish = isFinish && isFinish(jabongFetchResult);
                    isFinish = isFinish && isFinish(voonikFetchResult);
                    isFinish = isFinish && isFinish(homeShopResult);
                    isFinish = isFinish && isFinish(limeRoadResult);
                    if (isFinish) {
                        initResultMap(listProductMap, flipkartFetchResult);
                        initResultMap(listProductMap, amazonFetchResult);
                        initResultMap(listProductMap, snapdealFetchResult);
                        initResultMap(listProductMap, shopcluesFetchResult);
                        initResultMap(listProductMap, paytmFetchResult);
                        initResultMap(listProductMap, ebayFetchResult);
                        initResultMap(listProductMap, myntraFetchResult);
                        initResultMap(listProductMap, jabongFetchResult);
                        initResultMap(listProductMap, voonikFetchResult);
                        initResultMap(listProductMap, homeShopResult);
                        initResultMap(listProductMap, limeRoadResult);
                        autoSearchResult.setSitePros(listProductMap);
                        searchProductService.searchProductsFromSites(autoSearchResult);
                        if (logger.isDebugEnabled()) {
                            logger.debug("SearchRecordProcessWorker.flipkartFetchResult  result()--keyword is {} : size() = {}", keyword, flipkartFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.amazonFetchResult    result()--keyword is {} : size() = {}", keyword, amazonFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.snapdealFetchResult  result()--keyword is {} : size() = {}", keyword, snapdealFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.shopcluesFetchResult result()--keyword is {} : size() = {}", keyword, shopcluesFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.paytmFetchResult     result()--keyword is {} : size() = {}", keyword, paytmFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.ebayFetchResult      result()--keyword is {} : size() = {}", keyword, ebayFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.myntraFetchResult    result()--keyword is {} : size() = {}", keyword, myntraFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.jabongFetchResult    result()--keyword is {} : size() = {}", keyword, jabongFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.voonikFetchResult    result()--keyword is {} : size() = {}", keyword, voonikFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.homeShopResult       result()--keyword is {} : size() = {}", keyword, homeShopResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.limeRoadResult       result()--keyword is {} : size() = {}", keyword, limeRoadResult.getFetchProducts().size());
                        }
                    } else {
                        searchLogQueue.put(searchLog);
                    }
                } else if (HasofferRegion.USA.toString().equals(serRegion)) {

                    FetchResult amazonFetchResult = fetchService.getProductsKeyWord(Website.AMAZON, keyword, 0, 10);
                    FetchResult ebayFetchResult = fetchService.getProductsKeyWord(Website.EBAY, keyword, 0, 10);
                    FetchResult walmartFetchResult = fetchService.getProductsKeyWord(Website.WALMART, keyword, 0, 10);
                    FetchResult geekFetchResult = fetchService.getProductsKeyWord(Website.GEEK, keyword, 0, 10);
                    FetchResult newEggFetchResult = fetchService.getProductsKeyWord(Website.NEWEGG, keyword, 0, 10);
                    FetchResult bestbuyFetchResult = fetchService.getProductsKeyWord(Website.BESTBUY, keyword, 0, 10);

                    Boolean isFinish = isFinish(amazonFetchResult);
                    isFinish = isFinish && isFinish(ebayFetchResult);
                    isFinish = isFinish && isFinish(walmartFetchResult);
                    isFinish = isFinish && isFinish(geekFetchResult);
                    isFinish = isFinish && isFinish(newEggFetchResult);
                    isFinish = isFinish && isFinish(bestbuyFetchResult);
                    if (isFinish) {
                        while (Website.AMAZON.toString().equals(webSite) && amazonFetchResult.getFetchProducts().size() == 0) {
                            TimeUnit.SECONDS.sleep(30);
                            amazonFetchResult = fetchService.getProductsKeyWord(Website.AMAZON, keyword, 0, 10);
                        }
                        while (Website.EBAY.toString().equals(webSite) && amazonFetchResult.getFetchProducts().size() == 0) {
                            TimeUnit.SECONDS.sleep(30);
                            amazonFetchResult = fetchService.getProductsKeyWord(Website.EBAY, keyword, 0, 10);
                        }
                        while (Website.WALMART.toString().equals(webSite) && amazonFetchResult.getFetchProducts().size() == 0) {
                            TimeUnit.SECONDS.sleep(30);
                            amazonFetchResult = fetchService.getProductsKeyWord(Website.WALMART, keyword, 0, 10);
                        }
                        while (Website.GEEK.toString().equals(webSite) && amazonFetchResult.getFetchProducts().size() == 0) {
                            TimeUnit.SECONDS.sleep(30);
                            amazonFetchResult = fetchService.getProductsKeyWord(Website.GEEK, keyword, 0, 10);
                        }
                        while (Website.NEWEGG.toString().equals(webSite) && amazonFetchResult.getFetchProducts().size() == 0) {
                            TimeUnit.SECONDS.sleep(30);
                            amazonFetchResult = fetchService.getProductsKeyWord(Website.NEWEGG, keyword, 0, 10);
                        }
                        while (Website.BESTBUY.toString().equals(webSite) && amazonFetchResult.getFetchProducts().size() == 0) {
                            TimeUnit.SECONDS.sleep(30);
                            amazonFetchResult = fetchService.getProductsKeyWord(Website.BESTBUY, keyword, 0, 10);
                        }
                        initResultMap(listProductMap, amazonFetchResult);
                        initResultMap(listProductMap, ebayFetchResult);
                        initResultMap(listProductMap, walmartFetchResult);
                        initResultMap(listProductMap, geekFetchResult);
                        initResultMap(listProductMap, newEggFetchResult);
                        initResultMap(listProductMap, bestbuyFetchResult);
                        autoSearchResult.setSitePros(listProductMap);
                        searchProductService.searchProductsFromSites(autoSearchResult);
                        if (logger.isDebugEnabled()) {
                            logger.debug("SearchRecordProcessWorker.amazonFetchResult    result()--keyword is {} : size() = {}", keyword, amazonFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.ebayFetchResult      result()--keyword is {} : size() = {}", keyword, ebayFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.walmartFetchResult   result()--keyword is {} : size() = {}", keyword, walmartFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.geekFetchResult      result()--keyword is {} : size() = {}", keyword, geekFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.newEggFetchResult    result()--keyword is {} : size() = {}", keyword, newEggFetchResult.getFetchProducts().size());
                            logger.debug("SearchRecordProcessWorker.bestbuyFetchResult   result()--keyword is {} : size() = {}", keyword, bestbuyFetchResult.getFetchProducts().size());
                        }
                    } else {
                        searchLogQueue.put(searchLog);
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private boolean isFinish(FetchResult fetchResult) {
        return TaskStatus.FINISH.equals(fetchResult.getTaskStatus()) || TaskStatus.STOPPED.equals(fetchResult.getTaskStatus());
    }

    private void initResultMap(Map<Website, List<ListProduct>> listProductMap, FetchResult fetchResult) {
        List<FetchedProduct> listProductsResult = fetchResult.getFetchProducts();
        List<ListProduct> listProducts = new ArrayList<ListProduct>();
        for (FetchedProduct product : listProductsResult) {
            ListProduct listProduct = new ListProduct();
            listProduct.setImageUrl(product.getImageUrl());
            listProduct.setPrice(product.getPrice());
            listProduct.setSourceId(product.getSourceId());
            SkuStatus skuStatus = product.getSkuStatus();
            if (SkuStatus.OFFSALE.equals(skuStatus)) {
                listProduct.setStatus(ProductStatus.OFFSALE);
            } else if (SkuStatus.ONSALE.equals(skuStatus)) {
                listProduct.setStatus(ProductStatus.ONSALE);
            } else if (SkuStatus.OUTSTOCK.equals(skuStatus)) {
                listProduct.setStatus(ProductStatus.OUTSTOCK);
            }
            listProduct.setSubTitle(product.getSubTitle());
            listProduct.setTitle(product.getTitle());
            listProduct.setUrl(product.getUrl());
            listProduct.setWebsite(product.getWebsite());
            listProducts.add(listProduct);

        }
        listProductMap.put(fetchResult.getWebsite(), listProducts);
    }
}
