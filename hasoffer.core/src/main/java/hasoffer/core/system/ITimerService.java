package hasoffer.core.system;

import hasoffer.core.persistence.po.sys.SysTimerTaskLog;

/**
 * Date : 2016/3/4
 * Function :
 */
public interface ITimerService {

    SysTimerTaskLog createTaskLog(String name);

    void updateTaskLog(long id, String result);

}
