package hasoffer.spider.context;

import hasoffer.base.utils.TimeUtils;
import hasoffer.spider.task.service.SpiderSkuTaskService;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpiderSkuTaskInitBean {
    private final Logger logger = LoggerFactory.getLogger(SpiderSkuTaskInitBean.class);

    public void runTask() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        //Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.HOUR_OF_DAY, 12);
        //calendar.set(Calendar.MINUTE, 10);
        //calendar.set(Calendar.SECOND, 0);
        long runTime = new Date().getTime() + 5000;
        long nowTime = new Date().getTime();
        long delay;
        if (runTime < nowTime) {
            delay = runTime - nowTime + TimeUtils.MILLISECONDS_OF_1_DAY;
        } else {
            delay = runTime - nowTime;
        }
        logger.debug("Current Date:{}, delay {} ms  SpiderSkuTaskInitBean.queryProduct() be call.");
        // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
        service.scheduleAtFixedRate(new SpiderSkuTimerTask(), delay, TimeUtils.MILLISECONDS_OF_1_DAY, TimeUnit.MILLISECONDS);
    }

    private static class SpiderSkuTimerTask extends TimerTask {
        @Override
        public void run() {
            SpiderSkuTaskService taskInitContext = SpringContextHolder.getBean(SpiderSkuTaskService.class);
            String dateStr = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_DAY, "yyyyMMdd");
            taskInitContext.initAmazonTask(dateStr, "5");
        }
    }
}
