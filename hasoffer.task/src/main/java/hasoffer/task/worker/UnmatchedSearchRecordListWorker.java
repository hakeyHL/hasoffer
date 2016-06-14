package hasoffer.task.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function :
 */
public class UnmatchedSearchRecordListWorker implements Runnable {
    LinkedBlockingQueue<SrmSearchLog> searchLogQueue;
    IProductService productService;
    ISearchService searchService;
    private Logger logger = LoggerFactory.getLogger(UnmatchedSearchRecordListWorker.class);

    public UnmatchedSearchRecordListWorker(IProductService productService, ISearchService searchService,
                                           LinkedBlockingQueue<SrmSearchLog> searchLogQueue) {
        this.productService = productService;
        this.searchService = searchService;
        this.searchLogQueue = searchLogQueue;
    }

    public void run() {

        Date startDate = null;
        while (true) {
            logger.debug("UnmatchedSearchRecordListWorker START");
            try {
                if (searchLogQueue.size() > 1800) {
                    TimeUnit.SECONDS.sleep(10);
                    logger.debug("UnmatchedSearchRecordListWorker go to sleep!");
                    continue;
                }

                PageableResult<SrmSearchLog> pagedSearchLog = searchService.listNoresultSearchLogs(startDate, 1, 1000);
                List<SrmSearchLog> searchLogs = pagedSearchLog.getData();

                if (ArrayUtils.hasObjs(searchLogs)) {
                    startDate = searchLogs.get(searchLogs.size() - 1).getCreateTime();
                    searchLogQueue.addAll(searchLogs);
                } else {
                    TimeUnit.SECONDS.sleep(20);
                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }
    }
}
