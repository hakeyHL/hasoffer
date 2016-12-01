package hasoffer.dubbo.worker.listener;

import hasoffer.base.model.Website;
import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.dubbo.worker.fetch.FetchCompareWebsiteTaskWorker;
import hasoffer.dubbo.worker.fetch.FetchDealWorker;
import hasoffer.dubbo.worker.fetch.FetchKeywordWorker;
import hasoffer.dubbo.worker.fetch.FetchUrlWorker;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FetchListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        initUrlThread();
        initKeyWordThread();
        initDealFetchThread();
        initCompareWebsiteFetchThread();
    }

    private void initCompareWebsiteFetchThread() {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchCompareWebsiteWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        for (int i = 0; i < 1; i++) {
            es.execute(new FetchCompareWebsiteTaskWorker(springContext, Website.MOBILE91));
        }
    }

    private void initDealFetchThread() {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchDealWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        for (int i = 0; i < 1; i++) {
            es.execute(new FetchDealWorker(springContext, Website.DESIDIME));
        }
    }

    private void initUrlThread() {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchUrlWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);

        for (int i = 0; i < 5; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.AMAZON));
        }

        for (int i = 0; i < 20; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.FLIPKART));
        }

        for (int i = 0; i < 6; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.SNAPDEAL));
        }

        for (int i = 0; i < 2; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.EBAY));
        }

        for (int i = 0; i < 5; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.SHOPCLUES));
        }

        for (int i = 0; i < 1; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.PAYTM));
        }

        for (int i = 0; i < 1; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.MYNTRA));
        }

        for (int i = 0; i < 1; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.INFIBEAM));
        }

        for (int i = 0; i < 1; i++) {
            es.execute(new FetchUrlWorker(springContext, Website.JABONG));
        }
    }

    private void initKeyWordThread() {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchKeywordWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        for (int i = 0; i < 20; i++) {
            es.execute(new FetchKeywordWorker(springContext));
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}