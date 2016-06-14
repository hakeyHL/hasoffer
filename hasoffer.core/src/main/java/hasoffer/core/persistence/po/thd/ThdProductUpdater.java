package hasoffer.core.persistence.po.thd;

import hasoffer.core.persistence.dbm.osql.Updater;

/**
 * Date : 2016/2/26
 * Function :
 */
public class ThdProductUpdater extends Updater<Long, ThdProduct> {
    public ThdProductUpdater(Long aLong, Class<ThdProduct> thdClass) {
        super(thdClass, aLong);
    }
}
