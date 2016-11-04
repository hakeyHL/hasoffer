package hasoffer.job.bean.stat;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.job.manager.ProductSearchManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/6/27.
 */
public class StatSearchLogJobBean extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger("StatSearchLogJobBean.log");

    @Resource
    SearchLogCacheManager logCacheManager;
    @Resource
    ProductSearchManager productSearchManager;
    @Resource
    IProductService productService;
    @Resource
    ISearchService searchService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    ICmpSkuService cmpSkuService;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("StatSearchLogJobBean start");

        String ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");

        logger.info("saveSearchCount_old({}) start", ymd);
        saveSearchCount(ymd);
        logger.info("saveSearchCount_old({}) end", ymd);

        logger.info("expTopSellingsFromSearchCount({}) start", ymd);
        productService.expTopSellingsFromSearchCount(ymd);
        logger.info("expTopSellingsFromSearchCount({}) end", ymd);

        logger.info("statSearchCount_old({}) start", ymd);
        searchService.statSearchCount_old(ymd);
        logger.info("statSearchCount_old({}) end", ymd);

        logger.info("StatSearchLogJobBean end");
    }

    public void saveSearchCount(String ymd) {
        logger.debug(String.format("save search count [%s]", ymd));

        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd);

        if (countMap.size() > 0) {
            searchService.delSearchCount(ymd);
        }

        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            long productId = countKv.getKey();
            long searchCount = countKv.getValue();

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
            int size = 0;
            if (ArrayUtils.hasObjs(cmpSkus)) {
                size = cmpSkus.size();
            }

            searchService.saveLogCount(new SrmProductSearchCount(ymd, productId, searchCount, size));

            productService.importProduct2Solr2(productId);
        }

       /* logger.debug(String.format("save search count [%s]", ymd));

        List<SrmProductSearchCount> spscs = new ArrayList<SrmProductSearchCount>();

        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd);

        if (countMap.size() > 0) {
            delSearchCount(ymd);
        }

        int count = 0;
        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            long productId = countKv.getKey();
            long searchCount = countKv.getValue();

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
            int size = 0;
            if (ArrayUtils.hasObjs(cmpSkus)) {
                size = cmpSkus.size();
            }

            spscs.add(new SrmProductSearchCount(ymd, productId, searchCount, size));

            if (count % 2000 == 0) {
                saveLogCount(spscs);
                count = 0;
                spscs.clear();
            }

            productService.importProduct2Solr2(productId);
        }

        if (ArrayUtils.hasObjs(spscs)) {
            saveLogCount(spscs);
        }*/

    }
}
