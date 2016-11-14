package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.ISpiderService;
import hasoffer.spider.api.impl.SpiderServiceImpl;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.logger.SpiderLogger;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * Created  on 2016/11/9.
 */
public class FetchCompareWebsiteTaskWorker implements Runnable {
    private Logger logger = LoggerFactory.getLogger(FetchCompareWebsiteTaskWorker.class);

    private IFetchCacheService fetchCacheService;

    private Website website;

    private ISpiderService fetchService = new SpiderServiceImpl();

    public FetchCompareWebsiteTaskWorker(WebApplicationContext springContext, Website website) {
        fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
        this.website = website;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitCompareWebsiteFetchList(TaskLevel.LEVEL_1, website));
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitCompareWebsiteFetchList(TaskLevel.LEVEL_2, website));
                }
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitCompareWebsiteFetchList(TaskLevel.LEVEL_3, website));
                }
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitCompareWebsiteFetchList(TaskLevel.LEVEL_4, website));
                }
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitCompareWebsiteFetchList(TaskLevel.LEVEL_5, website));
                }
                if (pop == null) {
                    TimeUnit.MINUTES.sleep(1);
                } else {
                    SpiderLogger.infoFetchFlow("start spider this url: {}", pop);
                    FetchCompareWebsiteResult fetchCompareWebsiteResult = JSONUtil.toObject(pop.toString(), FetchCompareWebsiteResult.class);

                    fetch(fetchCompareWebsiteResult);
                    SpiderLogger.infoFetchFlow("Finish spider this url: {}", pop);
                }
            } catch (Exception e) {
                logger.error("FetchCompareWebsiteTaskWorker is error. Error Msg: Json to Object fail.", e);
            }
        }
    }

    public void fetch(FetchCompareWebsiteResult fetchCompareWebsiteResult) {
        try {
            fetchService.spiderCompareWebsite(fetchCompareWebsiteResult);
        } catch (UnSupportWebsiteException e) {
            fetchCompareWebsiteResult.setTaskStatus(TaskStatus.STOPPED);
            fetchCacheService.pushCompareWebsiteFetchResultToFinishList(RedisKeysUtils.getComparewebsiteFetchResultKey(fetchCompareWebsiteResult.getWebsite()), JSONUtil.toJSON(fetchCompareWebsiteResult));
        }
    }
}