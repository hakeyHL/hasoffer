package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
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
    public void sendDealTask(Website website, TaskLevel taskLevel) {

        //获取Deal等待队列的key名称
        String redisKey = RedisKeysUtils.getWaitDealList(taskLevel, website);
        System.out.println("wait deal redis key :" + redisKey);

        //封装结果对象
        FetchDealResult fetchDealResult = new FetchDealResult();
        fetchDealResult.setWebsite(website);
        fetchDealResult.setTaskStatus(TaskStatus.START);

        fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchDealResult));
    }

    @Override
    public FetchDealResult getDealInfo(Website website) {
        String result = fetchCacheService.popTaskList(RedisKeysUtils.getDealwebsiteFetchResultKey(website));

        if (StringUtils.isEmpty(result)) {
            return null;
        } else {
            try {
                return JSONUtil.toObject(result, FetchDealResult.class);
            } catch (IOException e) {
                logger.debug("dealresult string to json error");
                return null;
            }
        }
    }


    @Override
    public FetchResult getProductsKeyWord(Website webSite, String keyword) {
        FetchResult fetchResult = getFetchResultList(webSite, keyword);
        logger.info("FetchDubboServiceImpl.getProductsKeyWord(webSite,url):{}, {} . Now is {} ", webSite, keyword, fetchResult);
        return fetchResult;
    }

    @Override
    public void sendCompareWebsiteFetchTask(Website website, String url, TaskLevel taskLevel, long categoryId) {

        //先检查解析过的set中是否含有该url，如果有跳过，如果没有新增
        boolean flag = fetchCacheService.checkCompareWebsiteFetch(RedisKeysUtils.PARSED_COMPAREWEBSITE_FETCH_URL, url);

        if (!flag) {
            return;
        }

        //获取等待队列的名称
        String redisKey = RedisKeysUtils.getWaitCompareWebsiteFetchList(taskLevel, website);
        System.out.println("wait compare website task redis key :" + redisKey);

        //封装结果对象
        FetchCompareWebsiteResult fetchCompareWebsiteResult = new FetchCompareWebsiteResult();
        fetchCompareWebsiteResult.setWebsite(website);
        fetchCompareWebsiteResult.setUrl(url);
        fetchCompareWebsiteResult.setTaskStatus(TaskStatus.START);
        fetchCompareWebsiteResult.setCategoryId(categoryId);

        fetchCacheService.pushTaskList(redisKey, JSONUtil.toJSON(fetchCompareWebsiteResult));
    }

    @Override
    public FetchCompareWebsiteResult getCompareWebsiteFetchResult(Website webSite) {
        return fetchCacheService.popFetchCompareWebsite(RedisKeysUtils.getComparewebsiteFetchResultKey(webSite));
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
    public void sendUrlTask(Website website, String url, TaskTarget taskTarget, TaskLevel taskLevel) {

        try {
            String key = FetchUrlResult.getCacheKey(website, url);
            if (key == null) {
                logger.info("sendUrlTask(): key is null.website:{}, url:{}", website, url);
                return;
            }
            FetchUrlResult fetchUrlResult = new FetchUrlResult(website, url, TaskStatus.START, new Date(), taskTarget);
            fetchCacheService.pushTaskList(taskLevel, fetchUrlResult);
        } catch (Exception e) {
            SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendUrlTask(fetchUrlResult) save {} into Redis List {} fail", website + "_" + url, e);
        }
    }

    @Override
    public void sendUrlTask(Website website, String url, TaskTarget taskTarget, TaskLevel taskLevel, long id) {
        try {
            String key = FetchUrlResult.getCacheKey(website, url);
            if (key == null) {
                logger.info("sendUrlTask(): key is null.website:{}, url:{}", website, url);
                return;
            }
            FetchUrlResult fetchUrlResult = new FetchUrlResult(website, url, TaskStatus.START, new Date(), taskTarget);
            fetchUrlResult.setSkuId(id);
            fetchCacheService.pushTaskList(taskLevel, fetchUrlResult);
        } catch (Exception e) {
            SpiderLogger.debugSpiderUrl("FetchDubboServiceImpl.sendUrlTask(fetchUrlResult) save {} into Redis List {} fail", website + "_" + url, e);
        }
    }

    @Override
    public String popFetchUrlResult(TaskTarget taskTarget) {
        String fetchUrlResult = fetchCacheService.popFinishUrlList(taskTarget);
        logger.info("popFetchUrlResult(), obj:{}", fetchUrlResult);
        return fetchUrlResult;
    }

    private FetchResult getFetchResultList(Website webSite, String keyWord) {
        String fetchResultKey = FetchResult.getCacheKey(webSite, keyWord);
        return fetchCacheService.getResultByKeyword(fetchResultKey);

    }


}
