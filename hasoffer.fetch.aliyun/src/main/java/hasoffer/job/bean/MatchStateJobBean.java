package hasoffer.job.bean;

import hasoffer.aliyun.enums.TaskStatus;
import hasoffer.aliyun.enums.WebSite;
import hasoffer.base.utils.TimeUtils;
import hasoffer.data.redis.IRedisService;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.state.dmo.MatchStateDMO;
import hasoffer.state.service.MatchStateService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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
        for (WebSite website : WebSite.values()) {
            MatchStateDMO matchStateDMO = new MatchStateDMO();
            int pushNum = 0;
            int finishNum = 0;
            int exceptionNum = 0;
            String ymd = DateFormatUtils.format(new Date(), "yyyyMMdd");

            String pushKey = RedisKeysUtils.MATCH_PUSH_NUM + "_" + website.name() + "_" + ymd;
            long cacheSeconds = TimeUtils.SECONDS_OF_1_DAY * 7;
            String pushNumStr = redisService.get(pushKey, cacheSeconds);
            logger.info(pushKey + " value: {}", pushNumStr);
            pushNum = Integer.valueOf(pushNumStr == null ? "0" : pushNumStr) + pushNum;

            String finishKey = RedisKeysUtils.MATCH_POP_NUM + "_" + website.name() + "_" + TaskStatus.FINISH + "_" + ymd;
            String finishNumStr = redisService.get(finishKey, cacheSeconds);
            logger.info(finishKey + " value: {}", finishNumStr);
            finishNum = Integer.valueOf(finishNumStr == null ? "0" : finishNumStr) + finishNum;

            String exceptionKey = RedisKeysUtils.MATCH_POP_NUM + "_" + website.name() + "_" + TaskStatus.EXCEPTION + "_" + ymd;
            String exceptionNumStr = redisService.get(exceptionKey, cacheSeconds);
            logger.info(exceptionKey + " value: {}", exceptionNumStr);
            exceptionNum = Integer.valueOf(exceptionNumStr == null ? "0" : exceptionNumStr) + exceptionNum;

            String updateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            matchStateDMO.setWebSite(website.name());
            matchStateDMO.setPushNum(pushNum);
            matchStateDMO.setFinishNum(finishNum);
            matchStateDMO.setExceptionNum(exceptionNum);
            matchStateDMO.setUpdateDate(updateStr);
            matchStateDMO.setLogTime(new Date());

            List<MatchStateDMO> matchStateDMOs = matchStateService.selectByDate(updateStr);
            if (matchStateDMOs.size() == 0) {
                matchStateService.insert(matchStateDMO);
            } else {
                matchStateService.update(matchStateDMO);
            }
        }
    }
}
