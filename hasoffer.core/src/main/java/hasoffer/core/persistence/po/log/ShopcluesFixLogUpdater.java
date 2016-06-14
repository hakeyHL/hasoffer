package hasoffer.core.persistence.po.log;

import hasoffer.core.persistence.dbm.osql.Updater;

/**
 * Created on 2016/4/22.
 */
public class ShopcluesFixLogUpdater extends Updater<Long, ShopcluesFixLog> {

    public ShopcluesFixLogUpdater(Long aLong) {
        super(ShopcluesFixLog.class, aLong);
    }

}
