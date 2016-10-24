package hasoffer.job.listener;

import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.data.redis.IRedisListService;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2016/9/9.
 */
@Deprecated
public class CheckPriceOffGetDealListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        serverInitialized();
    }

    private void serverInitialized() {

        Hibernate4DataBaseManager dbm = springContext.getBean(Hibernate4DataBaseManager.class);
        MongoDbManager mdm = springContext.getBean(MongoDbManager.class);
        IDealService dealService = springContext.getBean(IDealService.class);
        IRedisListService redisListService = springContext.getBean(IRedisListService.class);

        ExecutorService es = Executors.newCachedThreadPool();

//        es.execute(DaemonThreadFactory.create(new CheckGetPriceOffDealWorker(mdm, dbm, dealService, redisListService)));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}