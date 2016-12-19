package hasoffer.core.app;

import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.persistence.po.ptm.PtmCategory;

import java.util.List;

/**
 * Created by hs on 2016年12月19日.
 * Time 17:25
 */
public interface AppCategoryService {
    List getCategorys(String categoryId);

    PtmCategory getCategoryById(Long cateId);

    List<CategoryVo> getTopCategoryList();
}
