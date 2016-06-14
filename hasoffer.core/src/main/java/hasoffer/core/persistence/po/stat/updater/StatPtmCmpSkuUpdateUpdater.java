package hasoffer.core.persistence.po.stat.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.stat.StatPtmCmpSkuUpdate;

/**
 * Created on 2016/5/30.
 */
public class StatPtmCmpSkuUpdateUpdater extends Updater<String, StatPtmCmpSkuUpdate> {
    public StatPtmCmpSkuUpdateUpdater(String s) {
        super(StatPtmCmpSkuUpdate.class, s);
    }
}
