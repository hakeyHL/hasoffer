package hasoffer.core.product.solr;

import com.alibaba.fastjson.JSON;
import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.data.solr.*;
import jodd.util.NameValue;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
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
        setFacetValues(pivotFields, pivotFieldSize, sr, pivotFieldVals);
        PageableResult<PtmStdSkuModel> pagedPms = new PageableResult<>(sr.getResult(), sr.getTotalCount(), sc.getPage(), sc.getPageSize(), pivotFieldVals);
        addBillion2ListEle(pagedPms);
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
        List<FilterQuery> fqList = new ArrayList<>();
        int priceFrom = criteria.getPriceFrom(), priceTo = criteria.getPriceTo();
        String priceFromStr = "*", priceToStr = "*";
        ApiUtils.setPriceSearchScope(fqList, priceFrom, priceTo, priceToStr);
        Sort[] sorts = new Sort[1];
        SearchResultSort resultSort = criteria.getSort();
        sorts = getSorts(sorts, resultSort);
        String q = "*:*";
        fqList.add(new FilterQuery("cate" + level, String.valueOf(cateId)));
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);
        SearchResult<PtmStdSkuModel> sr = searchObjs(q, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);
        Map<String, List<NameValue>> pivotFieldVals = new HashMap<>();
        setFacetValues(pivotFields, pivotFieldSize, sr, pivotFieldVals);
        PageableResult<PtmStdSkuModel> ptmStdSkuModelPageableResult = new PageableResult<>(sr.getResult(), sr.getTotalCount(), page, size, pivotFieldVals);
        addBillion2ListEle(ptmStdSkuModelPageableResult);
        return ptmStdSkuModelPageableResult;
    }

    private void setFacetValues(List<String> pivotFields, int pivotFieldSize, SearchResult<PtmStdSkuModel> sr, Map<String, List<NameValue>> pivotFieldVals) {
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
                    nvs.add(new NameValue(pf.getValue(), Long.valueOf(pf.getCount())));
                }
            }
        }
    }

    public PageableResult<PtmStdSkuModel> filterStdSkuOnCategoryByCriteria(SearchCriteria searchCriteria) {
        String queryString = "*:*";
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
        int level = searchCriteria.getLevel();
        String cateId = searchCriteria.getCategoryId();
        int page = searchCriteria.getPage();
        int size = searchCriteria.getPageSize();
        fqList.add(new FilterQuery("cate" + level, String.valueOf(cateId)));
        addQuerParams2List(searchCriteria, fqList);
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);

        int priceFrom = searchCriteria.getPriceFrom(), priceTo = searchCriteria.getPriceTo();
        String priceFromStr = "*", priceToStr = "*";
        ApiUtils.setPriceSearchScope(fqList, priceFrom, priceTo, priceToStr);
        SearchResult<PtmStdSkuModel> sr = searchObjs(queryString, fqs, null, null, page <= 1 ? 1 : page, size, true);
        //缓存以及从缓存中取
        Map<String, List<NameValue>> pivotFieldVals = new HashMap<>();
        setFacetValues(pivotFields, pivotFieldSize, sr, pivotFieldVals);
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

    private String joinQueryParams(String[] params, String param) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i > 0 && i < param.length() - 1) {
                sb.append(")");
            }
            if (i < params.length - 1) {
                sb.append(" OR ");
                sb.append("(" + param + ":");
            }
        }
        return sb.toString();
    }

    private void addQuerParams2List(SearchCriteria searchCriteria, List<FilterQuery> fqList) {
        //1. brand
        if (searchCriteria.getBrand() != null && searchCriteria.getBrand().length > 0) {
            String[] brands = searchCriteria.getBrand();
            fqList.add(new FilterQuery("brand", joinQueryParams(brands, "brand")));
        }
        //2. network --2G 3G 4G 处理下
        if (searchCriteria.getNetwork() != null && searchCriteria.getNetwork().length > 0) {
            String[] networks = searchCriteria.getNetwork();
            for (String network : networks) {
        /*        if (network.equalsIgnoreCase("3G")) {
                    fqList.add(new FilterQuery("Network3G", joinQueryParams(new String[]{"3G"}, "Network3G")));
                }
                if (network.equalsIgnoreCase("4G")) {
                    fqList.add(new FilterQuery("Network4G", joinQueryParams(new String[]{"4G"}, "Network4G")));
                }
                if (network.equalsIgnoreCase("2G")) {
                    fqList.add(new FilterQuery("Network", joinQueryParams(networks, "Network")));
                }*/
                fqList.add(new FilterQuery("Network", joinQueryParams(networks, "Network")));
            }
        }
        //3. screenResolution
        if (searchCriteria.getScreenResolution() != null && searchCriteria.getScreenResolution().length > 0) {
            String[] screenResolutions = searchCriteria.getScreenResolution();
            fqList.add(new FilterQuery("Screen_Resolution", joinQueryParams(screenResolutions, "Screen_Resolution")));
        }
        //4. operatingSystem
        if (searchCriteria.getOpreatingSystem() != null && searchCriteria.getOpreatingSystem().length > 0) {
            String[] opreatingSystems = searchCriteria.getOpreatingSystem();
            fqList.add(new FilterQuery("Operating_System", joinQueryParams(opreatingSystems, "Operating_System")));
        }
        //5. expandableMemory
        if (searchCriteria.getExpandableMemory() != null && searchCriteria.getExpandableMemory().length > 0) {
            String[] expandableMemorys = searchCriteria.getExpandableMemory();
            fqList.add(new FilterQuery("Expandable_Memory", joinQueryParams(expandableMemorys, "Expandable_Memory")));
        }
        //6. ram
        if (searchCriteria.getRam() != null && searchCriteria.getRam().length > 0) {
            String[] rams = searchCriteria.getRam();
            fqList.add(new FilterQuery("queryRam", joinQueryParams(rams, "queryRam")));
        }
        //7. batteryCapacity
        if (searchCriteria.getBatteryCapacity() != null && searchCriteria.getBatteryCapacity().length > 0) {
            String[] batteryCapacitys = searchCriteria.getBatteryCapacity();
            fqList.add(new FilterQuery("queryBatteryCapacity", joinQueryParams(batteryCapacitys, "queryBatteryCapacity")));
        }
        //8. internalMemory
        if (searchCriteria.getInternalMemory() != null && searchCriteria.getInternalMemory().length > 0) {
            String[] internalMemorys = searchCriteria.getInternalMemory();
            fqList.add(new FilterQuery("queryInternalMemory", joinQueryParams(internalMemorys, "queryInternalMemory")));
        }
        //9. primaryCamera
        if (searchCriteria.getPrimaryCamera() != null && searchCriteria.getPrimaryCamera().length > 0) {
            String[] primaryCameras = searchCriteria.getPrimaryCamera();
            fqList.add(new FilterQuery("queryPrimaryCamera", joinQueryParams(primaryCameras, "queryPrimaryCamera")));
        }
        //10.secondaryCamera
        if (searchCriteria.getSecondaryCamera() != null && searchCriteria.getSecondaryCamera().length > 0) {
            String[] secondaryCameras = searchCriteria.getSecondaryCamera();
            fqList.add(new FilterQuery("querySecondaryCamera", joinQueryParams(secondaryCameras, "querySecondaryCamera")));
        }
        //11.screenSize
        if (searchCriteria.getScreenSize() != null && searchCriteria.getScreenSize().length > 0) {
            String[] screenSizes = searchCriteria.getScreenSize();
            fqList.add(new FilterQuery("queryScreenSize", joinQueryParams(screenSizes, "queryScreenSize")));
        }
    }

    public PtmStdSkuModel getStdSkuModelById(long stdSkuId) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("id:" + stdSkuId);
        try {
            QueryResponse query = query(solrQuery);
            SolrDocumentList results = query.getResults();
            if (results.size() > 0) {
                String jsonSkuModel = JSON.toJSONString(results.get(0));
                PtmStdSkuModel ptmStdSkuModel = JSON.parseObject(jsonSkuModel, PtmStdSkuModel.class);
                return ptmStdSkuModel;
            }
        } catch (SolrServerException e) {
            System.out.println("=========================  get stdSku by Id from solr exception ." + e.getMessage() + "==========================");
        }
        return null;
    }


}
