package hasoffer.state.service;

import hasoffer.aliyun.enums.TaskLevel;
import hasoffer.aliyun.enums.TaskStatus;
import hasoffer.aliyun.enums.TaskTarget;
import hasoffer.aliyun.enums.WebSite;
import hasoffer.base.utils.TimeUtils;
import hasoffer.data.redis.IRedisService;
import hasoffer.state.dao.UpdateStateDAO;
import hasoffer.state.dmo.UpdateStateDMO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class UpdateStateService {

    private Logger logger = LoggerFactory.getLogger(UpdateStateService.class);


    @Resource
    private IRedisService<String> redisService;

    @Resource
    private UpdateStateDAO updateStateDao;

    public void insert(UpdateStateDMO updateStateDMO) {
        updateStateDao.insert(updateStateDMO);
    }

    public void update(UpdateStateDMO updateStateDMO) {
        updateStateDao.update(updateStateDMO);
    }

    public void cache2Db() {
        for (TaskTarget taskTarget : TaskTarget.values()) {
            for (WebSite website : WebSite.values()) {
                UpdateStateDMO updateStateDMO = new UpdateStateDMO();
                int pushNum = 0;
                int finishNum = 0;
                int exceptionNum = 0;
                int stopNum = 0;
                String ymd = DateFormatUtils.format(new Date(), "yyyyMMdd");
                long cacheSeconds = TimeUtils.SECONDS_OF_1_DAY * 7;
                for (TaskLevel taskLevel : TaskLevel.values()) {
                    String key = "SPIDER_PUSH_NUM_" + taskTarget.name() + "_" + website.name() + "_" + taskLevel.name() + "_" + ymd;
                    String s = redisService.get(key, cacheSeconds);
                    pushNum = Integer.valueOf(s == null ? "0" : s) + pushNum;
                    //logger.info(key + " value: {}", s);
                }

                String finishKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.FINISH + "_" + ymd;
                String finishNumStr = redisService.get(finishKey, cacheSeconds);
                //logger.info(finishKey + " value: {}", finishNumStr);
                finishNum = Integer.valueOf(finishNumStr == null ? "0" : finishNumStr) + finishNum;

                String exceptionKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.EXCEPTION + "_" + ymd;
                String exceptionNumStr = redisService.get(exceptionKey, cacheSeconds);
                //logger.info(exceptionKey + " value: {}", exceptionNumStr);
                exceptionNum = Integer.valueOf(exceptionNumStr == null ? "0" : exceptionNumStr) + exceptionNum;

                String stopKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.STOP + "_" + ymd;
                String stopNumStr = redisService.get(stopKey, cacheSeconds);
                //logger.info(stopKey + " value: {}", stopNumStr);
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
                List<UpdateStateDMO> updateStateDMOs = selectByTaskTargetDate(updateStr, taskTarget.name(), website.name());
                if (updateStateDMOs.size() == 0) {
                    insert(updateStateDMO);
                } else {
                    update(updateStateDMO);
                }
            }

        }
    }

    public List<UpdateStateDMO> selectByDate(String updateStr) {
        return updateStateDao.selectByDate(updateStr);
    }

    public List<UpdateStateDMO> selectStats(String queryDay, String taskTarget, String webSite) {
        return updateStateDao.selectStats(queryDay, taskTarget, webSite);
    }


    public List<UpdateStateDMO> selectByTaskTargetDate(String updateStr, String taskTarget, String webSite) {
        return updateStateDao.selectByTaskTargetDate(updateStr, taskTarget, webSite);
    }

    public List<TaskTarget> selectTaskTarget() {
        return Arrays.asList(TaskTarget.values());
    }

    public List<WebSite> selectWebSite() {
        return Arrays.asList(WebSite.values());
    }

}
