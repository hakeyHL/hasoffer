package hasoffer.job.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.SearchProductService;
import hasoffer.job.service.ISearchRecordListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function : 旧数据修复
 */
public class SearchRecordListWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SearchRecordListWorker.class);

    private LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue;
    private SearchProductService searchProductService;
    private ISearchRecordListService searchRecordListService;

    public SearchRecordListWorker(SearchProductService searchProductService, ISearchRecordListService service, LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue) {
        this.searchLogQueue = searchLogQueue;
        this.searchRecordListService = service;
        this.searchProductService = searchProductService;
    }

    public void run() {
        //String PATTERN_TIME = "yyyy-MM-dd HH:mm:ss";

        while (true) {
            try {
                if (searchLogQueue.size() > 5000) {
                    TimeUnit.MINUTES.sleep(10);
                    logger.debug("SearchRecordListWorker go to sleep!");
                    continue;
                }
                logger.debug("SearchRecordListWorker START {}.Queue size {}", TimeUtils.parse(new Date(), "yyyy-MM-dd HH:mm:ss"), searchLogQueue.size());

                PageableResult<SrmSearchLog> pagedSearchLog = searchRecordListService.getLastNoProductLog();

                List<SrmSearchLog> searchLogs = pagedSearchLog.getData();

                if (ArrayUtils.hasObjs(searchLogs)) {
                    for (SrmSearchLog searchLog : searchLogs) {
                        if (searchLog.getPrecise() == SearchPrecise.MANUALSET) {
                            continue;
                        }

                        if (searchLog.getPtmProductId() > 15 * 10000 && searchLog.getPtmProductId() <= 80 * 10000) {
                            searchLog.setPtmProductId(0);
                        }

                        searchLogQueue.add(getHistoryData(searchLog));
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }
    }

    private SrmAutoSearchResult getHistoryData(SrmSearchLog searchLog) {
        SrmAutoSearchResult autoSearchResult = searchProductService.getSearchResult(searchLog);
        if (autoSearchResult == null) {
            autoSearchResult = new SrmAutoSearchResult(searchLog);
        }
        return autoSearchResult;
    }

}
