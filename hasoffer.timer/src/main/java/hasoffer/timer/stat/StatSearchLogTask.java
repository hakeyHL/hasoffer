package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
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

    @Scheduled(cron = "0 20 5 * * ?")
    public void f() {

        String ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");

        searchService.saveSearchCount(ymd);

    }

}
