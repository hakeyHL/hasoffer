package hasoffer.core.persistence.po.ptm.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.ptm.PtmStdImage;

public class PtmStdImageUpdater extends Updater<Long, PtmStdImage> {
    public PtmStdImageUpdater(Long aLong) {
        super(PtmStdImage.class, aLong);
    }
}
