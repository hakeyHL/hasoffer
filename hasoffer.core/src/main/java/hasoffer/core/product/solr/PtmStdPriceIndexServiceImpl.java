package hasoffer.core.product.solr;

import hasoffer.base.config.AppConfig;
import hasoffer.base.model.PageableResult;
import hasoffer.core.app.vo.SearchIO;
import hasoffer.data.solr.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
}
