package hasoffer.timer.client;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.user.IDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

/**
 * Date : 2016/1/13
 * Function :
 */
//@Component
public class ClientVisitStatTask {

    @Resource
    IDeviceService deviceService;

    private Logger logger = LoggerFactory.getLogger(ClientVisitStatTask.class);

    @Scheduled(cron = "0 20 0 * * ?")
    public void f() {

        String ymd = TimeUtils.parse(TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_DAY, "yyyyMMdd");
        deviceService.statDayVisit(ymd);
    }

}
