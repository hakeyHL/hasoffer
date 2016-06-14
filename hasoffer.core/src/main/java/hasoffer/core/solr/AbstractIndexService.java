package hasoffer.core.solr;

import hasoffer.base.utils.BeanUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by glx on 2015/2/5.
 */
public abstract class AbstractIndexService<I, M extends IIdentifiable<I>> implements IIndexService<I, M> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractIndexService.class);
    private HttpSolrServer solrServer;

    public AbstractIndexService() {
        solrServer = new HttpSolrServer(getSolrUrl());
        solrServer.setConnectionTimeout(5000);
    }

    protected abstract String getSolrUrl();

    @Override
    public void createOrUpdate(M... ts) {
        try {
            solrServer.add(this.createDocuments(ts));
            solrServer.commit();
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(String id) {
        try {
            solrServer.deleteById(id);
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchResult<I> search(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize) {
        return this.search(qs, fqs, sorts, pivotFacets, pageNumber, pageSize, true);
    }

    @Override
    public SearchResult<I> search(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        QueryResponse rsp = searchSolr(qs, fqs, sorts, pivotFacets, pageNumber, pageSize, useCache);

        List<I> ids = new ArrayList<I>();
        Iterator<SolrDocument> iter = rsp.getResults().iterator();
        while (iter.hasNext()) {
            SolrDocument doc = iter.next();
            Object id = doc.getFieldValue("id");
            ids.add((I) id);
        }

        SearchResult<I> result = new SearchResult<I>();
        result.setResult(ids);
        long numFound = rsp.getResults().getNumFound();
        result.setTotalCount(numFound);

        NamedList<List<PivotField>> namedFacetPivot = rsp.getFacetPivot();
        result.setFacetPivot(namedFacetPivot);

        return result;
    }

    @Override
    public SearchResult<I> search(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize) {
        return this.search(q, fqs, sorts, pivotFacets, pageNumber, pageSize, true);
    }

    @Override
    public SearchResult<I> search(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        Query[] qs = new Query[]{new Query("", q)};
        return this.search(qs, fqs, sorts, pivotFacets, pageNumber, pageSize, useCache);
    }

    @Override
    public SearchResult<M> searchObjs(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        Query[] qs = new Query[]{new Query("", q)};
        QueryResponse rsp = searchSolr(qs, fqs, sorts, pivotFacets, pageNumber, pageSize, useCache);

        Class clazz = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        List<M> ms = convert(rsp.getResults(), clazz);

        SearchResult<M> result = new SearchResult<M>();
        result.setResult(ms);

        long numFound = rsp.getResults().getNumFound();
        result.setTotalCount(numFound);

        NamedList<List<PivotField>> namedFacetPivot = rsp.getFacetPivot();
        result.setFacetPivot(namedFacetPivot);

        return result;
    }

    private List<M> convert(SolrDocumentList results, Class<M> clazz) {
        List<M> list = new ArrayList<M>();

        Iterator<SolrDocument> iter = results.iterator();
        while (iter.hasNext()) {
            SolrDocument sd = iter.next();
            try {
                M m = clazz.newInstance();
                for (Map.Entry<String, Object> kv : sd.entrySet()) {
                    BeanUtils.setProperty(m, kv.getKey(), kv.getValue());
                }
                list.add(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    protected QueryResponse searchSolr(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets, int pageNumber, int pageSize, boolean useCache) {
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/select");
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
        logger.debug(qStr);

        QueryResponse rsp = null;
        try {
            rsp = solrServer.query(query);
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }

        return rsp;
    }

    protected List<SolrQuery.SortClause> getSort(Sort[] sorts) {
        List<SolrQuery.SortClause> sortClauses = new ArrayList<SolrQuery.SortClause>();
        if (sorts == null) {
            return sortClauses;
        }
        for (Sort s : sorts) {
            if (s == null) {
                continue;
            }
            sortClauses.add(new SolrQuery.SortClause(s.getField(), s.getOrder().name().toLowerCase()));
        }

        return sortClauses;
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


    protected Map<String, Object> createFields(M t) {
        Map<String, Object> map = null;
        try {
            map = BeanUtil.objectToMap(t);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    private SolrInputDocument createDocument(M t) {
        Map<String, Object> fields = this.createFields(t);
        if (fields == null) {
            return null;
        }

        if (!fields.containsKey("id")) {
            fields.put("id", t.getId());
        }

        SolrInputDocument doc = new SolrInputDocument();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            Object fieldValue = entry.getValue();
            if (fieldValue == null) {
                fieldValue = "";
            }
            // field.setBoost(boostField.getBoost());
            doc.addField(entry.getKey(), fieldValue);
        }

        return doc;
    }

    private List<SolrInputDocument> createDocuments(M... ts) {
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        for (M t : ts) {
            docs.add(this.createDocument(t));
        }
        return docs;
    }

    public void removeAll() throws SolrServerException, IOException {
        UpdateResponse response = solrServer.deleteByQuery("*");
        System.out.println(response.toString());
    }
}
