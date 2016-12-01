package hasoffer.dubbo.worker.fetch;

import hasoffer.base.utils.IPUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.spider.api.ISpiderService;
import hasoffer.spider.api.impl.SpiderServiceImpl;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FetchKeywordWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchKeywordWorker.class);

    private IFetchCacheService fetchCacheService;

    private ISpiderService fetchService = new SpiderServiceImpl();

    private IRedisMapService<String, String> mapService;

    private String localIp;


    public FetchKeywordWorker(WebApplicationContext springContext) {
        fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
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
            String isWait = mapService.getValue("ALI-VPC-STATUS", localIp);
            logger.info("Local IP:{}, ALI-VPC-STATUS: {}. Thread will sleep 1 min.", localIp, isWait);
            if (isWait != null && "N".equals(isWait)) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            Object pop = fetchCacheService.popTaskList(RedisKeysUtils.WAIT_KEY_LIST);
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
            fetchResult = fetchService.spiderProductSetByKeyword(fetchResult, 10);
        } catch (UnSupportWebsiteException e) {
            logger.error("don't support this website.", e);
            e.printStackTrace();
        }
        logger.info("Fetch Success:website:{}, Key :{}, success:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), fetchResult.getFetchProducts().size());
        String cacheKey = FetchResult.getCacheKey(fetchResult);
        fetchCacheService.setTaskStatusByKeyword(cacheKey, fetchResult.getTaskStatus());
        fetchCacheService.cacheResult(cacheKey, fetchResult,FetchResult.expirySeconds(cacheKey));
    }

}
