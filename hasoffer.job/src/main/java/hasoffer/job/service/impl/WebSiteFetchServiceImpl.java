package hasoffer.job.service.impl;

import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.api.fetch.service.IFetchService;
import hasoffer.job.service.IWebSiteFetchService;
import hasoffer.job.worker.UnmatchedSearchRecordListWorker2;
import hasoffer.job.worker.UnmatchedSearchRecordProcessWorker2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service("webSiteFetchService")
public class WebSiteFetchServiceImpl implements IWebSiteFetchService {

    private Logger logger = LoggerFactory.getLogger(WebSiteFetchServiceImpl.class);

    @Resource
    ISearchService searchService;

    @Resource
    IDataBaseManager dbm;

    @Resource
    SearchProductService searchProductService;

    @Resource
    IFetchService flipkartFetchService;

    @Override
    public void fetchProduct2Mongodb() {
        ExecutorService es = Executors.newCachedThreadPool();

        LinkedBlockingQueue<SrmSearchLog> searchLogQueue = new LinkedBlockingQueue<SrmSearchLog>();
        es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordListWorker2(searchService, dbm, searchLogQueue)));
        for (int i = 0; i < 20; i++) {
            es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordProcessWorker2(searchProductService, searchService,flipkartFetchService, searchLogQueue)));
        }

        while (true) {
            try {
                TimeUnit.MINUTES.sleep(30);
                logger.debug("AutoSearchMatchController-new fetcher");
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
    }
}
