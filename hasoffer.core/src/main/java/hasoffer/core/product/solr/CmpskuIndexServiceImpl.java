package hasoffer.core.product.solr;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.CoreConfig;
import hasoffer.core.solr.*;
import org.springframework.stereotype.Service;


@Service
public class CmpskuIndexServiceImpl extends AbstractIndexService<Long, CmpSkuModel> {
    @Override
    protected String getSolrUrl() {
        return CoreConfig.get(CoreConfig.SOLR_CMPSKU_URL);
    }

    public PageableResult<CmpSkuModel> searchSku(String key, int page, int size) {

        String q = key;
        if (StringUtils.isEmpty(key)) {
            q = "*:*";
        }

        FilterQuery[] fqs = null;
        Sort[] sorts = null;
        PivotFacet[] pivotFacets = null;

        SearchResult<CmpSkuModel> sr = searchObjs(q, fqs, sorts, pivotFacets, page, size, true);

        return new PageableResult<CmpSkuModel>(sr.getResult(), sr.getTotalCount(), page, size);
    }
}
