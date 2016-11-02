package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.logger.SpiderLogger;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedDealInfo;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class FetchDubboServiceImpl implements IFetchDubboService {

    private Logger logger = LoggerFactory.getLogger(FetchDubboServiceImpl.class);

    @Resource
    private IFetchCacheService fetchCacheService;

    @Override
    public void sendDealTask(Website website, long cacheSeconds, TaskLevel taskLevel) {

        String redisKey = RedisKeysUtils.getWaitUrlListKey(taskLevel, website);

        FetchDealResult fetchDealResult = new FetchDealResult();
        fetchDealResult.setWebsite(website);
        fetchDealResult.setTaskStatus(TaskStatus.START);
        fetchDealResult.setExpireSeconds(cacheSeconds);

        try {
            String key = FetchDealResult.getCacheKey(fetchDealResult);
            if (key == null) {
                return;
            }
            TaskStatus dealTaskStatus = fetchCacheService.getDealTaskStatus(key);
            if (TaskStatus.NONE.equals(dealTaskStatus)) {
                fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchDealResult));
                SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendDealTask(fetchDealResult) save {} into Redis List {} success", fetchDealResult.getWebsite(), redisKey);
                fetchCacheService.setTaskStatusByUrl(key, TaskStatus.START);
            }
        } catch (Exception e) {
            SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendDealTask(fetchDealResult) save {} into Redis List {} fail", fetchDealResult.getWebsite(), redisKey, e);
        }
    }

    @Override
    public TaskStatus getDealTaskStatus(Website website, long expireSeconds) {
        String cacheKey = FetchDealResult.getCacheKey(website, expireSeconds);
        TaskStatus taskStatusByUrl = fetchCacheService.getTaskStatusByUrl(cacheKey);
        return taskStatusByUrl;
    }

    @Override
    public List<FetchedDealInfo> getDesidimeDealInfo() {
        return null;
    }

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
        String redisKey = RedisKeysUtils.getWaitUrlListKey(taskLevel, website);
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
            }
        } catch (Exception e) {
            SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendUrlTask(fetchUrlResult) save {} into Redis List {} fail", fetchUrlResult.getWebsite() + "_" + fetchUrlResult.getUrl(), redisKey, e);
        }
    }

    @Override
    public TaskStatus getUrlTaskStatus(Website website, String url, long expireSeconds) {
        String cacheKey = FetchUrlResult.getCacheKey(website, url, expireSeconds);
        TaskStatus taskStatusByUrl = fetchCacheService.getTaskStatusByUrl(cacheKey);
        //SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.getUrlTaskStatus(website,url,expireSeconds) -->website:{}, url:{}, taskState:{}, expireSeconds:{}", website, url, taskStatusByUrl, expireSeconds);
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
