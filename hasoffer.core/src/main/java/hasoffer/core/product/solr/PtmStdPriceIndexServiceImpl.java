package hasoffer.core.product.solr;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageableResult;
import hasoffer.core.app.vo.SearchIO;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.data.solr.*;
import jodd.util.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年12月01日.
 * Time 09:57
 */
@Service
public class PtmStdPriceIndexServiceImpl extends AbstractIndexService<Long, PtmStdPriceModel> {

    @Override
    protected String getSolrUrl() {
        return AppConfig.get(AppConfig.SOLR_PTMSTDPRICE_URL);
    }

    public PageableResult<PtmStdPriceModel> searchPrices(SearchIO searchIO, int page, int size) {
        String q;
        if (searchIO != null && StringUtils.isNotEmpty(searchIO.getCliQ())) {
            q = searchIO.getCliQ();
        } else {
            return null;
        }
        if (StringUtils.isEmpty(q)) {
            q = "*:*";
        } else {
            q = "title:" + q;
            if (StringUtils.isNotEmpty(searchIO.getCliSite().name())) {
                q = q + " AND site:" + searchIO.getCliSite().name();
            }
        }
        FilterQuery[] fqs = null;
        Sort[] sorts = null;
        PivotFacet[] pivotFacets = null;
        SearchResult<PtmStdPriceModel> sr = searchObjs(q, fqs, sorts, pivotFacets, page, size, true);
        return new PageableResult<>(sr.getResult(), sr.getTotalCount(), page, size);
    }

    public PageableResult<PtmStdPriceModel> filterStdSkuOnCategoryByCriteria(SearchCriteria searchCriteria) {
        String queryString = searchCriteria.getKeyword();
        if (org.apache.commons.lang3.StringUtils.isEmpty(queryString)) {
            queryString = "*:*";
        }
        Sort[] sorts = new Sort[1];
        SearchResultSort resultSort = searchCriteria.getSort();
        List<FilterQuery> fqList = new ArrayList<>();
        //处理 facet
        List<String> pivotFields = searchCriteria.getPivotFields();
        int pivotFieldSize = pivotFields == null ? 0 : pivotFields.size();
        PivotFacet[] pivotFacets = new PivotFacet[pivotFieldSize];
        if (pivotFieldSize > 0) {
            for (int i = 0; i < pivotFieldSize; i++) {
                // cate2 distinct 提取出来所有值
                pivotFacets[i] = new PivotFacet(pivotFields.get(i));
            }
        }
        int page = searchCriteria.getPage();
        int size = searchCriteria.getPageSize();
        int priceFrom = searchCriteria.getPriceFrom(), priceTo = searchCriteria.getPriceTo();
        String priceFromStr = "*", priceToStr = "*";
        ApiUtils.setPriceSearchScope(fqList, priceFrom, priceTo, priceToStr);
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);
        SearchResult<PtmStdPriceModel> sr = searchObjs(queryString, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);
        //缓存以及从缓存中取
        Map<String, List<NameValue>> pivotFieldVals = new HashMap<>();
        PageableResult<PtmStdPriceModel> ptmStdPriceModelPageableResult = new PageableResult<>(sr.getResult(), sr.getTotalCount(), page, size, pivotFieldVals);
        return ptmStdPriceModelPageableResult;
    }
}
