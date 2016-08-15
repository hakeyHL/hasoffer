package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.IFetchService;
import hasoffer.spider.api.impl.FetchServiceImpl;
import hasoffer.spider.common.RedisKeysUtils;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FetchKeywordWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchKeywordWorker.class);

    private IFetchCacheService fetchCacheService;

    private IFetchService fetchService = new FetchServiceImpl();

    public FetchKeywordWorker(WebApplicationContext springContext) {
        fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
    }

    @Override
    public void run() {
        while (true) {
            Object pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_1));
            if (pop == null) {
                pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_2));
            }
            if (pop == null) {
                pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_3));
            }
            if (pop == null) {
                pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_4));
            }
            if (pop == null) {
                pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_5));
            }
            if (pop == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("FetchKeywordWorker at {} , pop word: {}", new Date(), pop);
                FetchResult fetchResult = null;
                try {
                    fetchResult = JSONUtil.toObject(pop.toString(), FetchResult.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fetch(fetchResult);
            }


        }
    }

    public void fetch(FetchResult fetchResult) {
        if (fetchResult == null) {
            return;
        }
        try {
            fetchResult = fetchService.getProductSetByKeyword(fetchResult, 10);
        } catch (UnSupportWebsiteException e) {
            logger.error("don't support this website.", e);
            e.printStackTrace();
        }
        logger.info("Fetch Success:website:{}, Key :{}, success:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), fetchResult.getFetchProducts().size());
        fetchCacheService.cacheResult(FetchResult.getCacheKey(fetchResult), fetchResult);
    }

}
