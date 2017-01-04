package hasoffer.job.worker;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.HasofferRegion;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.core.search.impl.SearchServiceImpl;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.WebFetchResult;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function :
 */
public class SearchRecordResultWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SearchRecordResultWorker.class);

    private SearchProductService searchProductService;
    private IFetchDubboService fetchService;
    private ISearchService searchService;

    public SearchRecordResultWorker(SearchProductService searchProductService, IFetchDubboService flipkartFetchService) {
        this.searchProductService = searchProductService;
        this.fetchService = flipkartFetchService;
        this.searchService = SpringContextHolder.getBean(SearchServiceImpl.class);
    }

    @Override
    public void run() {

        while (true) {
            try {

                // 获取mongo 中存储的数据并转换成java对象。
                String serRegion = AppConfig.get(AppConfig.SER_REGION);
                if (HasofferRegion.INDIA.toString().equals(serRegion)) {
                    FetchResult fetchResult = fetchService.popProductsKeyWord();
                    if (fetchResult == null || fetchResult.getKeyword() == null) {
                        TimeUnit.MINUTES.sleep(10);
                        continue;
                    }
                    fetchForIndia(fetchResult);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void fetchForIndia(FetchResult fetchResult) {
        String key = HexDigestUtil.md5(fetchResult.getKeyword());
        SrmAutoSearchResult autoSearchResult = searchProductService.getSearchResultById(key);
        if (logger.isDebugEnabled()) {
            logger.debug("fetchForIndia: {}", autoSearchResult);
        }
        initResultMap(autoSearchResult, fetchResult);
        updateMongo(autoSearchResult);
        analysisAndRelate(autoSearchResult);
    }


    private void updateMongo(SrmAutoSearchResult autoSearchResult) {
        autoSearchResult.setUpdateTime(new Date());
        searchProductService.saveSearchProducts(autoSearchResult);
    }

    private void analysisAndRelate(SrmAutoSearchResult autoSearchResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("analysisAndRelate: {}", autoSearchResult);
        }
        try {
            searchService.analysisAndRelate(autoSearchResult);
        } catch (Exception e) {
            logger.debug("[" + autoSearchResult.getId() + "]" + e.getMessage());
        }

    }


    private void initResultMap(SrmAutoSearchResult autoSearchResult, FetchResult fetchResult) {
        //1 判断抓取有没有返回商品，没有的话直接退出。
        if (fetchResult == null || !fetchResult.overFetch()) {
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
        //List<ListProduct> listProducts = webFetchResult.getProductList();
        List<ListProduct> listProducts = new ArrayList<>();
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
