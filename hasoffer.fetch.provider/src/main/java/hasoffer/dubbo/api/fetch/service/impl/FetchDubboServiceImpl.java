package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.common.RedisKeysConstant;
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
    public FetchUrlResult getProductsByUrl(Long skuId, Website webSite, String url) {

        FetchUrlResult fetchUrlResult = getFetchUrlResult(webSite, url);
        logger.info("FetchDubboServiceImpl.getProductsByUrl(webSite,url):{}, {} . Now is {} ", webSite, url, fetchUrlResult);
        return fetchUrlResult;
    }

    @Override
    public void sendKeyWordTask(Website website, String keyword) {
        FetchResult fetchResult = new FetchResult(website, keyword);
        fetchResult.setTaskStatus(TaskStatus.START);
        addFetchTask(fetchResult);
    }

    @Override
    public TaskStatus getKeyWordTaskStatus(Website webSite, String keyword) {
        String cacheKey = FetchResult.getCacheKey(webSite, keyword);
        return fetchCacheService.getTaskStatusByKeyword(cacheKey);
    }

    @Override
    public void sendUrlTask(Website website, String url) {
        FetchUrlResult fetchUrlResult = new FetchUrlResult(website, url);
        fetchUrlResult.setTaskStatus(TaskStatus.START);
        fetchUrlResult.setDate(new Date());
        addFetchUrlTask(fetchUrlResult);
    }

    @Override
    public TaskStatus getUrlTaskStatus(Website website, String url) {
        String cacheKey = FetchUrlResult.getCacheKey(website, url);
        return fetchCacheService.getTaskStatusByUrl(cacheKey);
    }

    private void addFetchTask(FetchResult fetchResult) {
        try {
            String key = FetchResult.getCacheKey(fetchResult);
            if (key == null) {
                return;
            }
            fetchCacheService.pushTaskList(RedisKeysConstant.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
            //fetchCacheService.cacheResult(key, fetchResult);
            fetchCacheService.setTaskStatusByUrl(key, TaskStatus.START);
            logger.info("FetchDubboServiceImpl.addFetchTask(FetchResult fetchResult) save {} into Redis List {}", fetchResult.getWebsite() + "_" + fetchResult.getKeyword(), RedisKeysConstant.WAIT_KEY_LIST);
        } catch (Exception e) {
            logger.error("FetchDubboServiceImpl.addFetchTask(FetchResult fetchResult) save {} into Redis List {} fail", fetchResult.getWebsite() + "_" + fetchResult.getKeyword(), RedisKeysConstant.WAIT_KEY_LIST, e);
        }
    }

    private FetchResult getFetchResultList(Website webSite, String keyWord) {
        String fetchResultKey = FetchResult.getCacheKey(webSite, keyWord);
        return fetchCacheService.getResultByKeyword(fetchResultKey);

    }

    private void addFetchUrlTask(FetchUrlResult fetchUrlResult) {
        try {
            String key = FetchUrlResult.getCacheKey(fetchUrlResult);
            if (key == null) {
                return;
            }
            fetchCacheService.pushTaskList(RedisKeysConstant.WAIT_URL_LIST, JSONUtil.toJSON(fetchUrlResult));
            logger.info("FetchDubboServiceImpl.addFetchUrlTask(fetchUrlResult) save {} into Redis List {}", fetchUrlResult.getUrl(), RedisKeysConstant.WAIT_URL_LIST);
            //fetchCacheService.cacheResult(key, fetchUrlResult);
            fetchCacheService.setTaskStatusByUrl(key, TaskStatus.START);
        } catch (Exception e) {
            logger.error("FetchDubboServiceImpl.addFetchUrlTask(fetchUrlResult) save {} into Redis List {} fail", fetchUrlResult.getUrl(), RedisKeysConstant.WAIT_URL_LIST, e);

        }
    }

    private FetchUrlResult getFetchUrlResult(Website webSite, String url) {
        String fetchResultKey = FetchUrlResult.getCacheKey(webSite, url);
        return fetchCacheService.getProductByUrl(fetchResultKey);
    }

}
