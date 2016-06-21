package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.utils.DaemonThreadFactory;
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

        initThread();
    }

    private void initThread() {
        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(DaemonThreadFactory.create(new FetchKeywordWorker(springContext)));

        es.execute(DaemonThreadFactory.create(new FetchUrlWorker(springContext)));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}