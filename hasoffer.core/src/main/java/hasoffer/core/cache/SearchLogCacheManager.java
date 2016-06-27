package hasoffer.core.cache;

import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.search.ISearchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Date : 2016/5/7
 * Function :
 */
@Component
public class SearchLogCacheManager {

    private static final String CACHE_KEY_PRE = "SEARCHLOG_";
    private static final long CACHE_EXPIRE_TIME = TimeUtils.SECONDS_OF_1_HOUR * 2;
    @Resource
    ICacheService<SrmSearchLog> cacheService;
    @Resource
    ISearchService searchService;

    /**
     * 根据id查询搜索日志
     *
     * @param logId
     * @return
     */
    public SrmSearchLog findSrmSearchLogById(String logId) {

        String key = CACHE_KEY_PRE + logId;

        SrmSearchLog searchLog = cacheService.get(SrmSearchLog.class, key, 0);

        if (searchLog == null) {
            searchLog = searchService.findSrmSearchLogById(logId);
        }

        if (searchLog != null) {
            cacheService.add(key, searchLog, CACHE_EXPIRE_TIME);
        }

        return searchLog;
    }

    public SrmSearchLog findSrmSearchLog(String logId) {
        String key = CACHE_KEY_PRE + logId;

        SrmSearchLog searchLog = cacheService.get(SrmSearchLog.class, key, 0);

        if (searchLog == null) {
            searchLog = searchService.findSrmSearchLogById(logId);
        }

        if (searchLog != null) {
            searchLog.setCount(searchLog.getCount() + 1);

            // 如果商品ID大于0，则缓存1天
            if (searchLog.getPtmProductId() > 0) {
                cacheService.add(key, searchLog, TimeUtils.SECONDS_OF_1_HOUR * 24);
            } else {
                cacheService.add(key, searchLog, CACHE_EXPIRE_TIME);
            }

            countSearchedProduct(searchLog.getPtmProductId());
        }

        return searchLog;
    }

    public void countSearchedProduct(long proId) {
        Date date = TimeUtils.nowDate();

        String logCountMap = "LOG_COUNT_" + TimeUtils.parse(date, "yyyyMMdd");

        String mapKey = String.valueOf(proId);

        boolean exist = cacheService.exists(logCountMap);
        if (!exist) {
            cacheService.mapPut(logCountMap, mapKey, "1");
            cacheService.expire(logCountMap, TimeUtils.SECONDS_OF_1_DAY);
            return;
        }

        String countStr = cacheService.mapGet(logCountMap, mapKey);

        if (StringUtils.isEmpty(countStr)) {
            cacheService.mapPut(logCountMap, mapKey, "1");
        } else {
            long count = Long.valueOf(countStr);
            cacheService.mapPut(logCountMap, mapKey, String.valueOf(count + 1));
        }
    }

    public Map<String, Long> getSearchLogCount(String ymd) {

        Map<String, Long> countMap = new HashMap<String, Long>();

        String logCountMap = "LOG_COUNT_" + ymd;

        boolean exist = cacheService.exists(logCountMap);
        if (!exist) {
            return countMap;
        }

        Map<String, String> countStrMap = cacheService.mapGetAll(logCountMap);

        for (Map.Entry<String, String> kv : countStrMap.entrySet()) {
            String key = kv.getKey();
            String countStr = kv.getValue();
            long count = Long.valueOf(countStr);
            countMap.put(key, count);
        }

        return countMap;
    }
}
