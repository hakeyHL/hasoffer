package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.ISpiderService;
import hasoffer.spider.api.impl.SpiderServiceImpl;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.logger.SpiderLogger;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

public class FetchUrlWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchUrlWorker.class);

    private IFetchCacheService fetchCacheService;

    private Website website;

    private ISpiderService fetchService = new SpiderServiceImpl();

    public FetchUrlWorker(WebApplicationContext springContext, Website website) {
        this.website = website;
        this.fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object pop = null;
                for (TaskLevel taskLevel : TaskLevel.values()) {
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.DEAL_UPDATE, website));
                    }
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.SKU_UPDATE, website));
                    }
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.PRICE_CHANGES, website));
                    }
                }
                if (pop == null) {
                    TimeUnit.MINUTES.sleep(1);
                } else {
                    SpiderLogger.infoFetchFlow("start spider this url: {}", pop);
                    FetchUrlResult fetchUrlResult = JSONUtil.toObject(pop.toString(), FetchUrlResult.class);
                    fetch(fetchUrlResult);
                    if (fetchUrlResult.overFetch()) {
                        logger.info("FetchUrlWorker crawl finish: {} ", fetchUrlResult);
                    } else {
                        logger.info("FetchUrlWorker crawl running: {} ", fetchUrlResult);
                    }
                    SpiderLogger.infoFetchFlow("Finish spider this url: {}", pop);
                }
            } catch (Exception e) {
                logger.error("FetchUrlWorker is error. Error Msg: Json to Object fail.", e);
            }
        }
    }

    public void fetch(FetchUrlResult fetchUrlResult) {
        if (Website.AMAZON.equals(fetchUrlResult.getWebsite())) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                logger.error("Time sleep error.", e);
            }
        }
        try {
            fetchUrlResult = fetchService.spiderProductByUrl(fetchUrlResult);
        } catch (UnSupportWebsiteException e) {
            fetchUrlResult.setTaskStatus(TaskStatus.STOPPED);
            fetchUrlResult.setErrMsg("un able support website.");
            fetchCacheService.pushFinishUrlList(fetchUrlResult);
            logger.error("FetchKeywordWorker is error. Error Msg: un able support website.", e);
        }
    }

}
