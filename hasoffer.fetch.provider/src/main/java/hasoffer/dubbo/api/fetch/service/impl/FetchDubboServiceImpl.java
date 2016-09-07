package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.common.SpiderLogger;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;

public class FetchDubboServiceImpl implements IFetchDubboService {

    private Logger logger = LoggerFactory.getLogger(FetchDubboServiceImpl.class);

    @Resource
    private IFetchCacheService fetchCacheService;

    @Override
    public FetchResult getProductsKeyWord(Website webSite, String keyword) {
        FetchResult fetchResult = getFetchResultList(webSite, keyword);
        logger.info("FetchDubboServiceImpl.getProductsKeyWord(webSite,url):{}, {} . Now is {} ", webSite, keyword, fetchResult);
        return fetchResult;
    }


    @Override
    public FetchUrlResult getProductsByUrl(Website webSite, String url, long expireSeconds) {
        FetchUrlResult fetchUrlResult = getFetchUrlResult(webSite, url, expireSeconds);
        logger.info("FetchDubboServiceImpl.getProductsByUrl(webSite,url):{}, {} . Now is {} ", webSite, url, fetchUrlResult);
        return fetchUrlResult;
    }

    @Override
    public void sendKeyWordTask(Website website, String keyword) {
        FetchResult fetchResult = new FetchResult(website, keyword);
        fetchResult.setTaskStatus(TaskStatus.START);
        try {
            String key = FetchResult.getCacheKey(fetchResult);
            if (key == null) {
                return;
            }
            fetchCacheService.pushTaskList(RedisKeysUtils.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
            fetchCacheService.setTaskStatusByKeyword(key, TaskStatus.START);
            SpiderLogger.debugSearchList("FetchDubboServiceImpl.sendKeyWordTask(FetchResult fetchResult) save {} into Redis List {} success", fetchResult.getWebsite() + "_" + fetchResult.getKeyword(), RedisKeysUtils.WAIT_KEY_LIST);
        } catch (Exception e) {
            SpiderLogger.debugSearchList("FetchDubboServiceImpl.sendKeyWordTask(FetchResult fetchResult) save {} into Redis List {} fail", fetchResult.getWebsite() + "_" + fetchResult.getKeyword(), RedisKeysUtils.WAIT_KEY_LIST, e);
        }

    }

    @Override
    public TaskStatus getKeyWordTaskStatus(Website webSite, String keyword) {
        String cacheKey = FetchResult.getCacheKey(webSite, keyword);
        return fetchCacheService.getTaskStatusByKeyword(cacheKey);
    }

    @Override
    public void sendUrlTask(Website website, String url) {
        sendUrlTask(website, url, TaskLevel.LEVEL_5);
    }

    @Override
    public void sendUrlTask(Website website, String url, TaskLevel taskLevel) {
        sendUrlTask(website, url, TimeUtils.SECONDS_OF_1_DAY, taskLevel);
    }

    @Override
    public void sendUrlTask(Website website, String url, long seconds, TaskLevel taskLevel) {
        FetchUrlResult fetchUrlResult = new FetchUrlResult(website, url, seconds);
        fetchUrlResult.setTaskStatus(TaskStatus.START);
        fetchUrlResult.setDate(new Date());
        String redisKey = RedisKeysUtils.getWaitUrlListKey(taskLevel);
        try {
            String key = FetchUrlResult.getCacheKey(fetchUrlResult);
            if (key == null) {
                return;
            }
            TaskStatus taskStatusByUrl = fetchCacheService.getTaskStatusByUrl(key);
            if (TaskStatus.NONE.equals(taskStatusByUrl)) {
                fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchUrlResult));
                SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendUrlTask(fetchUrlResult) save {} into Redis List {} success", fetchUrlResult.getWebsite() + "_" + fetchUrlResult.getUrl(), redisKey);
                fetchCacheService.setTaskStatusByUrl(key, TaskStatus.START);
            } else if (TaskStatus.FINISH.equals(taskStatusByUrl)) {
                FetchUrlResult temp = getFetchUrlResult(website, url, seconds);
                if (temp == null || temp.getTaskStatus().equals(TaskStatus.EXCEPTION)) {
                    fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchUrlResult));
                    SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendUrlTask(fetchUrlResult) save {} into Redis List {} success", fetchUrlResult.getWebsite() + "_" + fetchUrlResult.getUrl(), redisKey);
                    fetchCacheService.setTaskStatusByUrl(key, TaskStatus.START);
                }
            }
        } catch (Exception e) {
            SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendUrlTask(fetchUrlResult) save {} into Redis List {} fail", fetchUrlResult.getWebsite() + "_" + fetchUrlResult.getUrl(), redisKey, e);
        }
    }

    @Override
    public TaskStatus getUrlTaskStatus(Website website, String url, long expireSeconds) {
        String cacheKey = FetchUrlResult.getCacheKey(website, url, expireSeconds);
        TaskStatus taskStatusByUrl = fetchCacheService.getTaskStatusByUrl(cacheKey);
        SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.getUrlTaskStatus(website,url) -->website:{}, url:{}, taskState:{}", website, url, taskStatusByUrl);
        return taskStatusByUrl;
    }

    private FetchResult getFetchResultList(Website webSite, String keyWord) {
        String fetchResultKey = FetchResult.getCacheKey(webSite, keyWord);
        return fetchCacheService.getResultByKeyword(fetchResultKey);

    }


    private FetchUrlResult getFetchUrlResult(Website webSite, String url, long expireSeconds) {
        String fetchResultKey = FetchUrlResult.getCacheKey(webSite, url, expireSeconds);
        return fetchCacheService.getProductByUrl(fetchResultKey);
    }

}
