package hasoffer.core.app.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.app.AppSearchService;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hs on 2016年12月12日.
 * Time 14:48
 */
@Service
public class AppSearchServiceImpl implements AppSearchService {
    @Autowired
    PtmStdSkuIndexServiceImpl ptmStdSkuIndexService;

    @Override
    public PageableResult<PtmStdSkuModel> filterByParams(SearchCriteria searchCriteria) {
        PageableResult<PtmStdSkuModel> pageableResult = ptmStdSkuIndexService.filterStdSkuOnCategoryByCriteria(searchCriteria);
        return pageableResult;
    }
}
