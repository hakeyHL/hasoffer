package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.common.StringConstant;
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
    public FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex) {
        FetchResult fetchResult = getFetchResultList(webSite, keyword);
        if (fetchResult == null) {
            fetchResult = new FetchResult(webSite, keyword);
            fetchResult.setTaskStatus(TaskStatus.START);
            addFetchTask(fetchResult);
        }
        return fetchResult;
    }


    @Override
    public FetchUrlResult getProductsByUrl(Website webSite, String url) throws HttpFetchException, ContentParseException {

        FetchUrlResult fetchUrlResult = getFetchUrlResult(webSite, url);
        logger.info("FetchDubboServiceImpl.getProductsByUrl(webSite,url):{}, {} . Now is {} ", webSite, url, fetchUrlResult);
        if (fetchUrlResult == null) {
            fetchUrlResult = new FetchUrlResult(webSite, url);
            fetchUrlResult.setTaskStatus(TaskStatus.START);
            fetchUrlResult.setDate(new Date());
            addFetchUrlTask(fetchUrlResult);
        }

        return fetchUrlResult;
    }

    private void addFetchTask(FetchResult fetchResult) {
        String key = FetchResult.getCacheKey(fetchResult);
        fetchCacheService.pushTaskList(StringConstant.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
        logger.info("FetchDubboServiceImpl.addFetchTask(FetchResult fetchResult) save {} into Redis List {}", fetchResult.getWebsite() + "_" + fetchResult.getKeyword(), StringConstant.WAIT_KEY_LIST);
        fetchCacheService.cacheResult(key, fetchResult);
    }

    private FetchResult getFetchResultList(Website webSite, String keyWord) {
        String fetchResultKey = FetchResult.getCacheKey(webSite, keyWord);
        return fetchCacheService.getResultByKey(fetchResultKey);

    }

    private void addFetchUrlTask(FetchUrlResult fetchUrlResult) {
        logger.info("FetchDubboServiceImpl.addFetchUrlTask(fetchUrlResult) param={} " + fetchUrlResult.getUrl());
        String key = FetchUrlResult.getCacheKey(fetchUrlResult);
        if (key == null) {
            return;
        }
        fetchCacheService.pushTaskList(StringConstant.WAIT_URL_LIST, JSONUtil.toJSON(fetchUrlResult));
        logger.info("FetchDubboServiceImpl.addFetchUrlTask(fetchUrlResult) save {} into Redis List {}", fetchUrlResult.getUrl(), StringConstant.WAIT_URL_LIST);
        fetchCacheService.cacheResult(key, fetchUrlResult);

    }

    private FetchUrlResult getFetchUrlResult(Website webSite, String url) {
        String fetchResultKey = FetchUrlResult.getCacheKey(webSite, url);
        return fetchCacheService.getProductByUrl(fetchResultKey);
    }

}
