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
}
