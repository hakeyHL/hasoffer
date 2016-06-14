package hasoffer.core.thd.snapdeal;


import hasoffer.core.persistence.po.thd.snapdeal.ThdACategory;
import hasoffer.core.persistence.po.thd.snapdeal.ThdAProduct;

import java.util.List;


public interface ISnapdealService {
    ThdACategory createCategory(ThdACategory category);

    List<ThdACategory> getCate2s();

    List<ThdACategory> getCate2sAll();

    void updateSouceId(long id, long souceId);

    ThdAProduct createProduct(ThdAProduct productJob);
}
