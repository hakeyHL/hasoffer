package hasoffer.core.app.impl;

import com.alibaba.fastjson.JSON;
import com.amazonaws.util.Md5Utils;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.app.AppSearchService;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.ApiUtils;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    AppCacheService appCacheService;

    @Override
    public PageableResult<PtmStdSkuModel> filterByParams(SearchCriteria searchCriteria) {
        PageableResult<PtmStdSkuModel> pageableResult = null;
        String key = ConstantUtil.API_SOLR_PTMSTDSKU_CATEGORY_SEARCH + "_" + Md5Utils.md5AsBase64(searchCriteria.toString().getBytes());
        String cacheValueByKey = appCacheService.getCacheValueByKey(key);
        if (StringUtils.isNotEmpty(cacheValueByKey)) {
            pageableResult = ApiUtils.parseString2Pageable(cacheValueByKey, PtmStdSkuModel.class);
        }
        if (pageableResult == null) {
            pageableResult = ptmStdSkuIndexService.filterStdSkuOnCategoryByCriteria(searchCriteria);
            appCacheService.addStringCache(key, JSON.toJSONString(pageableResult), TimeUtils.MILLISECONDS_OF_1_HOUR * 6);
        }
        return pageableResult;
    }
}
