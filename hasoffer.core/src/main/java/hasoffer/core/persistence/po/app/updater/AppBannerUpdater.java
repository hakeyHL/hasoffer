package hasoffer.core.persistence.po.app.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.app.AppBanner;

/**
 * Created on 2016/9/18.
 */
public class AppBannerUpdater extends Updater<Long, AppBanner> {
    public AppBannerUpdater(Long aLong) {
        super(AppBanner.class, aLong);
    }
}
