package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.logger.SpiderLogger;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

public class FetchDubboServiceImpl implements IFetchDubboService {

    private Logger logger = LoggerFactory.getLogger(FetchDubboServiceImpl.class);

    @Resource
    private IFetchCacheService fetchCacheService;

    @Override
    public void sendDealTask(Website website, long cacheSeconds, TaskLevel taskLevel) {

        //获取Deal等待队列的key名称
        String redisKey = RedisKeysUtils.getWaitDealList(taskLevel, website);
        System.out.println("wait deal redis key :" + redisKey);

        //封装结果对象
        FetchDealResult fetchDealResult = new FetchDealResult();
        fetchDealResult.setWebsite(website);
        fetchDealResult.setTaskStatus(TaskStatus.START);
        fetchDealResult.setExpireSeconds(cacheSeconds);

        try {
            //获取缓存数据信息的key名称，该名称为状态map中存放该任务状态的key
            String key = FetchDealResult.getCacheKey(fetchDealResult);
            if (key == null) {
                return;
            }
            //获取状态map中的该key的状态信息
            //如果已经存在该数据，不执行任何操作
            //如果没有该数据，那么将该数据加入wait队列，并且添加状态信息为start到map中
            TaskStatus dealTaskStatus = fetchCacheService.getDealTaskStatus(key);
            if (TaskStatus.NONE.equals(dealTaskStatus)) {
                fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchDealResult));
                SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendDealTask(fetchDealResult) save {} into Redis List {} success", fetchDealResult.getWebsite(), redisKey);
                fetchCacheService.setDealTaskStatus(key, TaskStatus.START);
            }
        } catch (Exception e) {
            SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendDealTask(fetchDealResult) save {} into Redis List {} fail", fetchDealResult.getWebsite(), redisKey, e);
        }
    }

    @Override
    public TaskStatus getDealTaskStatus(Website website, long expireSeconds, TaskLevel taskLevel) {
        //获取该任务的key，查看该任务的状态
        String cacheKey = FetchDealResult.getCacheKey(website, expireSeconds);
        TaskStatus taskStatusByUrl = fetchCacheService.getDealTaskStatus(cacheKey);
        return taskStatusByUrl;
    }

    @Override
    public FetchDealResult getDealInfo(Website website, long expireSeconds, TaskLevel taskLevel) {
        String cacheKey = FetchDealResult.getCacheKey(website, expireSeconds);
        //获取该任务的响应数据
        FetchDealResult fetchDealResult = fetchCacheService.getDealInfo(cacheKey);
        return fetchDealResult;
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
    public void sendCompareWebsiteFetchTask(Website website, String url, TaskLevel taskLevel, long cacheSeconds) {

        //先检查解析过的set中是否含有该url，如果有跳过，如果没有新增
        boolean flag = fetchCacheService.checkCompareWebsiteFetch(RedisKeysUtils.PARSED_COMPAREWEBSITE_FETCH_URL, url, cacheSeconds);

        if (!flag) {
            return;
        }

        //获取等待队列的名称
        String redisKey = RedisKeysUtils.getWaitCompareWebsiteFetchList(taskLevel, website);
        System.out.println("wait compare website task redis key :" + redisKey);

        //封装结果对象
        FetchCompareWebsiteResult fetchCompareWebsiteResult = new FetchCompareWebsiteResult();
        fetchCompareWebsiteResult.setOriWebsite(website);
        fetchCompareWebsiteResult.setCacheSeconds(cacheSeconds);
        fetchCompareWebsiteResult.setUrl(url);

        fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchCompareWebsiteResult));
    }

    @Override
    public FetchCompareWebsiteResult getCompareWebsiteFetchResult(Website webSite, String url, long expireSeconds) {
        return null;
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
    public void sendUrlTask(Website website, String url, Long expireSeconds, TaskTarget taskTarget, TaskLevel taskLevel) {
        if (expireSeconds == null) {
            expireSeconds = TimeUtils.SECONDS_OF_1_DAY;
        }

        fetchCacheService.pushNum(website.name() + "_" + taskTarget.name());
        fetchCacheService.countPushUrl(website.name() + "_" + taskTarget.name(), url);
        FetchUrlResult fetchUrlResult = new FetchUrlResult(website, url, expireSeconds, TaskStatus.START, new Date(), taskTarget);
        String redisKey = RedisKeysUtils.getWaitUrlListKey(taskLevel, website);
        try {
            String key = FetchUrlResult.getCacheKey(fetchUrlResult);
            if (key == null) {
                logger.info("key is null.website:{}, url:{}", website, url);
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
    public String popFetchUrlResult(TaskTarget taskTarget) {
        String fetchUrlResult = fetchCacheService.popFinishUrlList(taskTarget);
        if (fetchUrlResult != null) {

            try {
                FetchUrlResult result = JSONUtil.toObject(fetchUrlResult, FetchUrlResult.class);
                fetchCacheService.popNum(result.getWebsite() + "_" + taskTarget + "_" + result.getTaskStatus());
            } catch (IOException e) {
                logger.error("Json:{}", fetchUrlResult, e);
            }
        }
        logger.info("popFetchUrlResult(), obj:{}", fetchUrlResult);
        return fetchUrlResult;
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
