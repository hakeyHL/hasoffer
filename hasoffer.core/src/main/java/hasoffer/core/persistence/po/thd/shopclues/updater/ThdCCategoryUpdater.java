package hasoffer.core.persistence.po.thd.shopclues.updater;
import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.thd.shopclues.ThdCCategory;
import hasoffer.core.persistence.po.thd.snapdeal.ThdACategory;

public class ThdCCategoryUpdater extends Updater<Long, ThdCCategory> {
	public ThdCCategoryUpdater(Long aLong) {
		super(ThdCCategory.class, aLong);
	}
}
