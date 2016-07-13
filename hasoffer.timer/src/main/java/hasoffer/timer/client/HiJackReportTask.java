package hasoffer.timer.client;

import hasoffer.core.admin.IHiJackReportService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created on 2016/3/30.
 */
//@Component
public class HiJackReportTask {

    private static Logger logger = LoggerFactory.getLogger(HiJackReportTask.class);

    @Resource
    IMongoDbManager mdm;
    @Resource
    IHiJackReportService hiJackReportService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void reportHiJack() {

        logger.debug("---------------------------------------------");
        long currentTimes = System.currentTimeMillis();
        long cycleTime = 1000 * 60 * 10;
        long cycleIndex = currentTimes / cycleTime;

        Date startTime = new Date(cycleIndex * cycleTime - cycleTime * 2);
        Date endTime = new Date(cycleIndex * cycleTime - cycleTime);
        logger.info("reportHiJack is called. beginTime:{}.",startTime);
        try {
            hiJackReportService.countHiJack(startTime, endTime);
        } catch (Exception e) {
            logger.debug("reportHiJack:任务失败,   DATE:" + new Date() + ":具体如下");
            logger.debug(e.toString());
        }

    }

}
