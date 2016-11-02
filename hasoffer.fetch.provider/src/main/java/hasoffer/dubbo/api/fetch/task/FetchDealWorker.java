package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.ISpiderService;
import hasoffer.spider.api.impl.SpiderServiceImpl;
import hasoffer.spider.constants.RedisKeysUtils;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/1.
 */
public class FetchDealWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchDealWorker.class);

    private IFetchCacheService fetchCacheService;

    private ISpiderService fetchService = new SpiderServiceImpl();

    public FetchDealWorker(WebApplicationContext springContext) {
        fetchCacheService = (IFetchCacheService) springContext.getBean("fetchCacheService");
    }

    @Override
    public void run() {
        while (true) {
            Object pop = fetchCacheService.popTaskList(RedisKeysUtils.WAIT_KEY_LIST);
            if (pop == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("FetchDealWorker at {} , pop word: {}", new Date(), pop);
                FetchDealResult fetchDealResult = null;
                try {
                    fetchDealResult = JSONUtil.toObject(pop.toString(), FetchDealResult.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                fetch(fetchResult);
            }


        }
    }
}
