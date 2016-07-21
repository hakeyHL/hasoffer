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
        if (fetchUrlResult == null) {
            fetchUrlResult = new FetchUrlResult(webSite, url);
            fetchUrlResult.setTaskStatus(TaskStatus.START);
            addFetchUrlTask(fetchUrlResult);
        }

        return fetchUrlResult;
    }

    private void addFetchTask(FetchResult fetchResult) {
        String key = FetchResult.getCacheKey(fetchResult);
        fetchCacheService.saveKeywordList(StringConstant.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
        fetchCacheService.cacheResult(key, fetchResult);
    }

    private FetchResult getFetchResultList(Website webSite, String keyWord) {
        String fetchResultKey = FetchResult.getCacheKey(webSite, keyWord);
        return fetchCacheService.getResultByKey(fetchResultKey);

    }

    private void addFetchUrlTask(FetchUrlResult fetchUrlResult) {
        String key = FetchUrlResult.getCacheKey(fetchUrlResult);
        if (key == null) {
            return;
        }
        fetchCacheService.saveKeywordList(StringConstant.WAIT_URL_LIST, JSONUtil.toJSON(fetchUrlResult));
        fetchCacheService.cacheResult(key, fetchUrlResult);

    }

    private FetchUrlResult getFetchUrlResult(Website webSite, String url) {
        String fetchResultKey = FetchUrlResult.getCacheKey(webSite, url);
        return fetchCacheService.getProductByUrl(fetchResultKey);
    }


}
