package hasoffer.job.worker;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.HasofferRegion;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.WebFetchResult;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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

    private LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue;
    private SearchProductService searchProductService;
    private IFetchDubboService fetchService;

    public SearchRecordProcessWorker(SearchProductService searchProductService, IFetchDubboService flipkartFetchService, LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue) {
        this.searchProductService = searchProductService;
        this.searchLogQueue = searchLogQueue;
        this.fetchService = flipkartFetchService;
    }

    @Override
    public void run() {

        while (true) {
            try {
                SrmAutoSearchResult autoSearchResult = searchLogQueue.poll();
                if (autoSearchResult == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SearchRecordProcessWorker. search-log-queue is null. go to sleep!");
                    }
                    TimeUnit.SECONDS.sleep(30);
                    continue;
                }
                //if (logger.isDebugEnabled()) {
                //    logger.debug("SearchRecordProcessWorker. search keyword {}. begin", autoSearchResult);
                //}

                // 获取mongo 中存储的数据并转换成java对象。
                boolean isUpdate = false;
                String serRegion = AppConfig.get(AppConfig.SER_REGION);
                if (HasofferRegion.INDIA.toString().equals(serRegion)) {
                    isUpdate = fetchForIndia(autoSearchResult);
                } else if (HasofferRegion.USA.toString().equals(serRegion)) {
                    isUpdate = fetchForUsa(autoSearchResult);
                }

                if (isUpdate) {
                    autoSearchResult.setUpdateTime(new Date());
                    searchProductService.saveSearchProducts(autoSearchResult);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 抓取并判断是否需要更新到mongodb中。
     *
     * @param autoSearchResult
     * @return
     */
    private boolean fetchForUsa(SrmAutoSearchResult autoSearchResult) {

        String keyword = StringUtils.getCleanWordString(autoSearchResult.getTitle());
        Map<Website, WebFetchResult> sitePros = autoSearchResult.getSitePros();
        FetchResult amazonFetchResult = getFetchResult(Website.AMAZON, keyword, sitePros);
        FetchResult ebayFetchResult = getFetchResult(Website.EBAY, keyword, sitePros);
        FetchResult walmartFetchResult = getFetchResult(Website.WALMART, keyword, sitePros);
        FetchResult geekFetchResult = getFetchResult(Website.GEEK, keyword, sitePros);
        FetchResult newEggFetchResult = getFetchResult(Website.NEWEGG, keyword, sitePros);
        FetchResult bestbuyFetchResult = getFetchResult(Website.BESTBUY, keyword, sitePros);

        initResultMap(autoSearchResult, amazonFetchResult);
        initResultMap(autoSearchResult, ebayFetchResult);
        initResultMap(autoSearchResult, walmartFetchResult);
        initResultMap(autoSearchResult, geekFetchResult);
        initResultMap(autoSearchResult, newEggFetchResult);
        initResultMap(autoSearchResult, bestbuyFetchResult);

        //判断是否需要重新抓取，如果需要，这放回队列中。
        if (isReFetch(amazonFetchResult, ebayFetchResult, walmartFetchResult, geekFetchResult, newEggFetchResult, bestbuyFetchResult)) {
            searchLogQueue.add(autoSearchResult);
        }

        return isUpdate(amazonFetchResult, ebayFetchResult, walmartFetchResult, geekFetchResult, newEggFetchResult, bestbuyFetchResult);

    }

    /**
     * 1.判断是否需要更新该网站（website）的该商品（keyword）<br>
     * 2.需要的话，则加入更新队列。并返回一个实体。如果不需要，则返回空。
     *
     * @param website
     * @param keyword
     * @param sitePros
     * @return
     */
    private FetchResult getFetchResult(Website website, String keyword, Map<Website, WebFetchResult> sitePros) {
        WebFetchResult fetchResult = sitePros.get(website);
        long updateCycle = TimeUtils.MILLISECONDS_OF_1_HOUR * 12;
        //判断是否需要更新该网站（website）的该商品（keyword）
        boolean isFetch = fetchResult == null || System.currentTimeMillis() - fetchResult.getlUpdateDate() > updateCycle;
        // 需要的话，则加入更新队列。并返回一个实体。如果不需要，这返回空
        if (isFetch) {
            try {
                return fetchService.getProductsKeyWord(website, keyword, 0, 10);
            } catch (Exception e) {
                FetchResult temp = new FetchResult(website, keyword);
                temp.setTaskStatus(TaskStatus.START);
                return temp;
            }
        }
        return null;
    }

    private boolean fetchForIndia(SrmAutoSearchResult autoSearchResult) {

        String keyword = StringUtils.getCleanWordString(autoSearchResult.getTitle());
        Map<Website, WebFetchResult> sitePros = autoSearchResult.getSitePros();
        FetchResult flipkartFetchResult = getFetchResult(Website.FLIPKART, keyword, sitePros);
        FetchResult amazonFetchResult = getFetchResult(Website.AMAZON, keyword, sitePros);
        FetchResult snapdealFetchResult = getFetchResult(Website.SNAPDEAL, keyword, sitePros);
        FetchResult shopcluesFetchResult = getFetchResult(Website.SHOPCLUES, keyword, sitePros);
        FetchResult paytmFetchResult = getFetchResult(Website.PAYTM, keyword, sitePros);
        FetchResult ebayFetchResult = getFetchResult(Website.EBAY, keyword, sitePros);
        FetchResult myntraFetchResult = getFetchResult(Website.MYNTRA, keyword, sitePros);
        FetchResult jabongFetchResult = getFetchResult(Website.JABONG, keyword, sitePros);
        FetchResult voonikFetchResult = getFetchResult(Website.VOONIK, keyword, sitePros);
        FetchResult homeShopResult = getFetchResult(Website.HOMESHOP18, keyword, sitePros);
        FetchResult limeRoadResult = getFetchResult(Website.LIMEROAD, keyword, sitePros);
        initResultMap(autoSearchResult, flipkartFetchResult);
        initResultMap(autoSearchResult, amazonFetchResult);
        initResultMap(autoSearchResult, snapdealFetchResult);
        initResultMap(autoSearchResult, shopcluesFetchResult);
        initResultMap(autoSearchResult, paytmFetchResult);
        initResultMap(autoSearchResult, ebayFetchResult);
        initResultMap(autoSearchResult, myntraFetchResult);
        initResultMap(autoSearchResult, jabongFetchResult);
        initResultMap(autoSearchResult, voonikFetchResult);
        initResultMap(autoSearchResult, homeShopResult);
        initResultMap(autoSearchResult, limeRoadResult);

        if (isReFetch(flipkartFetchResult, amazonFetchResult, snapdealFetchResult, shopcluesFetchResult, paytmFetchResult, ebayFetchResult, myntraFetchResult, jabongFetchResult, voonikFetchResult, homeShopResult, limeRoadResult)) {
            searchLogQueue.add(autoSearchResult);
        }

        Boolean isUpdate = isUpdate(flipkartFetchResult, amazonFetchResult, snapdealFetchResult, shopcluesFetchResult, paytmFetchResult, ebayFetchResult, myntraFetchResult, jabongFetchResult, voonikFetchResult, homeShopResult, limeRoadResult);

        if (isUpdate && logger.isDebugEnabled()) {
            logger.debug("SearchRecordProcessWorker.flipkartFetchResult  result()--keyword is {} : size() = {}", keyword, flipkartFetchResult == null ? "" : flipkartFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.amazonFetchResult    result()--keyword is {} : size() = {}", keyword, amazonFetchResult == null ? "" : amazonFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.snapdealFetchResult  result()--keyword is {} : size() = {}", keyword, snapdealFetchResult == null ? "" : snapdealFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.shopcluesFetchResult result()--keyword is {} : size() = {}", keyword, shopcluesFetchResult == null ? "" : shopcluesFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.paytmFetchResult     result()--keyword is {} : size() = {}", keyword, paytmFetchResult == null ? "" : paytmFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.ebayFetchResult      result()--keyword is {} : size() = {}", keyword, ebayFetchResult == null ? "" : ebayFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.myntraFetchResult    result()--keyword is {} : size() = {}", keyword, myntraFetchResult == null ? "" : myntraFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.jabongFetchResult    result()--keyword is {} : size() = {}", keyword, jabongFetchResult == null ? "" : jabongFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.voonikFetchResult    result()--keyword is {} : size() = {}", keyword, voonikFetchResult == null ? "" : voonikFetchResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.homeShopResult       result()--keyword is {} : size() = {}", keyword, homeShopResult == null ? "" : homeShopResult.getFetchProducts().size());
            logger.debug("SearchRecordProcessWorker.limeRoadResult       result()--keyword is {} : size() = {}", keyword, limeRoadResult == null ? "" : limeRoadResult.getFetchProducts().size());
        }

        return isUpdate;
    }

    private boolean isReFetch(FetchResult... fetchResultList) {
        for (FetchResult result : fetchResultList) {
            if (result != null && !isClose(result)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否更新到mongodb。
     *
     * @param fetchResultList
     * @return
     */
    private boolean isUpdate(FetchResult... fetchResultList) {
        boolean b = false;
        for (FetchResult fetchResult : fetchResultList) {
            b = b || isClose(fetchResult);
        }
        return b;
    }

    private boolean isClose(FetchResult result) {
        return result != null && (TaskStatus.FINISH.equals(result.getTaskStatus()) || TaskStatus.STOPPED.equals(result.getTaskStatus()));
    }

    private void initResultMap(SrmAutoSearchResult autoSearchResult, FetchResult fetchResult) {
        //1 判断抓取有没有返回商品，没有的话直接退出。
        if (fetchResult == null || isClose(fetchResult)) {
            return;
        }
        Map<Website, WebFetchResult> fetchResultMap = autoSearchResult.getSitePros();
        WebFetchResult webFetchResult = fetchResultMap.get(fetchResult.getWebsite());
        if (webFetchResult == null) {
            webFetchResult = new WebFetchResult();
            fetchResultMap.put(fetchResult.getWebsite(), webFetchResult);
        }
        webFetchResult.setUpdateDate(new Date());
        webFetchResult.setTaskStatus(fetchResult.getTaskStatus());
        List<ListProduct> listProducts = webFetchResult.getProductList();
        List<FetchedProduct> listProductsResult = fetchResult.getFetchProducts();
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
        webFetchResult.setProductList(listProducts);
    }
}
