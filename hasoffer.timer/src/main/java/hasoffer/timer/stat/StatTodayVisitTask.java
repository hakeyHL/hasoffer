package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.user.DayVisitBo;
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
public class StatTodayVisitTask {

    @Resource
    IDeviceService deviceService;
    @Resource
    ITimerService timerService;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void f() {
        SysTimerTaskLog log = timerService.createTaskLog("StatTodayVisitTask");

        String ymd = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");

        DayVisitBo dayVisitBo = deviceService.statDayVisit(ymd);
        deviceService.saveOrUpdate(dayVisitBo);

        timerService.updateTaskLog(log.getId(), "");
    }

}
