package hasoffer.job.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function : 旧数据修复
 */
public class SearchRecordListWorker implements Runnable {
    private static final String SQL_SEARCHLOG = "select t from SrmSearchLog t where t.updateTime >?0 order by t.updateTime ASC ";
    LinkedBlockingQueue<SrmSearchLog> searchLogQueue;
    ISearchService searchService;
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(SearchRecordListWorker.class);

    public SearchRecordListWorker(ISearchService searchService, IDataBaseManager dbm, LinkedBlockingQueue<SrmSearchLog> searchLogQueue) {
        this.dbm = dbm;
        this.searchService = searchService;
        this.searchLogQueue = searchLogQueue;
    }

    public void run() {
        String PATTERN_TIME = "yyyy-MM-dd HH:mm:ss";
        Date startTime = new Date(TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_HOUR);

        while (true) {

            logger.debug("SearchRecordListWorker START {}.Queue size {}", TimeUtils.parse(startTime, PATTERN_TIME), searchLogQueue.size());

            try {
                if (searchLogQueue.size() > 1000) {
                    TimeUnit.MINUTES.sleep(2);
                    logger.debug("SearchRecordListWorker go to sleep!");
                    continue;
                }
                if (startTime.compareTo(TimeUtils.nowDate()) > 0) {
                    TimeUnit.MINUTES.sleep(10);
                }
                Date searchTime = startTime;
                PageableResult<SrmSearchLog> pagedSearchLog = dbm.queryPage(SQL_SEARCHLOG, 1, 1000, Collections.singletonList(searchTime));

                List<SrmSearchLog> searchLogs = pagedSearchLog.getData();

                if (ArrayUtils.hasObjs(searchLogs)) {
                    for (SrmSearchLog searchLog : searchLogs) {
                        if (searchLog.getPrecise() == SearchPrecise.MANUALSET) {
                            continue;
                        }

                        if (searchLog.getPtmProductId() > 15 * 10000 && searchLog.getPtmProductId() <= 80 * 10000) {
                            searchLog.setPtmProductId(0);
                        }

                        searchLogQueue.add(searchLog);

                        startTime = searchLog.getUpdateTime();
                    }
                }

                if (startTime.compareTo(searchTime) == 0) {
                    startTime = TimeUtils.add(startTime, 1000);
                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }
    }
}
