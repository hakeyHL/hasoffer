package hasoffer.dubbo.worker.fetch;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.IPUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
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

import java.net.SocketException;
import java.util.concurrent.TimeUnit;

public class FetchUrlWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchUrlWorker.class);

    private IFetchCacheService fetchCacheService;

    private Website website;

    private ISpiderService fetchService = new SpiderServiceImpl();

    private IRedisMapService<String, String> mapService;
    private IRedisListService redisListService;

    private String localIp;

    public FetchUrlWorker(WebApplicationContext springContext, Website website) {
        this.website = website;
        this.fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
        this.redisListService = springContext.getBean(RedisListServiceImpl.class);
        this.mapService = springContext.getBean(IRedisMapService.class);
        try {
            this.localIp = IPUtils.getFirstNoLoopbackIPAddresses();
        } catch (SocketException e) {
            logger.error("Get local IP error.", e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String waitStr = mapService.getValue("ALI-VPC-STATUS", localIp);
                boolean isWait = waitStr != null && "N".equals(waitStr);
                logger.info("Local IP:{}, isWait(ALI-VPC-STATUS): {} ", localIp, isWait);
                if (isWait) {
                    TimeUnit.MINUTES.sleep(1);
                    continue;
                }
                Object pop = null;
                for (TaskLevel taskLevel : TaskLevel.values()) {

                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.PRICEOFF_NOTICE, website));
                    }
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.DEAL_UPDATE, website));
                    }
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.STDPRICE_UPDATE, website));
                    }
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.SKU_UPDATE, website));
                    }
                    if (pop == null) {
                        pop = fetchCacheService.popTaskList(RedisKeysUtils.getWaitUrlListKey(taskLevel, TaskTarget.PRICE_CHANGES, website));
                    }
                }
                if (pop == null) {
                    logger.info("task list is null. thread will sleep 1 min.");
                    TimeUnit.MINUTES.sleep(1);
                } else {
                    logger.info("pop wait taskLevel:{}, taskTarget:{}, size:{} ", TaskLevel.LEVEL_1, TaskTarget.PRICEOFF_NOTICE, redisListService.size(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_1, TaskTarget.PRICEOFF_NOTICE, website)));
                    logger.info("pop wait taskLevel:{}, taskTarget:{}, size:{} ", TaskLevel.LEVEL_2, TaskTarget.DEAL_UPDATE, redisListService.size(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_2, TaskTarget.DEAL_UPDATE, website)));
                    logger.info("pop wait taskLevel:{}, taskTarget:{}, size:{} ", TaskLevel.LEVEL_3, TaskTarget.STDPRICE_UPDATE, redisListService.size(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_3, TaskTarget.STDPRICE_UPDATE, website)));
                    logger.info("pop wait taskLevel:{}, taskTarget:{}, size:{} ", TaskLevel.LEVEL_4, TaskTarget.SKU_UPDATE, redisListService.size(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_4, TaskTarget.SKU_UPDATE, website)));
                    logger.info("pop wait taskLevel:{}, taskTarget:{}, size:{} ", TaskLevel.LEVEL_5, TaskTarget.PRICE_CHANGES, redisListService.size(RedisKeysUtils.getWaitUrlListKey(TaskLevel.LEVEL_5, TaskTarget.PRICE_CHANGES, website)));
                    SpiderLogger.infoFetchFlow("Spider this url: {}", pop);
                    FetchUrlResult fetchUrlResult = JSONUtil.toObject(pop.toString(), FetchUrlResult.class);
                    fetch(fetchUrlResult);
                }
            } catch (Exception e) {
                logger.error("FetchUrlWorker is error. Error Msg: Json to Object fail.", e);
            }
        }
    }

    public void fetch(FetchUrlResult fetchUrlResult) {
        try {
            fetchService.spiderProductByUrl(fetchUrlResult);
        } catch (UnSupportWebsiteException e) {
            fetchUrlResult.setTaskStatus(TaskStatus.STOPPED);
            fetchUrlResult.setErrMsg("un able support website.");
            fetchCacheService.pushFinishUrlList(fetchUrlResult);
            logger.error("FetchKeywordWorker is error. Error Msg: un able support website.", e);
        }
    }

}
