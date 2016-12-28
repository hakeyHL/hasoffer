package hasoffer.job.bean;

import hasoffer.data.redis.IRedisService;
import hasoffer.state.service.MatchStateService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

public class MatchStateJobBean extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(EipJobBean.class);

    @Resource
    private MatchStateService matchStateService;

    @Resource
    private IRedisService<String> redisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("UpdateStateJobBean.updateState start.");
        stateTask();
        logger.info("UpdateStateJobBean.updateState end.");
    }

    private void stateTask() {
        //for (WebSite website : WebSite.values()) {
        //    MatchStateDMO updateStateDMO = new MatchStateDMO();
        //    int pushNum = 0;
        //    int finishNum = 0;
        //    int exceptionNum = 0;
        //    String ymd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        //
        //    String finishKey = "MATCH_POP_NUM_" + website.name() + "_" + UpdateStateService.TaskStatus.FINISH + "_" + ymd;
        //    String finishNumStr = redisService.get(finishKey, -1);
        //    logger.info(finishKey + " value: {}", finishNumStr);
        //    finishNum = Integer.valueOf(finishNumStr == null ? "0" : finishNumStr) + finishNum;
        //
        //    String exceptionKey = "MATCH_POP_NUM_" + website.name() + "_" + UpdateStateService.TaskStatus.EXCEPTION + "_" + ymd;
        //    String exceptionNumStr = redisService.get(exceptionKey, -1);
        //    logger.info(exceptionKey + " value: {}", exceptionNumStr);
        //    exceptionNum = Integer.valueOf(exceptionNumStr == null ? "0" : exceptionNumStr) + exceptionNum;
        //
        //
        //    String updateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        //    updateStateDMO.setWebSite(website.name());
        //    updateStateDMO.setPushNum(pushNum);
        //    updateStateDMO.setExceptionNum(exceptionNum);
        //    updateStateDMO.setFinishNum(finishNum);
        //    updateStateDMO.setUpdateDate(updateStr);
        //    updateStateDMO.setLogTime(new Date());
        //    List<MatchStateDMO> updateStateDMOs = matchStateService.selectByDate(updateStr);
        //    if (updateStateDMOs.size() == 0) {
        //        matchStateService.insert(updateStateDMO);
        //    } else {
        //        matchStateService.update(updateStateDMO);
        //    }
        //}
    }
}
