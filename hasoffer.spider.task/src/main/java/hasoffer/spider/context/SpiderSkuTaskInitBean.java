package hasoffer.spider.context;

import hasoffer.base.utils.TimeUtils;
import hasoffer.spider.task.service.SpiderSkuTaskService;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class SpiderSkuTaskInitBean {
    private final Logger logger = LoggerFactory.getLogger(SpiderSkuTaskInitBean.class);

    public void runTask() {
        Timer timer = new Timer();
        timer.schedule(new SpiderSkuTimerTask(), 10000, TimeUtils.MILLISECONDS_OF_1_DAY);
        logger.debug("SpiderSkuTaskInitBean.queryProduct() be call.");
    }

    private static class SpiderSkuTimerTask extends TimerTask {
        @Override
        public void run() {
            SpiderSkuTaskService taskInitContext = SpringContextHolder.getBean(SpiderSkuTaskService.class);
            taskInitContext.initTask();
        }
    }
}
