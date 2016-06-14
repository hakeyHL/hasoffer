package hasoffer.timer.product.worker;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductHelper;
import hasoffer.fetch.model.ListProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function :
 */
public class UnmatchedSearchRecordProcessWorker implements Runnable {

    LinkedBlockingQueue<SrmSearchLog> searchLogQueue;
    IProductService productService;
    ISearchService searchService;
    private Logger logger = LoggerFactory.getLogger(UnmatchedSearchRecordProcessWorker.class);

    public UnmatchedSearchRecordProcessWorker(IProductService productService, ISearchService searchService,
                                              LinkedBlockingQueue<SrmSearchLog> searchLogQueue) {
        this.productService = productService;
        this.searchService = searchService;
        this.searchLogQueue = searchLogQueue;
    }

    public void run() {
        logger.debug("UnmatchedSearchRecordProcessWorker START");

        while (true) {

            try {

                SrmSearchLog searchLog = searchLogQueue.poll();

                if (searchLog == null) {
                    TimeUnit.SECONDS.sleep(5);
                    logger.debug("UnmatchedSearchRecordProcessWorker . search-log-queue job has no jobs. go to sleep!");
                    continue;
                }

                String keyword = searchLog.getKeyword();

                Map<Website, ListProduct> listProductMap = SearchProductHelper.getProducts(searchLog);
                searchService.relateUnmatchedSearchLog(searchLog, listProductMap);

            } catch (Exception e) {
            }
        }
    }

}
