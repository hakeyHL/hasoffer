package hasoffer.job.bean.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.search.SrmProductSearchStat;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.job.manager.ProductSearchManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 * Created on 2016/6/27.
 */
public class StatSearchLogJobBean extends QuartzJobBean {

    @Resource
    SearchLogCacheManager logCacheManager;
    @Resource
    ProductSearchManager productSearchManager;
    @Resource
    IProductService productService;
    @Resource
    ISearchService searchService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//        String ymd = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");
        String ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");

        System.out.println("ymd = " + ymd);
        System.out.println("saveSearchCount...");
        // 保存所有被搜索过的商品
        productSearchManager.saveSearchCount(ymd);

        System.out.println("expTopSellingsFromSearchCount...");
        // top selling
        productService.expTopSellingsFromSearchCount(ymd);

        System.out.println("statSearchCount...");
        // 统计比价质量
        SrmProductSearchStat ss = searchService.statSearchCount(ymd);
        searchService.saveSrmProductSearchStat(ss);

        System.out.println("StatSearchLogJobBean finished.");
    }
}
