package hasoffer.job.listener;

import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.core.admin.IDealService;
import hasoffer.core.admin.impl.DealServiceImpl;
import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.bean.deal.DealSiteGetDealWoker;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2016/8/16.
 */
public class DealSiteGetDealListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        serverInitialized();
    }

    private void serverInitialized() {

        Hibernate4DataBaseManager dbm = springContext.getBean(Hibernate4DataBaseManager.class);
        IFetchDubboService fetchDubboService = springContext.getBean(IFetchDubboService.class);
        IDealService dealService = springContext.getBean(DealServiceImpl.class);

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(DaemonThreadFactory.create(new DealSiteGetDealWoker(dbm, fetchDubboService, dealService)));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}
