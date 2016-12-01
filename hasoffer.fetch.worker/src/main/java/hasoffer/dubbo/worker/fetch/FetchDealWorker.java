package hasoffer.dubbo.worker.fetch;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.ISpiderService;
import hasoffer.spider.api.impl.SpiderServiceImpl;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.logger.SpiderLogger;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/1.
 */
public class FetchDealWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchDealWorker.class);

    private IFetchCacheService fetchCacheService;

    private Website website;

    private ISpiderService fetchService = new SpiderServiceImpl();

    public FetchDealWorker(WebApplicationContext springContext, Website website) {
        fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
        this.website = website;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitDealList(TaskLevel.LEVEL_1, website));
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitDealList(TaskLevel.LEVEL_2, website));
                }
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitDealList(TaskLevel.LEVEL_3, website));
                }
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitDealList(TaskLevel.LEVEL_4, website));
                }
                if (pop == null) {
                    pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitDealList(TaskLevel.LEVEL_5, website));
                }
                if (pop == null) {
                    TimeUnit.MINUTES.sleep(1);
                } else {
                    SpiderLogger.infoFetchFlow("start spider this url: {}", pop);
                    FetchDealResult fetchDealResult = JSONUtil.toObject(pop.toString(), FetchDealResult.class);
                    fetch(fetchDealResult);
                    if (fetchDealResult.overFetch()) {
                        logger.info("FetchDealWorker crawl finish: {} ", fetchDealResult);
                    } else {
                        logger.info("FetchDealWorker crawl running: {} ", fetchDealResult);
                    }
                    SpiderLogger.infoFetchFlow("Finish spider this url: {}", pop);
                }
            } catch (Exception e) {
                logger.error("FetchDealWorker is error. Error Msg: Json to Object fail.", e);
            }
        }
    }

    public void fetch(FetchDealResult fetchDealResult) {
        try {
            fetchService.spiderDealInfo(fetchDealResult);
        } catch (UnSupportWebsiteException e) {
            fetchDealResult.setTaskStatus(TaskStatus.STOPPED);
            fetchDealResult.setErrMsg("un able support website.");
            String resultKey = RedisKeysUtils.getDealwebsiteFetchResultKey(fetchDealResult.getWebsite());
            fetchCacheService.pushDealWebsiteFetchResultToFinishList(resultKey, JSONUtil.toJSON(fetchDealResult));
            logger.error("FetchKeywordWorker is error. Error Msg: un able support website.", e);
        }
    }
}
