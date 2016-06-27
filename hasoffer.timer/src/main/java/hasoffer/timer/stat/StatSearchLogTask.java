package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.search.SrmSearchCount;
import hasoffer.core.search.ISearchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/6/27.
 */
@Component
public class StatSearchLogTask {

    @Resource
    SearchLogCacheManager logCacheManager;
    @Resource
    ISearchService searchService;

    @Scheduled(cron = "0 20 0 * * ?")
    public void f() {

        String ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");

        Map<String, Long> countMap = logCacheManager.getSearchLogCount(ymd);

        List<SrmSearchCount> sscs = new ArrayList<SrmSearchCount>();

        for (Map.Entry<String, Long> countKv : countMap.entrySet()) {
            SrmSearchCount ssc = new SrmSearchCount(ymd, countKv.getKey(), countKv.getValue());
            sscs.add(ssc);
        }

        searchService.saveLogCount(sscs);
    }

}
