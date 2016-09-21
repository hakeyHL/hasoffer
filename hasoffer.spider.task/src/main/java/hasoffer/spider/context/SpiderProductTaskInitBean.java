package hasoffer.spider.context;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.spider.thread.SearchRecordListWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpiderProductTaskInitBean {
    private Logger logger = LoggerFactory.getLogger(SpiderProductTaskInitBean.class);

    @Resource
    ISearchService searchService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    SearchProductService searchProductService;

    public void queryProduct() {
        logger.debug("queryProduct()");
        ExecutorService es = Executors.newCachedThreadPool();

        //LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue = new LinkedBlockingQueue<SrmAutoSearchResult>();

        es.execute(new SearchRecordListWorker(searchProductService, dbm));

        //String threadName = "SearchRecordProcessWorker-Thread";
        //HasofferThreadFactory factory = new HasofferThreadFactory(threadName);
        //es = Executors.newCachedThreadPool(factory);
        //for (int i = 0; i < 10; i++) {
        //    es.execute(new SearchRecordProcessWorker(searchProductService, fetchDubboService, searchLogQueue));
        //}

    }
}
