package hasoffer.core.cache;

import com.alibaba.fastjson.JSON;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.product.SkuUpdateResult;
import hasoffer.core.bo.product.SkuUpdateResult2;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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

    private Logger logger = LoggerFactory.getLogger(SearchLogCacheManager.class);

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

    public SrmSearchLog findSrmSearchLog(String logId, boolean addCount) {
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

            if (addCount) {
                countSearchedProduct(searchLog.getPtmProductId());
                countSearchedProductByHour(searchLog.getPtmProductId());
            }
        }

        return searchLog;
    }

    public void countSearchedProduct(long proId) {
        logger.debug("product id = " + proId);
        if (proId <= 0) {
            return;
        }
        // 业务需求：当前时间减10小时 - out date
//        Date date = TimeUtils.add(TimeUtils.nowDate(), -1 * TimeUtils.MILLISECONDS_OF_1_HOUR * 10);
        Date date = TimeUtils.nowDate();

        String logCountMap = "LOG_COUNT_" + TimeUtils.parse(date, "yyyyMMdd");

        String key = String.valueOf(proId);

        boolean exist = cacheService.exists(logCountMap);
        if (!exist) {
            cacheService.mapPut(logCountMap, key, "1");
            cacheService.expire(logCountMap, TimeUtils.SECONDS_OF_1_DAY * 2);
            return;
        }

        String countStr = cacheService.mapGet(logCountMap, key);

        if (StringUtils.isEmpty(countStr)) {
            cacheService.mapPut(logCountMap, key, "1");
        } else {
            long count = Long.valueOf(countStr);
            cacheService.mapPut(logCountMap, key, String.valueOf(count + 1));
        }
    }

    public void countSearchedProductByHour(long proId) {
        logger.debug("product id = " + proId);
        if (proId <= 0) {
            return;
        }

        Date date = TimeUtils.nowDate();

        String logCountMap = "LOG_COUNT_" + TimeUtils.parse(date, "yyyyMMdd_HH");

        String key = String.valueOf(proId);

        boolean exist = cacheService.exists(logCountMap);
        if (!exist) {
            cacheService.mapPut(logCountMap, key, "1");
            cacheService.expire(logCountMap, TimeUtils.SECONDS_OF_1_DAY * 1);
            return;
        }

        String countStr = cacheService.mapGet(logCountMap, key);

        if (StringUtils.isEmpty(countStr)) {
            cacheService.mapPut(logCountMap, key, "1");
        } else {
            long count = Long.valueOf(countStr);
            cacheService.mapPut(logCountMap, key, String.valueOf(count + 1));
        }
    }

    public Map<Long, Long> getProductCountByHour(String ymd_hour) {

        Map<Long, Long> countMap = new HashMap<Long, Long>();

        String logCountMap = "LOG_COUNT_" + ymd_hour;

        boolean exist = cacheService.exists(logCountMap);
        if (!exist) {
            return countMap;
        }

        Map<String, String> countStrMap = cacheService.mapGetAll(logCountMap);

        for (Map.Entry<String, String> kv : countStrMap.entrySet()) {
            String proIdStr = kv.getKey();
            String countStr = kv.getValue();
            long proId = Long.valueOf(proIdStr);
            long count = Long.valueOf(countStr);
            countMap.put(proId, count);
        }

        return countMap;
    }

    public Map<Long, Long> getProductCount(String ymd) {

        Map<Long, Long> countMap = new HashMap<Long, Long>();

        String logCountMap = "LOG_COUNT_" + ymd;

        boolean exist = cacheService.exists(logCountMap);
        if (!exist) {
            return countMap;
        }

        Map<String, String> countStrMap = cacheService.mapGetAll(logCountMap);

        for (Map.Entry<String, String> kv : countStrMap.entrySet()) {
            String proIdStr = kv.getKey();
            String countStr = kv.getValue();
            long proId = Long.valueOf(proIdStr);
            long count = Long.valueOf(countStr);
            countMap.put(proId, count);
        }

        return countMap;
    }

    public void delCache(String searchLogId) {

        String key = CACHE_KEY_PRE + searchLogId;

        cacheService.del(key);
    }

    public void cacheStatResult(SkuUpdateResult skuUpdateResult) {
        String ymd = skuUpdateResult.getYmd();

        if (ymd.indexOf("_") > 0) {
            ymd = ymd.substring(0, ymd.indexOf("_"));
        }

        String key = CACHE_KEY_PRE + "cacheStatResult_" + ymd;

        cacheService.mapPut(key, skuUpdateResult.getYmd(), JSON.toJSONString(skuUpdateResult));
    }

    public List<SkuUpdateResult2> getStatResults(String ymd) {
        String key = CACHE_KEY_PRE + "cacheStatResult_" + ymd;

        Map<String, String> datas = cacheService.mapGetAll(key);

        List<SkuUpdateResult2> skuUpdateResults = new ArrayList<>();

        for (Map.Entry<String, String> data : datas.entrySet()) {
            SkuUpdateResult2 skuUpdateResult = JSON.parseObject(data.getValue(), SkuUpdateResult2.class);
            skuUpdateResults.add(skuUpdateResult);
        }

        Collections.sort(skuUpdateResults, new Comparator<SkuUpdateResult2>() {
            @Override
            public int compare(SkuUpdateResult2 o1, SkuUpdateResult2 o2) {
                return o1.getYmd().compareTo(o2.getYmd());
            }
        });

        return skuUpdateResults;
    }
}
