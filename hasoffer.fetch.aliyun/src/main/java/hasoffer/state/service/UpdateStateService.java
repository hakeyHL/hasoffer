package hasoffer.state.service;

import hasoffer.aliyun.enums.WebSite;
import hasoffer.state.dao.UpdateStateDAO;
import hasoffer.state.dmo.UpdateStateDMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
public class UpdateStateService {

    private Logger logger = LoggerFactory.getLogger(UpdateStateService.class);

    @Resource
    private UpdateStateDAO updateStateDao;

    public void insert(UpdateStateDMO updateStateDMO) {
        updateStateDao.insert(updateStateDMO);
    }

    public void update(UpdateStateDMO updateStateDMO) {
        updateStateDao.update(updateStateDMO);
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

    public enum TaskTarget {
        DEAL_UPDATE, SKU_UPDATE, WAIT_URL_LIST, STDPRICE_UPDATE, PRICEOFF_NOTICE
    }


    public enum TaskLevel {
        LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5
    }


    public enum TaskStatus {
        FINISH, EXCEPTION, STOP
    }
}
