package hasoffer.core.persistence.po.log.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.log.SkuUpdateLog;

/**
 * Created on 2016/6/12.
 */
public class SkuUpdateLogUpdater extends Updater<String, SkuUpdateLog> {

    public SkuUpdateLogUpdater(String id) {
        super(SkuUpdateLog.class, id);
    }
}