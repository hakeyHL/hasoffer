package hasoffer.core.persistence.po.log.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.log.ShopcluesFixLog;

/**
 * Created on 2016/4/22.
 */
public class ShopcluesFixLogUpdater extends Updater<Long, ShopcluesFixLog> {

    public ShopcluesFixLogUpdater(Long aLong) {
        super(ShopcluesFixLog.class, aLong);
    }

}
