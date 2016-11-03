package hasoffer.job.bean.stat;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/11/2.
 */
public class StatLogCountJobBean extends QuartzJobBean {

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ISearchService searchService;
    @Resource
    IProductService productService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    CmpSkuCacheManager cmpSkuCacheManager;
    @Resource
    ProductCacheManager productCacheManager;

    private Logger logger = LoggerFactory.getLogger(StatLogCountJobBean.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String ymd_hour = TimeUtils.parse(TimeUtils.add(TimeUtils.nowDate(), TimeUtils.MILLISECONDS_OF_1_HOUR * -1), "yyyyMMdd_HH");

        logger.info("statSearchCountByHour : " + ymd_hour);

        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd_hour);

        if (countMap.size() > 0) {
            logger.info("delete if exists old stats");
            searchService.delSearchCountByHour(ymd_hour);
        }

        logger.info("to save search count");
        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            long productId = countKv.getKey();
            long searchCount = countKv.getValue();

            List<PtmCmpSku> cmpSkus = cmpSkuCacheManager.listCmpSkus(productId, SkuStatus.ONSALE);

            int size = cmpSkus.size();

            searchService.saveSearchCountByHour(ymd_hour, productId, searchCount, size);

            productCacheManager.put2UpdateQueue(productId);
        }

    }
}
