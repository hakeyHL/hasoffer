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

import java.io.IOException;
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
            Object pop = fetchCacheService.popKeyword(StringConstant.WAIT_KEY_LIST);
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
        //Website website = fetchResult.getWebsite();
        fetchResult.setTaskStatus(TaskStatus.RUNNING);
        fetchCacheService.cacheResult(FetchResult.getCacheKey(fetchResult), fetchResult);
        String keyword = fetchResult.getKeyword();
        try {
            List<FetchedProduct> productList = fetchService.getProductSetByKeyword(fetchResult.getWebsite(), keyword, 10);
            fetchResult.setFetchProducts(productList);
            fetchResult.setTaskStatus(TaskStatus.FINISH);
            logger.info("Fetch Success:website:{}, Key :{}, success:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), fetchResult.getFetchProducts().size());
        } catch (HttpFetchException e) {
            if (fetchResult.getRunCount() < 5) {
                fetchResult.setRunCount(fetchResult.getRunCount() + 1);
                fetchCacheService.saveKeywordList(StringConstant.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
            } else {
                fetchResult.setTaskStatus(TaskStatus.STOPPED);
                fetchResult.setErrMsg("The task is failed: run over 5 times. ");
                e.printStackTrace();
                logger.info("Fetch Fail:website:{}, Key :{}, errMsg:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), e);
            }
        } catch (ContentParseException e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("The task is failed: content parse failed.");
            logger.info("Fetch Fail:website:{}, Key :{}, errMsg:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), e);
        } catch (UnSupportWebsiteException e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("The task is failed: The website is not support.");
            logger.info("Fetch Fail:website:{}, Key :{}, errMsg:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), e);
        } catch (Exception e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("Other question.");
            logger.info("Fetch Fail:website:{}, Key :{}, errMsg:{}", fetchResult.getWebsite(), fetchResult.getKeyword(), e);
        }
        fetchCacheService.cacheResult(FetchResult.getCacheKey(fetchResult), fetchResult);
    }

}
