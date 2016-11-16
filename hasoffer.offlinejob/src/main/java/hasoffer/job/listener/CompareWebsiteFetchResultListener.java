package hasoffer.job.listener;

import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.core.product.impl.StdProductServiceImpl;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2016/8/16.
 */
public class CompareWebsiteFetchResultListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        serverInitialized();
    }

    private void serverInitialized() {

        IFetchDubboService fetchDubboService = springContext.getBean(IFetchDubboService.class);
        StdProductServiceImpl stdProductService = springContext.getBean(StdProductServiceImpl.class);

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(DaemonThreadFactory.create(new CompareWebsiteFetchResultWorker(fetchDubboService, stdProductService)));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}
