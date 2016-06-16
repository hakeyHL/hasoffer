package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.dubbo.api.fetch.service.IFetchService;
import hasoffer.dubbo.api.fetch.service.IKeywordService;
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

        serverInitialized();
    }

    private void serverInitialized() {
        ExecutorService es = Executors.newCachedThreadPool();
        IFetchService fetchService = (IFetchService) springContext.getBean("flipkartFetchService");
        IKeywordService keywordService = (IKeywordService) springContext.getBean("keywordService");
        es.execute(DaemonThreadFactory.create(new FetchWorker(keywordService,fetchService)));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}