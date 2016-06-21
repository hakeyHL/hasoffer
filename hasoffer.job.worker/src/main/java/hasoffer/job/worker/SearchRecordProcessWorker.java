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
                logger.debug("SearchRecordProcessWorker. search keyword {}. begin",searchLog);
                SrmAutoSearchResult autoSearchResult = new SrmAutoSearchResult(searchLog);

                String keyword = autoSearchResult.getTitle();
                Website website = Website.valueOf(autoSearchResult.getFromWebsite());

                Map<Website, List<ListProduct>> listProductMap = new HashMap<Website, List<ListProduct>>();

                // read from html
                List<ListProduct> listProducts = listProductMap.get(website);
                if (listProducts == null) {
                    listProducts = new ArrayList<ListProduct>();
                    listProductMap.put(website, listProducts);
                }
                FetchResult productsKeyWord = flipkartFetchService.getProductsKeyWord(website, keyword, 0, 10);
                TaskStatus taskStatus = productsKeyWord.getTaskStatus();
                if (TaskStatus.FINSH.equals(taskStatus) || TaskStatus.STOPPED.equals(taskStatus)) {
                    List<FetchedProduct> listProductsResult = productsKeyWord.getFetchProducts();

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
                    autoSearchResult.setSitePros(listProductMap);
                    searchProductService.searchProductsFromSites(autoSearchResult);
                }else {
                    searchLogQueue.put(searchLog);
                }

            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
    }

}
