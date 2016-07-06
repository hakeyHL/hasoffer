package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.search.SrmSearchCount;
import hasoffer.core.search.ISearchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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

        Map<Long, Long> countMap = logCacheManager.getProductCount(ymd);

        List<SrmSearchCount> sscs = new ArrayList<SrmSearchCount>();

        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {
            SrmSearchCount ssc = new SrmSearchCount(ymd, countKv.getKey(), countKv.getValue());
            sscs.add(ssc);
        }

        Collections.sort(sscs, new Comparator<SrmSearchCount>() {
            @Override
            public int compare(SrmSearchCount o1, SrmSearchCount o2) {
                if (o1.getCount() > o2.getCount()) {
                    return -1;
                } else if (o1.getCount() < o2.getCount()) {
                    return 1;
                }
                return 0;
            }
        });

        searchService.saveLogCount(sscs.subList(0, 20));
    }

}
