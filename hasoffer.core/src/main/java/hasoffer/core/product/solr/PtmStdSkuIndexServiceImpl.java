package hasoffer.core.product.solr;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.data.solr.*;
import jodd.util.NameValue;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hs on 2016年11月28日.
 * Time 18:36
 */
@Service
public class PtmStdSkuIndexServiceImpl extends AbstractIndexService<Long, PtmStdSkuModel> {
    public static void main(String[] args) {
        String aas = "_5D5_inchWMore";
        StringBuilder sb = new StringBuilder();
//        String s = aas.replaceAll("_", " ");
        String s[] = aas.split("_");
        for (String str : s) {
            System.out.println(":" + str + ":");
        }
        int i = 0;
        if (s[0] == "") {
            i = 1;
        }
        for (; i < s.length; i++) {
            if (s[i].contains("W")) {
                s[i] = s[i].replaceAll("W", "&");
                sb.append(s[i]);
            } else if (s[i].contains("D")) {
                s[i] = s[i].replaceAll("D", ".");
                sb.append(s[i]);
            } else {
                sb.append(s[i]);
            }
            if (i >= 1 && i < s.length - 1) {
                sb.append("-");
            }
        }
        System.out.println(sb.toString());
    }

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
        sorts = getSorts(sorts, resultSort);

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
                for (PivotField pf : cate2List) {
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
        addBillion2ListEle(pagedPms);
        //遍历列表修改id
       /* if (pagedPms != null && pagedPms.getData() != null && pagedPms.getData().size() > 0) {
            Iterator<PtmStdSkuModel> iterator = pagedPms.getData().iterator();
            while (iterator.hasNext()) {
                PtmStdSkuModel ptmStdSkuModel = iterator.next();
                ptmStdSkuModel.setId(ApiUtils.addBillion(ptmStdSkuModel.getId()));
            }
        }*/
        return pagedPms;
    }

    private Sort[] getSorts(Sort[] sorts, SearchResultSort resultSort) {
        if (resultSort != null) {
            if (resultSort == SearchResultSort.POPULARITY) {
                sorts = new Sort[2];
                sorts[0] = new Sort("review", Order.DESC);
                sorts[1] = new Sort(ProductModel2SortField.F_POPULARITY.getFieldName(), Order.DESC);
            } else if (resultSort == SearchResultSort.PRICEL2H) {
                sorts = new Sort[1];
                sorts[0] = new Sort(ProductModel2SortField.F_PRICE.getFieldName(), Order.ASC);
            } else if (resultSort == SearchResultSort.PRICEH2L) {
                sorts = new Sort[1];
                sorts[0] = new Sort(ProductModel2SortField.F_PRICE.getFieldName(), Order.DESC);
            }
        } else {
            sorts[0] = new Sort("review", Order.DESC);
        }
        return sorts;
    }

    public PageableResult<PtmStdSkuModel> searchStdPricesByCategory(SearchCriteria criteria) {
        List<String> pivotFields = criteria.getPivotFields();
        int pivotFieldSize = pivotFields == null ? 0 : pivotFields.size();
        PivotFacet[] pivotFacets = new PivotFacet[pivotFieldSize];
        if (pivotFieldSize > 0) {
            for (int i = 0; i < pivotFieldSize; i++) {
                // cate2 distinct 提取出来所有值
                pivotFacets[i] = new PivotFacet(pivotFields.get(i));
            }
        }

        int level = criteria.getLevel();
        String cateId = criteria.getCategoryId();
        int page = criteria.getPage();
        int size = criteria.getPageSize();
        if (level < 1 || level > 3) {
            return null;
        }
        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        int priceFrom = criteria.getPriceFrom(), priceTo = criteria.getPriceTo();
        String priceFromStr = "*", priceToStr = "*";
        if (priceFrom < priceTo && priceFrom >= 0) {
            if (priceFrom <= 0) {
                priceFrom = 1;
            }
            priceFromStr = String.valueOf(priceFrom);
            if (priceTo > 0) {
                priceToStr = String.valueOf(priceTo);
            }
            fqList.add(new FilterQuery("minPrice", String.format("[%s TO %s]", priceFromStr, priceToStr)));
        } else {
            fqList.add(new FilterQuery("minPrice", String.format("[%s TO %s]", "1", "*")));
        }
        Sort[] sorts = new Sort[1];
        SearchResultSort resultSort = criteria.getSort();
        sorts = getSorts(sorts, resultSort);
        String q = "*:*";
        fqList.add(new FilterQuery("cate" + level, String.valueOf(cateId)));
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);
        SearchResult<PtmStdSkuModel> sr = searchObjs(q, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);
        Map<String, List<NameValue>> pivotFieldVals = new HashMap<>();
        if (pivotFieldSize > 0) {
            NamedList<List<PivotField>> facetPivot = sr.getFacetPivot();
            for (int i = 0; i < pivotFieldSize; i++) {
                String field = pivotFields.get(i);
                List<PivotField> cate2List = facetPivot.get(field);
                for (PivotField pf : cate2List) {
                    List<NameValue> nvs = pivotFieldVals.get(field);
                    if (nvs == null) {
                        nvs = new ArrayList<>();
                        pivotFieldVals.put(field, nvs);
                    }
                    nvs.add(new NameValue<>(pf.getValue(), Long.valueOf(pf.getCount())));
                }
            }
        }
        PageableResult<PtmStdSkuModel> ptmStdSkuModelPageableResult = new PageableResult<>(sr.getResult(), sr.getTotalCount(), page, size, pivotFieldVals);
        addBillion2ListEle(ptmStdSkuModelPageableResult);
        return ptmStdSkuModelPageableResult;
    }

    private void addBillion2ListEle(PageableResult<PtmStdSkuModel> ptmStdSkuModelPageableResult) {
        if (ptmStdSkuModelPageableResult != null && ptmStdSkuModelPageableResult.getData() != null && ptmStdSkuModelPageableResult.getData().size() > 0) {
            Iterator<PtmStdSkuModel> iterator = ptmStdSkuModelPageableResult.getData().iterator();
            while (iterator.hasNext()) {
                PtmStdSkuModel ptmStdSkuModel = iterator.next();
                ptmStdSkuModel.setId(ApiUtils.addBillion(ptmStdSkuModel.getId()));
            }
        }
    }

    public String getCategoryFilterEnum(String enumString) {
        if (enumString != null) {
            //-按_ split D换成. W换成&
            //除第一个_其他换空格
//            enumString
        }
        return null;
    }
}
