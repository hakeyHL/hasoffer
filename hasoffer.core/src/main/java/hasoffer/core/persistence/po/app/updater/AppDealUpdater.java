package hasoffer.core.persistence.po.app.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.app.AppDeal;

/**
 * Created on 2016/9/18.
 */
public class AppDealUpdater extends Updater<Long, AppDeal> {
    public AppDealUpdater(Long aLong) {
        super(AppDeal.class, aLong);
    }
}
