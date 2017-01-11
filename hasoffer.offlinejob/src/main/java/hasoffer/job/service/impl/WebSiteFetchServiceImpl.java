package hasoffer.job.service.impl;

import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.service.ISearchRecordListService;
import hasoffer.job.service.IWebSiteFetchService;
import hasoffer.job.worker.SearchRecordListWorker;
import hasoffer.job.worker.SearchRecordProcessWorker;
import hasoffer.job.worker.SearchRecordResultWorker;
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

    @Resource
    ISearchService searchService;

    @Resource
    ISearchRecordListService searchRecordListService;

    @Resource
    SearchProductService searchProductService;
    @Resource
    IFetchDubboService fetchDubboService;
    private Logger logger = LoggerFactory.getLogger(WebSiteFetchServiceImpl.class);

    @Override
    public void fetchProduct2Mongodb() {
        ExecutorService es = Executors.newCachedThreadPool();

        LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue = new LinkedBlockingQueue<SrmAutoSearchResult>();

        es.execute(DaemonThreadFactory.create(new SearchRecordListWorker(searchProductService, searchRecordListService, searchLogQueue)));

        HasofferThreadFactory factory = new HasofferThreadFactory("SearchRecordProcessWorker-Thread");
        es = Executors.newCachedThreadPool(factory);
        for (int i = 0; i < 6; i++) {
            es.execute(new SearchRecordProcessWorker(searchProductService, fetchDubboService, searchLogQueue));
        }

        HasofferThreadFactory resultFactory = new HasofferThreadFactory("SearchRecordResultWorker-Thread");
        es = Executors.newCachedThreadPool(resultFactory);
        for (int i = 0; i < 5; i++) {
            es.execute(new SearchRecordResultWorker(searchProductService, fetchDubboService));
        }


        while (true) {
            try {
                TimeUnit.MINUTES.sleep(30);
            } catch (Exception e) {
                logger.error("Error Msg: ", e);
            }
        }
    }
}
