package hasoffer.core.app;

import hasoffer.core.app.vo.ProductListVo;
import hasoffer.core.bo.system.SearchCriteria;

import java.util.List;

/**
 * Created by hs on 2016年12月12日.
 * Time 14:48
 */
public interface AppSearchService {
    List<ProductListVo> filterByParams(SearchCriteria searchCriteria);
}
