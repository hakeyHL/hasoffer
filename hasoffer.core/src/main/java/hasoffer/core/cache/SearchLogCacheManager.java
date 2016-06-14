package hasoffer.core.cache;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.search.ISearchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    public SrmSearchLog updateSrmSearchLog(String logId) {

        String key = CACHE_KEY_PRE + logId;

        SrmSearchLog searchLog = cacheService.get(SrmSearchLog.class, key, 0);

        if (searchLog == null) {
            searchLog = searchService.findSrmSearchLogById(logId);
        }

        if (searchLog != null) {
            searchLog.setCount(searchLog.getCount() + 1);
            cacheService.add(key, searchLog, CACHE_EXPIRE_TIME);
        }

        return searchLog;
    }
}
