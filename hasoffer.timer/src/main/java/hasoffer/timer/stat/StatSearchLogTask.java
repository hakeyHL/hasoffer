package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created on 2016/6/27.
 */
@Component
public class StatSearchLogTask {

    @Resource
    SearchLogCacheManager logCacheManager;
    @Resource
    ISearchService searchService;
    @Resource
    IProductService productService;

    @Scheduled(cron = "0 20 21 * * ?")
    public void f() {

        String ymd = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");

        // 保存所有被搜索过的商品
        searchService.saveSearchCount(ymd);

        // top selling
        productService.expTopSellingsFromSearchCount(ymd);

        // 统计比价质量
        searchService.statSearchCount(ymd);
    }

}
