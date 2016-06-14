package hasoffer.core.persistence.po.ptm.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.ptm.PtmBrand;

/**
 * Created by glx on 2015/10/13.
 */
public class PtmBrandUpdater extends Updater<Long, PtmBrand> {
	public PtmBrandUpdater(Long aLong) {
		super(PtmBrand.class, aLong);
	}
}
