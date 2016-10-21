package hasoffer.job.manager;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chevy on 2016/10/21.
 */
@Component
public class ProductSearchManager {
    @Resource
    IProductService productService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ISearchService searchService;
    private Logger logger = LoggerFactory.getLogger(ProductSearchManager.class);

    public void saveSearchCount(String ymd) {
        logger.debug(String.format("save search count [%s]", ymd));

        List<SrmProductSearchCount> spscs = new ArrayList<>();

        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd);

        if (countMap.size() > 0) {
            delSearchCount(ymd);
        }

        int count = 0;
        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            analysisSearchProduct(countKv, ymd);

            if (count % 2000 == 0) {
                saveLogCount(spscs);
                count = 0;
            }

            count++;
        }

        if (ArrayUtils.hasObjs(spscs)) {
            saveLogCount(spscs);
        }
    }

    @DataSource(DataSourceType.Slave)
    private SrmProductSearchCount analysisSearchProduct(Map.Entry<Long, Long> countKv, String ymd) {

        long productId = countKv.getKey();
        long searchCount = countKv.getValue();

        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);

        int size = 0;
        if (ArrayUtils.hasObjs(cmpSkus)) {
            size = cmpSkus.size();
        }

        productService.importProduct2Solr2(productId);

        return new SrmProductSearchCount(ymd, productId, searchCount, size);
    }


    @DataSource(DataSourceType.Master)
    @Transactional
    private void delSearchCount(String ymd) {
        String sql = "delete from SrmProductSearchCount t where t.ymd='" + ymd + "'";
        dbm.deleteBySQL(sql);
    }

    @DataSource(DataSourceType.Master)
    private void saveLogCount(List<SrmProductSearchCount> spscs) {
        searchService.saveLogCount(spscs);
        spscs.clear();
    }


}
