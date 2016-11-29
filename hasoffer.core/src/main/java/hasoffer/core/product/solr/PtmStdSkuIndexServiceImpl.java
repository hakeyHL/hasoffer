package hasoffer.core.product.solr;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.data.solr.*;
import jodd.util.NameValue;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年11月28日.
 * Time 18:36
 */
@Service
public class PtmStdSkuIndexServiceImpl extends AbstractIndexService<Long, PtmStdSkuModel> {
    @Override
    protected String getSolrUrl() {
        return AppConfig.get(AppConfig.SOLR_PTMSTDSKU_URL);
    }

    public PageableResult<PtmStdSkuModel> searchProducts(SearchCriteria sc) {
        List<String> pivotFields = sc.getPivotFields();
        int pivotFieldSize = pivotFields == null ? 0 : pivotFields.size();

        Sort[] sorts = null;

        // sort by
        SearchResultSort resultSort = sc.getSort();
        if (resultSort != null) {
            if (resultSort == SearchResultSort.POPULARITY) {
                sorts = new Sort[2];
                sorts[0] = new Sort(ProductModel2SortField.F_POPULARITY.getFieldName(), Order.DESC);
                sorts[1] = new Sort("review", Order.DESC);
            } else if (resultSort == SearchResultSort.PRICEL2H) {
                sorts = new Sort[1];
                sorts[0] = new Sort(ProductModel2SortField.F_PRICE.getFieldName(), Order.ASC);
            } else if (resultSort == SearchResultSort.PRICEH2L) {
                sorts = new Sort[1];
                sorts[0] = new Sort(ProductModel2SortField.F_PRICE.getFieldName(), Order.DESC);
            }
        }

        // pivot fields
        PivotFacet[] pivotFacets = new PivotFacet[pivotFieldSize];
        if (pivotFieldSize > 0) {
            for (int i = 0; i < pivotFieldSize; i++) {
                // cate2 distinct 提取出来所有值
                pivotFacets[i] = new PivotFacet(pivotFields.get(i));
            }
        }

        // filter list
        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        if (NumberUtils.isNumber(sc.getCategoryId())) {
            fqList.add(new FilterQuery("cate" + sc.getLevel(), sc.getCategoryId()));
        }
        int priceFrom = sc.getPriceFrom(), priceTo = sc.getPriceTo();
        String priceFromStr = "*", priceToStr = "*";
        if (priceFrom <= 0) {
            priceFrom = 1;
        }
        if (priceFrom < priceTo) {
            priceFromStr = String.valueOf(priceFrom);
            priceToStr = String.valueOf(priceTo);
        } else {
            priceFromStr = String.valueOf(priceFrom);
        }
        System.out.println("priceFromStr   " + priceFromStr + "   priceToStr   " + priceToStr);

        fqList.add(new FilterQuery("minPrice", String.format("[%s TO %s]", priceFromStr, priceToStr)));

        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);

        String keyword = sc.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            keyword = "*:*";
        }

        // search by solr
        SearchResult<PtmStdSkuModel> sr = searchObjs(keyword, fqs, sorts, pivotFacets, sc.getPage() <= 1 ? 1 : sc.getPage(), sc.getPageSize(), true);
        //process pivot fields
        Map<String, List<NameValue>> pivotFieldVals = new HashMap<>();
        if (pivotFieldSize > 0) {
            NamedList<List<PivotField>> nl = sr.getFacetPivot();

            for (int i = 0; i < pivotFieldSize; i++) {
                String field = pivotFields.get(i);

                List<PivotField> cate2List = nl.get(field);
                for (PivotField pf : cate2List) {// string - object - long
//                    System.out.println(pf.getValue() + "\t" + pf.getCount());
                    List<NameValue> nvs = pivotFieldVals.get(field);
                    if (nvs == null) {
                        nvs = new ArrayList<>();
                        pivotFieldVals.put(field, nvs);
                    }
                    nvs.add(new NameValue<Long, Long>((Long) pf.getValue(), Long.valueOf(pf.getCount())));
                }
            }
        }
        PageableResult<PtmStdSkuModel> pagedPms = new PageableResult<PtmStdSkuModel>(sr.getResult(), sr.getTotalCount(), sc.getPage(), sc.getPageSize(), pivotFieldVals);
        return pagedPms;
    }

}
