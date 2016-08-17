package hasoffer.job.listener;

import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2016/8/16.
 */
public class PtmProductPriceUpdateListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        serverInitialized();
    }

    private void serverInitialized() {

        Hibernate4DataBaseManager dbm = springContext.getBean(Hibernate4DataBaseManager.class);

        ExecutorService es = Executors.newCachedThreadPool();

//        es.execute(DaemonThreadFactory.create(new PtmProductPriceUpdateWorker(dbm)));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}
