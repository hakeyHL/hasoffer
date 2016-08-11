package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.IFetchService;
import hasoffer.spider.api.impl.FetchServiceImpl;
import hasoffer.spider.common.RedisKeysConstant;
import hasoffer.spider.common.SpiderLogger;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

public class FetchUrlWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchUrlWorker.class);

    private IFetchCacheService fetchCacheService;

    private IFetchService fetchService = new FetchServiceImpl();

    public FetchUrlWorker(WebApplicationContext springContext) {
        fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object pop = fetchCacheService.popTaskList(RedisKeysConstant.WAIT_URL_LIST);
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
                logger.error("FetchKeywordWorker is error. Error Msg: Json to Object fail.", e);
            }
        }
    }

    public void fetch(FetchUrlResult fetchUrlResult) {
        try {
            fetchUrlResult = fetchService.getProductByUrl(fetchUrlResult);
        } catch (UnSupportWebsiteException e) {
            fetchUrlResult.setTaskStatus(TaskStatus.STOPPED);
            fetchUrlResult.setErrMsg("un able support website.");
            fetchCacheService.cacheResult(FetchUrlResult.getCacheKey(fetchUrlResult), fetchUrlResult);
            logger.error("FetchKeywordWorker is error. Error Msg: un able support website.", e);
        }
    }

}
