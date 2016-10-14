package hasoffer.spider.context;

import hasoffer.base.utils.TimeUtils;
import hasoffer.spider.task.service.SpiderProductTaskService;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class SpiderProductTaskInitBean {
    private final Logger logger = LoggerFactory.getLogger(SpiderProductTaskInitBean.class);

    public void runTask() {
        Timer timer = new Timer();
        timer.schedule(new SpiderProductTimerTask(), 10000, TimeUtils.MILLISECONDS_OF_1_MINUTE);
        logger.debug("SpiderProductTaskInitBean.queryProduct() be call.");
    }

    private static class SpiderProductTimerTask extends TimerTask {

        private final Logger logger = LoggerFactory.getLogger(SpiderProductTaskInitBean.class);

        @Override
        public void run() {
            logger.info("SpiderProductTimerTask.run() be call.");
            SpiderProductTaskService taskInitContext = SpringContextHolder.getBean(SpiderProductTaskService.class);
            taskInitContext.initTask();
        }
    }
}
