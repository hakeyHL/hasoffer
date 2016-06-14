package hasoffer.core.persistence.po.sys.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;

/**
 * Date : 2016/3/4
 * Function :
 */
public class SysTimerTaskLogUpdater extends Updater<Long, SysTimerTaskLog> {
    public SysTimerTaskLogUpdater(Long aLong) {
        super(SysTimerTaskLog.class, aLong);
    }
}
