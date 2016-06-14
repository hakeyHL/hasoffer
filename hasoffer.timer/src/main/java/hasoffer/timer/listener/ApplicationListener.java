package hasoffer.timer.listener;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;

public class ApplicationListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());

        serverInitialized();
    }

    private void serverInitialized() {
//        ExecutorService es = Executors.newCachedThreadPool();

//        IDataBaseManager dbm = springContext.getBean(Hibernate4DataBaseManager.class);
//        ICmpSkuService cmpSkuService = springContext.getBean(CmpSkuServiceImpl.class);
//        IProductService productService = springContext.getBean(ProductServiceImpl.class);
//        ISearchService searchService = springContext.getBean(SearchServiceImpl.class);
//        IFetchService fetchService = springContext.getBean(FetchServiceImpl.class);

        // cmp sku 更新
        /*ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();
        es.execute(DaemonThreadFactory.create(new CmpSkuListWorker(dbm, cmpSkuService, skuQueue)));
        for (int i = 0; i < 15; i++) {
            es.execute(DaemonThreadFactory.create(new CmpSkuUpdateWorker(skuQueue, cmpSkuService, fetchService)));
        }*/

        // UnmatchedSearchRecordProcessWorker
        /*LinkedBlockingQueue<SrmSearchLog> searchLogQueue = new LinkedBlockingQueue<SrmSearchLog>();
        es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordListWorker(productService, searchService, searchLogQueue)));
        for (int i = 0; i < 30; i++) {
            es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordProcessWorker(productService, searchService, searchLogQueue)));
        }*/
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
