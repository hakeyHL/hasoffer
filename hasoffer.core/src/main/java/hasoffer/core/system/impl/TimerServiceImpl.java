package hasoffer.core.system.impl;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.persistence.po.sys.updater.SysTimerTaskLogUpdater;
import hasoffer.core.system.ITimerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Date : 2016/3/4
 * Function :
 */
@Service
public class TimerServiceImpl implements ITimerService {

    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskLog(long id, String result) {
        SysTimerTaskLogUpdater updater = new SysTimerTaskLogUpdater(id);
        updater.getPo().setResult(result);
        updater.getPo().setEndTime(TimeUtils.nowDate());
        dbm.update(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysTimerTaskLog createTaskLog(String name) {

        SysTimerTaskLog sysTimerTaskLog = new SysTimerTaskLog(name);

        dbm.create(sysTimerTaskLog);

        return sysTimerTaskLog;
    }
}
