package hasoffer.core.persistence.po.thd.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.thd.ThdFetchTask;

public class ThdFetchTaskUpdater extends Updater<Long, ThdFetchTask> {
    public ThdFetchTaskUpdater(Long aLong) {
        super(ThdFetchTask.class, aLong);
    }
}
