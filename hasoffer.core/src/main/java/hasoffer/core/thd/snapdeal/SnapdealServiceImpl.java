package hasoffer.core.thd.snapdeal;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.thd.snapdeal.ThdACategory;
import hasoffer.core.persistence.po.thd.snapdeal.ThdAProduct;
import hasoffer.core.persistence.po.thd.snapdeal.updater.ThdACategoryUpdater;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author:menghaiquan
 * Date:2016/1/14 2016/1/14
 */
//@Component
public class SnapdealServiceImpl implements ISnapdealService {
	private static String FIND_CATE2S = "select s from ThdACategory s where s.depth = 2 and s.sourceId = 0";
	private static String FIND_CATE2S_ALL = "select s from ThdACategory s where s.depth = 2";

	@Resource
	IDataBaseManager dbm;

	@Override
	public ThdACategory createCategory(ThdACategory category) {
		this.dbm.create(category);
		return category;
	}

	@Override
	public List<ThdACategory> getCate2s() {
		return this.dbm.query(FIND_CATE2S);
	}

	@Override
	public List<ThdACategory> getCate2sAll() {
		return this.dbm.query(FIND_CATE2S_ALL);
	}

	@Override
	public void updateSouceId(long id, long souceId) {
		ThdACategoryUpdater updater = new ThdACategoryUpdater(id);
		updater.getPo().setSourceId(souceId);
		this.dbm.update(updater);
	}

	@Override
	public ThdAProduct createProduct(ThdAProduct productJob) {
		this.dbm.create(productJob);
		return productJob;
	}


}
