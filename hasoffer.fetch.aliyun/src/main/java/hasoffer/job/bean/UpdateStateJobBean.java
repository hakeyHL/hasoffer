package hasoffer.job.bean;

import hasoffer.aliyun.enums.TaskLevel;
import hasoffer.aliyun.enums.TaskStatus;
import hasoffer.aliyun.enums.TaskTarget;
import hasoffer.aliyun.enums.WebSite;
import hasoffer.data.redis.IRedisService;
import hasoffer.state.dmo.UpdateStateDMO;
import hasoffer.state.service.UpdateStateService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class UpdateStateJobBean extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(EipJobBean.class);

    @Resource
    private UpdateStateService updateStateService;

    @Resource
    private IRedisService<String> redisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("UpdateStateJobBean.updateState start.");
        stateTask();
        logger.info("UpdateStateJobBean.updateState end.");
    }

    private void stateTask() {
        for (TaskTarget taskTarget : TaskTarget.values()) {
            for (WebSite website : WebSite.values()) {
                UpdateStateDMO updateStateDMO = new UpdateStateDMO();
                int pushNum = 0;
                int finishNum = 0;
                int exceptionNum = 0;
                int stopNum = 0;
                String ymd = DateFormatUtils.format(new Date(), "yyyyMMdd");
                for (TaskLevel taskLevel : TaskLevel.values()) {
                    String key = "SPIDER_PUSH_NUM_" + taskTarget.name() + "_" + website.name() + "_" + taskLevel.name() + "_" + ymd;
                    String s = redisService.get(key, -1);
                    pushNum = Integer.valueOf(s == null ? "0" : s) + pushNum;
                    logger.info(key + " value: {}", s);
                }
                String finishKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.FINISH + "_" + ymd;
                String finishNumStr = redisService.get(finishKey, -1);
                logger.info(finishKey + " value: {}", finishNumStr);
                finishNum = Integer.valueOf(finishNumStr == null ? "0" : finishNumStr) + finishNum;

                String exceptionKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.EXCEPTION + "_" + ymd;
                String exceptionNumStr = redisService.get(exceptionKey, -1);
                logger.info(exceptionKey + " value: {}", exceptionNumStr);
                exceptionNum = Integer.valueOf(exceptionNumStr == null ? "0" : exceptionNumStr) + exceptionNum;

                String stopKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.STOP + "_" + ymd;
                String stopNumStr = redisService.get(stopKey, -1);
                logger.info(stopKey + " value: {}", stopNumStr);
                stopNum = Integer.valueOf(stopNumStr == null ? "0" : stopNumStr) + stopNum;
                String updateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
                updateStateDMO.setTaskTarget(taskTarget.name());
                updateStateDMO.setWebSite(website.name());
                updateStateDMO.setPushNum(pushNum);
                updateStateDMO.setExceptionNum(exceptionNum);
                updateStateDMO.setFinishNum(finishNum);
                updateStateDMO.setStopNum(stopNum);
                updateStateDMO.setUpdateDate(updateStr);
                updateStateDMO.setLogTime(new Date());
                List<UpdateStateDMO> updateStateDMOs = updateStateService.selectByTaskTargetDate(updateStr, taskTarget.name(), website.name());
                if (updateStateDMOs.size() == 0) {
                    updateStateService.insert(updateStateDMO);
                } else {
                    updateStateService.update(updateStateDMO);
                }
            }

        }
    }
}
