package hasoffer.state.service;

import hasoffer.data.redis.IRedisService;
import hasoffer.state.dao.UpdateStateDAO;
import hasoffer.state.dmo.UpdateStateDMO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UpdateStateService {

    private Logger logger = LoggerFactory.getLogger(UpdateStateService.class);

    @Resource
    private IRedisService<String> redisService;

    @Resource
    private UpdateStateDAO updateStateDao;

    public void stateTask() {
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
                List<UpdateStateDMO> updateStateDMOs = updateStateDao.selectByTaskTargetDate(updateStr, taskTarget.name(), website.name());
                if (updateStateDMOs.size() == 0) {
                    updateStateDao.insert(updateStateDMO);
                } else {
                    updateStateDao.update(updateStateDMO);
                }
            }

        }
    }

    public List<UpdateStateDMO> selectByDate(String updateStr) {
        return updateStateDao.selectByDate(updateStr);
    }

    enum TaskTarget {
        DEAL_UPDATE, SKU_UPDATE, WAIT_URL_LIST, STDPRICE_UPDATE, PRICEOFF_NOTICE
    }


    enum TaskLevel {
        LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5
    }

    enum WebSite {
        SNAPDEAL, VOONIK, FLIPKART, AMAZON, PAYTM, INFIBEAM, JABONG, EBAY, SHOPCLUES
    }

    enum TaskStatus {
        FINISH, EXCEPTION, STOP
    }
}
