package hasoffer.core.persistence.po.task.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.task.SkuTaskUpdateLog;

/**
 * Created on 2016/5/20.
 */
public class SkuTaskUpdateLogUpdater extends Updater<String, SkuTaskUpdateLog> {
    public SkuTaskUpdateLogUpdater(String id) {
        super(SkuTaskUpdateLog.class, id);
    }
}