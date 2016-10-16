package hasoffer.api.listener;

import hasoffer.api.worker.DeviceRequestSaveWorker;
import hasoffer.api.worker.UrmSignAlertWorker;
import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.system.IAppService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.user.impl.DeviceServiceImpl;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApplicationListener extends ContextLoaderListener {

    //获取spring注入的bean对象
    private WebApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        serverInitialized();
    }

    private void serverInitialized() {
        ExecutorService es = Executors.newCachedThreadPool();

//        ISearchService searchService = springContext.getBean(SearchServiceImpl.class);
//        es.execute(DaemonThreadFactory.create(new SearchLogSaveWorker(searchService)));
//
        IDeviceService deviceService = springContext.getBean(DeviceServiceImpl.class);
        es.execute(DaemonThreadFactory.create(new DeviceRequestSaveWorker(deviceService)));

        //check if user had signed yesterday by until today 22:00:00 hadn't sign.
        //we will push message to alert them to sign in our app and get hasoffer coin.
        Date currentDate = new Date();
        long indiaTime = TimeUtils.getIndiaTime(currentDate.getTime());
        long pushTime = TimeUtils.today(0, 30, 0);
        pushTime = TimeUtils.getIndiaTime(pushTime);

        IAppService appService = springContext.getBean(AppServiceImpl.class);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new UrmSignAlertWorker(appService), pushTime - indiaTime, 1000 * 60 * 60 * 24, TimeUnit.SECONDS);

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
