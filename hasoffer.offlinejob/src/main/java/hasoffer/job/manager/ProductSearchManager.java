package hasoffer.job.manager;

import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by chevy on 2016/10/21.
 */
@Component
public class ProductSearchManager {

    private final Logger searchLog = LoggerFactory.getLogger("StatSearchLogJobBean.log");

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

//    public void saveSearchCount(String ymd) {
//        logger.debug(String.format("save search count [%s]", ymd));
//        searchLog.info("saveSearchCount(String ymd) {}: start.", ymd);
//        List<SrmProductSearchCount> spscs = new ArrayList<>();
//
//        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd);
//
//        if (countMap.size() > 0) {
//            delSearchCount(ymd);
//        }
//
//        int count = 0;
//        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {
//
//            analysisSearchProduct(countKv, ymd);
//
//            if (count % 2000 == 0) {
//                saveLogCount(spscs);
//                count = 0;
//            }
//
//            count++;
//        }
//
//        if (ArrayUtils.hasObjs(spscs)) {
//            saveLogCount(spscs);
//        }
//        searchLog.info("saveSearchCount(String ymd) {}: end.", ymd);
//    }
//
//    @DataSource(DataSourceType.Slave)
//    private SrmProductSearchCount analysisSearchProduct(Map.Entry<Long, Long> countKv, String ymd) {
//
//        long productId = countKv.getKey();
//        long searchCount = countKv.getValue();
//
//        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
//
//        int size = 0;
//        if (ArrayUtils.hasObjs(cmpSkus)) {
//            size = cmpSkus.size();
//        }
//
//        productService.importProduct2Solr2(productId);
//
//        return new SrmProductSearchCount(ymd, productId, searchCount, size);
//    }
//
//
//    @DataSource(DataSourceType.Master)
//    @Transactional
//    private void delSearchCount(String ymd) {
//        String sql = "delete from SrmProductSearchCount t where t.ymd='" + ymd + "'";
//        dbm.deleteBySQL(sql);
//    }
//
//    @DataSource(DataSourceType.Master)
//    private void saveLogCount(List<SrmProductSearchCount> spscs) {
//        searchService.saveLogCount(spscs);
//        spscs.clear();
//    }


}
