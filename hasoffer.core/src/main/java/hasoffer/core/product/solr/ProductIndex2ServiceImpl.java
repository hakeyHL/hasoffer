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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ProductIndex2ServiceImpl extends AbstractIndexService<Long, ProductModel2> {

    Logger logger = LoggerFactory.getLogger(ProductIndex2ServiceImpl.class);
    /*protected HttpSolrServer solrServer;

    public ProductIndex2ServiceImpl() {
        solrServer = new HttpSolrServer(getSolrUrl());
        solrServer.setConnectionTimeout(5000);
    }*/

    @Override
    protected String getSolrUrl() {
//        return "http://solrserver:8983/solr/hasofferproduct3/";
        return AppConfig.get(AppConfig.SOLR_PRODUCT_2_URL);
    }

    /**
     * 根据关键词搜索
     *
     * @param title
     * @param page
     * @param size
     * @return
     */
    public PageableResult<ProductModel2> searchProductsByKey(String title, int page, int size) {
        Sort[] sorts = null;
        PivotFacet[] pivotFacets = new PivotFacet[1];

        // cate2 distinct 提取出来所有值
        pivotFacets[0] = new PivotFacet("cate2");

        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);

        SearchResult<ProductModel2> sr = searchObjs(title, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);

        return new PageableResult<ProductModel2>(sr.getResult(), sr.getTotalCount(), page, size);
    }

    /**
     * 根据关键词搜索
     *
     * @param title
     * @param page
     * @param size
     * @return
     */
    public PageableResult<ProductModel2> searchProductsByKey(String title, int page, int size, SearchResultSort resultSort) {

        Sort[] sorts = null;
        if (resultSort != null) {
            sorts = new Sort[1];
            if (resultSort == SearchResultSort.POPULARITY) {
                sorts[0] = new Sort("searchCount", Order.DESC);
            } else if (resultSort == SearchResultSort.PRICEL2H) {
                sorts[0] = new Sort("minPrice", Order.ASC);
            } else if (resultSort == SearchResultSort.PRICEH2L) {
                sorts[0] = new Sort("minPrice", Order.DESC);
            }
        }

        PivotFacet[] pivotFacets = null;

        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);

        SearchResult<ProductModel2> sr = searchObjs(title, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);

        return new PageableResult<ProductModel2>(sr.getResult(), sr.getTotalCount(), page, size);
    }

    /**
     * 根据关键词搜索
     *
     * @param sc
     * @return
     */
    public PageableResult<ProductModel2> searchProducts(SearchCriteria sc) {
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
            long cateId = Long.valueOf(sc.getCategoryId());
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
        SearchResult<ProductModel2> sr = searchObjs(keyword, fqs, sorts, pivotFacets, sc.getPage() <= 1 ? 1 : sc.getPage(), sc.getPageSize(), true);
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
        PageableResult<ProductModel2> pagedPms = new PageableResult<ProductModel2>(sr.getResult(), sr.getTotalCount(), sc.getPage(), sc.getPageSize(), pivotFieldVals);

        return pagedPms;
    }

    /**
     * 根据类目搜索商品
     */
    @Deprecated
    public PageableResult<ProductModel2> searchPro(SearchCriteria criteria) {
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
        ApiUtils.setPriceSearchScope(fqList, priceFrom, priceTo, priceToStr);
        Sort[] sorts = new Sort[1];
        SearchResultSort resultSort = criteria.getSort();
        if (resultSort == null) {
            sorts[0] = new Sort("searchCount", Order.DESC);
        } else {
            if (resultSort == SearchResultSort.POPULARITY) {
                sorts[0] = new Sort(ProductModel2SortField.F_POPULARITY.getFieldName(), Order.DESC);
            } else if (resultSort == SearchResultSort.PRICEL2H) {
                sorts[0] = new Sort(ProductModel2SortField.F_PRICE.getFieldName(), Order.ASC);
            } else if (resultSort == SearchResultSort.PRICEH2L) {
                sorts[0] = new Sort(ProductModel2SortField.F_PRICE.getFieldName(), Order.DESC);
            }
        }

        String q = "*:*";
        PivotFacet[] pivotFacets = null;
        fqList.add(new FilterQuery("cate" + level, String.valueOf(cateId)));
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);
        System.out.println(Thread.currentThread().getName() + " page " + page + "  size " + size);
        SearchResult<ProductModel2> sr = searchObjs(q, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);

        return new PageableResult<ProductModel2>(sr.getResult(), sr.getTotalCount(), page, size);
    }

    /**
     * 类目下按关键词搜索
     *
     * @param cateId
     * @param level
     * @param title
     * @param page
     * @param size
     * @return
     */
    public PageableResult searchPro(long cateId, int level, String title, int page, int size) {
        long cate1 = 0, cate2 = 0, cate3 = 0;

        if (level == 1) {
            cate1 = cateId;
        } else if (level == 2) {
            cate2 = cateId;
        } else if (level == 3) {
            cate3 = cateId;
        }

        return searchPro(cate1, cate2, cate3, title, page, size);
    }

    private PageableResult searchPro(long category1, long category2, long category3, String title, int page, int size) {
        String q = title;
        if (StringUtils.isEmpty(q)) {
            q = "*:*";
        }

        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        if (category3 > 0) {
            fqList.add(new FilterQuery("cate3", String.valueOf(category3)));
        } else if (category2 > 0) {
            fqList.add(new FilterQuery("cate2", String.valueOf(category2)));
        } else if (category1 > 0) {
            fqList.add(new FilterQuery("cate1", String.valueOf(category1)));
        }

        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);
        Sort[] sorts = null;
        PivotFacet[] pivotFacets = null;

        SearchResult<Long> sr = search(q, fqs, sorts, pivotFacets, page, size);

        long totalCount = sr.getTotalCount();

        return new PageableResult<Long>(sr.getResult(), totalCount, page, size);
    }

    public Map<String, List<String>> spellCheck(String keyword) {
        SolrQuery criteria = new SolrQuery();
        criteria.setRequestHandler("/spell");//select
        criteria.set("spellcheck", "true");
        criteria.set("spellcheck.q", keyword);
        criteria.set("spellcheck.build", "true");// 遇到新的检查词，会自动添加到索引里面
        criteria.set("spellcheck.count", 5);

        Map<String, List<String>> map = new HashMap<>();

        try {
            QueryResponse rsp = query(criteria);

            SpellCheckResponse spellres = rsp.getSpellCheckResponse();

            if (spellres != null) {
                if (!spellres.isCorrectlySpelled()) {
                    List<SpellCheckResponse.Suggestion> suggestions = spellres.getSuggestions();
                    for (SpellCheckResponse.Suggestion suggestion1 : suggestions) {
                        map.put(suggestion1.getToken(), suggestion1.getAlternatives());
                    }
                }
            }
        } catch (SolrServerException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            return map;
        }

    }

    // ************************父类实现*******************************
    /*protected QueryResponse searchSolr(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/select");//select
        String q = this.getQ(qs);
        if (!useCache) {
            q = "{!cache=false}" + q;
        }
        query.setQuery(q);

        String fq = this.getFQ(fqs);
        if (!useCache) {
            fq = "{!cache=false}" + fq;
        }
        query.setFilterQueries(fq);

        query.setSorts(this.getSort(sorts));
        query.setStart(pageNumber * pageSize - pageSize);
        query.setRows(pageSize);

        if (pivotFacets != null && pivotFacets.length > 0) {
            List<String> facetFields = new LinkedList<String>();
            for (PivotFacet pivotFacet : pivotFacets) {
                facetFields.add(pivotFacet.getField());
            }
            query.setFacet(true);
            query.addFacetPivotField(facetFields.toArray(new String[]{}));
        }

        String qStr = query.toString();
        qStr = URLDecoder.decode(qStr);

        QueryResponse rsp = null;
        try {
            rsp = solrServer.query(query);
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }

        return rsp;
    }

    private String getFQ(FilterQuery[] fqs) {
        if (fqs == null) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < fqs.length; i++) {
            if (fqs[i] == null) {
                continue;
            }
            if (i != fqs.length - 1) {
                buffer.append(fqs[i].toString() + " AND ");
            } else {
                buffer.append(fqs[i].toString());
            }
        }

        return buffer.toString();
    }

    private String getQ(Query[] qs) {
        if (qs == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < qs.length; i++) {
            if (qs[i] == null) {
                continue;
            }
            if (i != qs.length - 1) {
                buffer.append(qs[i].toString() + " OR ");
            } else {
                buffer.append(qs[i].toString());
            }
        }

        return buffer.toString();
    }

    public SearchResult<ProductModel2> searchObjs(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        Query[] qs = new Query[]{new Query(ConstantUtil.API_DATA_EMPTYSTRINGstr_createTime, q)};
        QueryResponse rsp = searchSolr(qs, fqs, sorts, pivotFacets, pageNumber, pageSize, useCache);

        Class clazz = (Class<ProductModel2>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        List<ProductModel2> ms = convert(rsp.getResults(), clazz);

        SearchResult<ProductModel2> result = new SearchResult<ProductModel2>();
        result.setResult(ms);

        long numFound = rsp.getResults().getNumFound();
        result.setTotalCount(numFound);

        NamedList<List<PivotField>> namedFacetPivot = rsp.getFacetPivot();
        result.setFacetPivot(namedFacetPivot);

        return result;
    }

    private List<ProductModel2> convert(SolrDocumentList results, Class<ProductModel2> clazz) {
        List<ProductModel2> list = new ArrayList<ProductModel2>();

        Iterator<SolrDocument> iter = results.iterator();
        while (iter.hasNext()) {
            SolrDocument sd = iter.next();
            try {
                ProductModel2 m = clazz.newInstance();
                for (Map.Entry<String, Object> kv : sd.entrySet()) {
                    BeanUtils.setProperty(m, kv.getKey(), kv.getValue());
                }
                list.add(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }*/

//
//    public static void parseCacheString2Object(String jsonString, Class obj) {
//        Method[] methods = obj.getMethods();
//        for (Method method : methods) {
//            if (method.getName().startsWith("set")) {
//                System.out.println(method.getName());
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        ProductIndex2ServiceImpl.parseCacheString2Object(ConstantUtil.API_DATA_EMPTYSTRINGstr_createTime, PageableResult.class);
//    }
}
