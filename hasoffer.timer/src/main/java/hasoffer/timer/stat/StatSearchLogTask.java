package hasoffer.timer.stat;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
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

    @Scheduled(cron = "0 20 5 * * ?")
    public void f() {

        String ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");

        Map<Long, Long> countMap = logCacheManager.getProductCount(ymd);

        List<PtmTopSelling> sscs = new ArrayList<PtmTopSelling>();

        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {
            PtmTopSelling ssc = new PtmTopSelling(ymd, countKv.getKey(), countKv.getValue());
            sscs.add(ssc);
        }

        Collections.sort(sscs, new Comparator<PtmTopSelling>() {
            @Override
            public int compare(PtmTopSelling o1, PtmTopSelling o2) {
                if (o1.getCount() > o2.getCount()) {
                    return -1;
                } else if (o1.getCount() < o2.getCount()) {
                    return 1;
                }
                return 0;
            }
        });

        int size = 20;
        if (sscs.size() < size) {
            size = sscs.size();
        }

        searchService.saveLogCount(sscs.subList(0, size));
    }

}
