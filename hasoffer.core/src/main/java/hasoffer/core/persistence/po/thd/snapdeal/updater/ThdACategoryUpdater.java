package hasoffer.core.persistence.po.thd.snapdeal.updater;
import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.thd.snapdeal.ThdACategory;

public class ThdACategoryUpdater extends Updater<Long, ThdACategory> {
	public ThdACategoryUpdater(Long aLong) {
		super(ThdACategory.class, aLong);
	}
}
