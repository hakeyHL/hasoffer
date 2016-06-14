package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.system.ITimerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Date : 2016/1/22
 * Function :
 */
@Component
public class StatPriceUpdateTask {

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    ITimerService timerService;

    @Scheduled(cron = "0 20 11 * * ?")
    public void f() {
        SysTimerTaskLog log = timerService.createTaskLog("MspJobFetchTask");

        String ymd = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_DAY, "yyyyMMdd");
        cmpSkuService.saveOrUpdateSkuPriceUpdateResult(cmpSkuService.countUpdate(ymd));

        timerService.updateTaskLog(log.getId(), "");
    }

}
