package hasoffer.state.service;

import hasoffer.data.redis.IRedisService;
import hasoffer.state.dao.UpdateStateDAO;
import hasoffer.state.dmo.UpdateStateDMO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UpdateStateService {

    @Resource
    private IRedisService<String> redisService;

    @Resource
    private UpdateStateDAO updateStateDao;

    public void stateTask() {
        for (TaskTarget taskTarget : TaskTarget.values()) {
            UpdateStateDMO updateStateDMO = new UpdateStateDMO();
            int pushNum = 0;
            int finishNum = 0;
            int exceptionNum = 0;
            int stopNum = 0;
            String ymd = DateFormatUtils.format(new Date(), "yyyyMMdd");
            for (WebSite website : WebSite.values()) {
                for (TaskLevel taskLevel : TaskLevel.values()) {
                    String key = "SPIDER_PUSH_NUM_" + taskTarget.name() + "_" + website.name() + "_" + taskLevel.name() + "_" + ymd;
                    pushNum = Integer.valueOf(redisService.get(key, 1000)) + pushNum;
                }
                String finishKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.FINISH + "_" + ymd;
                finishNum = Integer.valueOf(redisService.get(finishKey, 1000)) + finishNum;

                String exceptionKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.EXCEPTION + "_" + ymd;
                exceptionNum = Integer.valueOf(redisService.get(exceptionKey, 1000)) + exceptionNum;

                String stopKey = "SPIDER_POP_NUM_" + taskTarget.name() + "_" + website.name() + "_" + TaskStatus.STOP + "_" + ymd;
                stopNum = Integer.valueOf(redisService.get(stopKey, 1000)) + stopNum;
            }
            String updateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            updateStateDMO.setTaskTarget(taskTarget.name());
            updateStateDMO.setExceptionNum(exceptionNum);
            updateStateDMO.setFinishNum(finishNum);
            updateStateDMO.setStopNum(stopNum);
            updateStateDMO.setUpdateDate(updateStr);
            updateStateDMO.setLogTime(new Date());
            List<UpdateStateDMO> updateStateDMOs = updateStateDao.selectByTaskTargetDate(taskTarget.name(), updateStr);
            if (updateStateDMOs.size() == 0) {
                updateStateDao.insert(updateStateDMO);
            } else {
                updateStateDao.update(updateStateDMO);
            }

        }
    }

    public List<UpdateStateDMO> selectByDate(String updateStr) {
        return updateStateDao.selectByDate(updateStr);
    }

    enum TaskTarget {
        DEAL_UPDATE, SKU_UPDATE, WAIT_URL_LIST, STDPRICE_UPDATE
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
