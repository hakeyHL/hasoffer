package hasoffer.job.worker;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.api.fetch.service.IFetchService;
import hasoffer.fetch.model.ListProduct;
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
public class UnmatchedSearchRecordProcessWorker2 implements Runnable {

    private Logger logger = LoggerFactory.getLogger(UnmatchedSearchRecordProcessWorker2.class);

    LinkedBlockingQueue<SrmSearchLog> searchLogQueue;
    ISearchService searchService;
    SearchProductService searchProductService;



    IFetchService flipkartFetchService;

    public UnmatchedSearchRecordProcessWorker2(SearchProductService searchProductService, ISearchService searchService, IFetchService flipkartFetchService,LinkedBlockingQueue<SrmSearchLog> searchLogQueue) {
        this.searchProductService = searchProductService;
        this.searchService = searchService;
        this.searchLogQueue = searchLogQueue;
        this.flipkartFetchService = flipkartFetchService;
    }

    public void run() {
        logger.debug("UnmatchedSearchRecordProcessWorker2 START");

        while(true) {

            try {

                SrmSearchLog searchLog = searchLogQueue.poll();

                if (searchLog == null) {
                    TimeUnit.SECONDS.sleep(5);
                    logger.debug("UnmatchedSearchRecordProcessWorker2 . search-log-queue job has no jobs. go to sleep!");
                    continue;
                }

                SrmAutoSearchResult autoSearchResult = new SrmAutoSearchResult(searchLog);

                String keyword = autoSearchResult.getTitle();
                Website logSite = Website.valueOf(autoSearchResult.getFromWebsite());

                Map<Website, List<ListProduct>> listProductMap = new HashMap<Website, List<ListProduct>>();

                // read from html
                List<ListProduct> listProducts = listProductMap.get(logSite);
                if (listProducts == null) {
                    listProducts = new ArrayList<ListProduct>();
                }
                listProducts.addAll(flipkartFetchService.getProductsKeyWord(logSite, keyword, 0, 10).getListProducts());

                autoSearchResult.setSitePros(listProductMap);

                // fetch
                searchProductService.searchProductsFromSites(autoSearchResult);


            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
    }

}
