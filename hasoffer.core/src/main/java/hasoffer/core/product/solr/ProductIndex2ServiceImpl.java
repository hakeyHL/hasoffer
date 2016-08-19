package hasoffer.core.product.solr;

import hasoffer.base.config.AppConfig;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.data.solr.*;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class ProductIndex2ServiceImpl extends AbstractIndexService<Long, ProductModel2> {
    @Override
    protected String getSolrUrl() {
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
    public PageableResult<ProductModel2> searchProductsByKey_test(String title, int page, int size) {
        Sort[] sorts = null;
        PivotFacet[] pivotFacets = new PivotFacet[1];

        // cate2 distinct 提取出来所有值
        pivotFacets[0] = new PivotFacet("cate2");

        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);

        SearchResult<ProductModel2> sr = searchObjs(title, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);

        NamedList<List<PivotField>> nl = sr.getFacetPivot();

        List<PivotField> cate2List = nl.get("cate2");
        for (PivotField pf : cate2List) {
            System.out.println(pf.getValue() + "\t" + pf.getCount());
        }

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
    public PageableResult<ProductModel2> searchProductsByKey(String title, int page, int size) {
        Sort[] sorts = null;
        PivotFacet[] pivotFacets = null;

        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
        FilterQuery[] fqs = fqList.toArray(new FilterQuery[0]);

        SearchResult<ProductModel2> sr = searchObjs(title, fqs, sorts, pivotFacets, page <= 1 ? 1 : page, size, true);

        return new PageableResult<ProductModel2>(sr.getResult(), sr.getTotalCount(), page, size);
    }

    /**
     * 根据类目搜索商品
     *
     * @param cateId
     * @param level
     * @param page
     * @param size
     * @return
     */
    public PageableResult<ProductModel2> searchPro(long cateId, int level, int page, int size) {
        if (level < 1 || level > 3) {
            return null;
        }
        Sort[] sorts = new Sort[]{new Sort("searchCount", Order.DESC)};
        String q = "*:*";
        PivotFacet[] pivotFacets = null;

        List<FilterQuery> fqList = new ArrayList<FilterQuery>();
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

    // ************************父类实现*******************************
    /*
    protected QueryResponse searchSolr(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/query2");//select
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

    protected HttpSolrServer solrServer;

    public ProductIndex2ServiceImpl() {
        solrServer = new HttpSolrServer(getSolrUrl());
        solrServer.setConnectionTimeout(5000);
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
        Query[] qs = new Query[]{new Query("", q)};
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
}
