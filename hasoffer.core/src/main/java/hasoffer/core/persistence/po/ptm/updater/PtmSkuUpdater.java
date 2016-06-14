package hasoffer.core.persistence.po.ptm.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.ptm.PtmSku;

/**
 * Created by glx on 2015/10/13.
 */
public class PtmSkuUpdater extends Updater<Long, PtmSku> {
	public PtmSkuUpdater(Long aLong) {
		super(PtmSku.class, aLong);
	}
}
