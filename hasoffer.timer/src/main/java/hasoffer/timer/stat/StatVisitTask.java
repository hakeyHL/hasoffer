package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.system.ITimerService;
import hasoffer.core.user.IDeviceService;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

/**
 * Date : 2016/1/22
 * Function :
 */
//@Component
public class StatVisitTask {

    @Resource
    IDeviceService deviceService;
    @Resource
    ITimerService timerService;

    @Scheduled(cron = "0 20 0 * * ?")
    public void f() {
        SysTimerTaskLog log = timerService.createTaskLog("StatVisitTask");

        String ymd = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_HOUR * 12, TimeUtils.PATTERN_YMD);
        deviceService.saveOrUpdate(deviceService.statDayVisit(ymd));

        timerService.updateTaskLog(log.getId(), "");
    }

}
