package hasoffer.job.worker;

import hasoffer.base.utils.HexDigestUtil;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.search.SearchProductService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.WebFetchResult;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spider.redis.service.IFetchCacheService;
import hasoffer.spring.context.SpringContextHolder;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DealozFetchWorker implements Runnable {

    private SearchProductService searchProductService;
    private IFetchCacheService fetchCacheService;

    public DealozFetchWorker(SearchProductService searchProductService, IDataBaseManager dbm) {
        this.searchProductService = searchProductService;
        this.fetchCacheService = (IFetchCacheService) SpringContextHolder.getBean("fetchCacheService");
    }

    @Override
    public void run() {
        while (true) {
            FetchResult fetchResult = fetchCacheService.popFetchResult(RedisKeysUtils.DEALOZ_RESULT_LIST);
            if (fetchResult == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                String id = HexDigestUtil.md5(fetchResult.getKeyword() + "-" + fetchResult.getWebsite());
                SrmAutoSearchResult autoSearchResult = searchProductService.getSearchResultById(id);
                if (autoSearchResult == null) {
                    autoSearchResult = new SrmAutoSearchResult();
                    autoSearchResult.setId(id);
                }
                initResultMap(autoSearchResult, fetchResult);
                searchProductService.saveSearchProducts(autoSearchResult);
            }
        }

    }

    private void initResultMap(SrmAutoSearchResult autoSearchResult, FetchResult fetchResult) {
        //1 判断抓取有没有返回商品，没有的话直接退出。
        if (fetchResult == null) {
            return;
        }
        List<FetchedProduct> fetchProducts = fetchResult.getFetchProducts();
        for (FetchedProduct fetchedProduct : fetchProducts) {
            if (!fetchedProduct.getUrl().contains("www.dealoz.com")) {
                WebFetchResult webFetchResult = autoSearchResult.getSitePros().get(fetchedProduct.getWebsite());
                if (webFetchResult == null) {
                    webFetchResult = new WebFetchResult();
                    autoSearchResult.getSitePros().put(fetchedProduct.getWebsite(), webFetchResult);
                }
                List<ListProduct> listProducts = webFetchResult.getProductList();
                ListProduct listProduct = new ListProduct();
                listProduct.setImageUrl(fetchedProduct.getImageUrl());
                listProduct.setPrice(fetchedProduct.getPrice());
                listProduct.setSourceId(fetchedProduct.getSourceId());
                listProduct.setStatus(ProductStatus.ONSALE);
                listProduct.setSubTitle(fetchedProduct.getSubTitle());
                listProduct.setTitle(fetchedProduct.getTitle());
                listProduct.setUrl(fetchedProduct.getUrl());
                listProduct.setWebsite(fetchedProduct.getWebsite());
                listProducts.add(listProduct);
            }
        }

    }
}
