package hasoffer.spider.result.service.impl;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.spider.result.api.IProductResultDubboService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.WebFetchResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProductResultDubboServiceImpl implements IProductResultDubboService {

    private final Logger logger = LoggerFactory.getLogger(ProductResultDubboServiceImpl.class);

    @Resource
    private SearchProductService searchProductService;

    @Resource
    private ISearchService searchService;

    public ProductResultDubboServiceImpl() {
    }

    //@Override
    //public void cacheProductTask(SpiderProductTask spiderProductTask) {
    //    // 1. 在mongo中缓存该对象。
    //    Query query = new Query();
    //    query.addCriteria(Criteria.where("_id").is(spiderProductTask.getTitle()));//2.使用flipkart网站过滤，
    //    List<SrmAutoSearchResult> searchResultTemp = mdm.query(SrmAutoSearchResult.class, query);
    //    if (ArrayUtils.isNullOrEmpty(searchResultTemp)) {
    //        SrmAutoSearchResult searchResult = new SrmAutoSearchResult();
    //        searchResult.setTitle(spiderProductTask.getTitle());
    //        searchResult.setFromWebsite(spiderProductTask.getWebsite().toString());
    //
    //    } else {
    //
    //    }
    //
    //}

    @Override
    public void updateListProduct(String productId, Website website, List<FetchedProduct> productList) {

        // 1.查找缓存中该商品的列表；
        SrmAutoSearchResult autoSearchResult = searchProductService.getSearchResultById(productId);
        autoSearchResult.setUpdateTime(new Date());
        Map<Website, WebFetchResult> sitePros = autoSearchResult.getSitePros();
        WebFetchResult webFetchResult = sitePros.get(website);
        if (webFetchResult == null) {
            webFetchResult = new WebFetchResult();
            sitePros.put(website, webFetchResult);
        }
        webFetchResult.setProductList(transferObj(productList));
        webFetchResult.setUpdateDate(new Date());
        Update update = new Update();
        if (autoSearchResult.getUpdateTime() != null) {
            update.set("updateTime", autoSearchResult.getUpdateTime());
            update.set("lUpdateTime", autoSearchResult.getUpdateTime().getTime());
        }
        update.set("sitePros." + website, webFetchResult);

        // 2.更新该商品的对应网站的数据；
        searchProductService.update(productId, update);
        // 3.调用匹配接口去匹配数据；
        analysisAndRelate(autoSearchResult);


    }

    @Override
    public void updateDetailProduct(String productId, FetchedProduct productList) {

    }

    private List<ListProduct> transferObj(List<FetchedProduct> fetchedProductList) {
        List<ListProduct> listProducts = new ArrayList<>();
        for (FetchedProduct product : fetchedProductList) {
            //String sourceId, String url, String imageUrl, String title, float price, Website website, ProductStatus status
            ProductStatus productStatus = ProductStatus.ONSALE;
            if (SkuStatus.OFFSALE.equals(product.getSkuStatus())) {
                productStatus = productStatus.OFFSALE;
            } else if (SkuStatus.OUTSTOCK.equals(product.getSkuStatus())) {
                productStatus = productStatus.OUTSTOCK;
            }
            ListProduct listProduct = new ListProduct(product.getSourceId(), product.getUrl(), product.getImageUrl(), product.getTitle(), product.getPrice(), product.getWebsite(), productStatus);
            listProducts.add(listProduct);
        }
        return listProducts;
    }

    private void analysisAndRelate(SrmAutoSearchResult autoSearchResult) {
        try {
            searchService.analysisAndRelate(autoSearchResult);
        } catch (Exception e) {
            logger.debug("[" + autoSearchResult.getId() + "]" + e.getMessage());
        }

    }
}
