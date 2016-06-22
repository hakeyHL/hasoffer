package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.IFetchService;
import hasoffer.spider.api.impl.FetchServiceImpl;
import hasoffer.spider.common.StringConstant;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.List;
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
            try {
                logger.info("FetchKeywordWorker is alive at {} ", new Date());
                Object pop = fetchCacheService.popKeyword(StringConstant.WAIT_KEY_LIST);
                if (pop == null) {
                    TimeUnit.SECONDS.sleep(60);
                } else {
                    FetchResult fetchResult = JSONUtil.toObject(pop.toString(), FetchResult.class);
                    fetch(fetchResult);
                    fetchCacheService.cacheResult(FetchResult.getCacheKey(fetchResult), fetchResult);
                }
            } catch (Exception e) {
                logger.error("FetchKeywordWorker is error. Error Msg: {}", e.getMessage());
            }
        }
    }

    public void fetch(FetchResult fetchResult) {
        //Website website = fetchResult.getWebsite();
        String keyword = fetchResult.getKeyword();
        try {
            List<FetchedProduct> productList = fetchService.getProductSetByKeyword(fetchResult.getWebsite(), keyword, 10);
            fetchResult.setFetchProducts(productList);
            fetchResult.setTaskStatus(TaskStatus.FINISH);
        } catch (HttpFetchException e) {
            if (fetchResult.getRunCount() < 5) {
                fetchResult.setRunCount(fetchResult.getRunCount() + 1);
                fetchCacheService.saveKeyword(StringConstant.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
            } else {
                fetchResult.setTaskStatus(TaskStatus.STOPPED);
                fetchResult.setErrMsg("The task is be ran 5 times.");
            }
            logger.error(e.getMessage());
        } catch (ContentParseException e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("It is error.");
            logger.error(e.getMessage());
        } catch (UnSupportWebsiteException e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("The website is not support.");
            logger.error(e.getMessage());
        }
    }

}
