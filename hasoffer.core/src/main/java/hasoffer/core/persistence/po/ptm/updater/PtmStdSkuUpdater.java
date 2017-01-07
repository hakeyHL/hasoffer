package hasoffer.core.persistence.po.ptm.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.ptm.PtmStdSku;

/**
 * Created by hs on 2017年01月06日.
 * Time 17:17
 */
public class PtmStdSkuUpdater extends Updater<Long, PtmStdSku> {
    public PtmStdSkuUpdater(Long aLong) {
        super(PtmStdSku.class, aLong);
    }
}
