package hasoffer.core.persistence.po.ptm.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;

/**
 * Created on 2016/11/30.
 */
public class PtmStdPriceUpdater extends Updater<Long, PtmStdPrice> {
    public PtmStdPriceUpdater(Long aLong) {
        super(PtmStdPrice.class, aLong);
    }
}
