package hasoffer.core.app;

import hasoffer.base.model.PageableResult;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.solr.PtmStdSkuModel;

/**
 * Created by hs on 2016年12月12日.
 * Time 14:48
 */
public interface AppSearchService {
    PageableResult<PtmStdSkuModel> filterByParams(SearchCriteria searchCriteria);
}
