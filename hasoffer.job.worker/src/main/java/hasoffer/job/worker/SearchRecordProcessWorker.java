package hasoffer.job.worker;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
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
    private IFetchDubboService flipkartFetchService;

    public SearchRecordProcessWorker(SearchProductService searchProductService, IFetchDubboService flipkartFetchService, LinkedBlockingQueue<SrmSearchLog> searchLogQueue) {
        this.searchProductService = searchProductService;
        this.searchLogQueue = searchLogQueue;
        this.flipkartFetchService = flipkartFetchService;
    }

    @Override
    public void run() {

        while (true) {
            try {
                SrmSearchLog searchLog = searchLogQueue.poll();
                if (searchLog == null) {
                    logger.debug("SearchRecordProcessWorker. search-log-queue is null. go to sleep!");
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                }
                //logger.info("SearchRecordProcessWorker. search keyword {}. begin", searchLog);
                SrmAutoSearchResult autoSearchResult = new SrmAutoSearchResult(searchLog);

                String keyword = autoSearchResult.getTitle();

                Map<Website, List<ListProduct>> listProductMap = new HashMap<Website, List<ListProduct>>();
                FetchResult flipkartFetchResult = flipkartFetchService.getProductsKeyWord(Website.FLIPKART, keyword, 0, 10);
                FetchResult amazonFetchResult = flipkartFetchService.getProductsKeyWord(Website.AMAZON, keyword, 0, 10);
                FetchResult snapdealFetchResult = flipkartFetchService.getProductsKeyWord(Website.SNAPDEAL, keyword, 0, 10);
                FetchResult shopcluesFetchResult = flipkartFetchService.getProductsKeyWord(Website.SHOPCLUES, keyword, 0, 10);
                FetchResult paytmFetchResult = flipkartFetchService.getProductsKeyWord(Website.PAYTM, keyword, 0, 10);
                FetchResult ebayFetchResult = flipkartFetchService.getProductsKeyWord(Website.EBAY, keyword, 0, 10);
                FetchResult myntraFetchResult = flipkartFetchService.getProductsKeyWord(Website.MYNTRA, keyword, 0, 10);
                FetchResult jabongFetchResult = flipkartFetchService.getProductsKeyWord(Website.JABONG, keyword, 0, 10);
                FetchResult voonikFetchResult = flipkartFetchService.getProductsKeyWord(Website.VOONIK, keyword, 0, 10);
                Boolean isFinish = isFinish(flipkartFetchResult);
                isFinish = isFinish && isFinish(amazonFetchResult);
                isFinish = isFinish && isFinish(snapdealFetchResult);
                isFinish = isFinish && isFinish(shopcluesFetchResult);
                isFinish = isFinish && isFinish(paytmFetchResult);
                isFinish = isFinish && isFinish(ebayFetchResult);
                isFinish = isFinish && isFinish(myntraFetchResult);
                isFinish = isFinish && isFinish(jabongFetchResult);
                isFinish = isFinish && isFinish(voonikFetchResult);
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
                    autoSearchResult.setSitePros(listProductMap);
                    searchProductService.searchProductsFromSites(autoSearchResult);
                    logger.info("SearchRecordProcessWorker.flipkartFetchResult  result()--keyword is {} : size() = {}", keyword, flipkartFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.amazonFetchResult    result()--keyword is {} : size() = {}", keyword, amazonFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.snapdealFetchResult  result()--keyword is {} : size() = {}", keyword, snapdealFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.shopcluesFetchResult result()--keyword is {} : size() = {}", keyword, shopcluesFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.paytmFetchResult     result()--keyword is {} : size() = {}", keyword, paytmFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.ebayFetchResult      result()--keyword is {} : size() = {}", keyword, ebayFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.myntraFetchResult    result()--keyword is {} : size() = {}", keyword, myntraFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.jabongFetchResult    result()--keyword is {} : size() = {}", keyword, jabongFetchResult.getFetchProducts().size());
                    logger.info("SearchRecordProcessWorker.voonikFetchResult    result()--keyword is {} : size() = {}", keyword, voonikFetchResult.getFetchProducts().size());
                } else {
                    searchLogQueue.put(searchLog);
                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private boolean isFinish(FetchResult fetchResult){
        return TaskStatus.FINISH.equals(fetchResult.getTaskStatus())||TaskStatus.STOPPED.equals(fetchResult.getTaskStatus());
    }

    private void initResultMap(Map<Website, List<ListProduct>> listProductMap,FetchResult fetchResult ){
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
