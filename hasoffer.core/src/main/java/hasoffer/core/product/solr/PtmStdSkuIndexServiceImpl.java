package hasoffer.core.product.solr;

import hasoffer.base.config.AppConfig;
import hasoffer.data.solr.AbstractIndexService;
import org.springframework.stereotype.Service;

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

}
