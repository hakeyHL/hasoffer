package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.IFetchService;
import hasoffer.spider.api.impl.FetchServiceImpl;
import hasoffer.spider.common.StringConstant;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spider.redis.service.IFetchCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
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
                logger.info("FetchUrlWorker is alive at {}", new Date());
                Object pop = fetchCacheService.popKeyword(StringConstant.WAIT_URL_LIST);
                if (pop == null) {
                    TimeUnit.SECONDS.sleep(60);
                } else {
                    FetchUrlResult fetchResult = JSONUtil.toObject(pop.toString(), FetchUrlResult.class);
                    fetch(fetchResult);
                    fetchCacheService.cacheResult(FetchUrlResult.getCacheKey(fetchResult), fetchResult);
                }
            } catch (Exception e) {
                logger.error("FetchKeywordWorker is error. Error Msg: ", e.getMessage());
            }
        }
    }

    public void fetch(FetchUrlResult fetchResult) {
        try {
            FetchedProduct product = fetchService.getProductByUrl(fetchResult.getWebsite(), fetchResult.getUrl());
            fetchResult.setFetchProduct(product);
            fetchResult.setTaskStatus(TaskStatus.FINSH);
        } catch (HttpFetchException e) {
            if (fetchResult.getRunCount() < 5) {
                fetchResult.setRunCount(fetchResult.getRunCount() + 1);
                fetchCacheService.saveKeyword(StringConstant.WAIT_KEY_LIST, JSONUtil.toJSON(fetchResult));
            } else {
                fetchResult.setTaskStatus(TaskStatus.STOPPED);
                fetchResult.setErrMsg("尝试抓取5次没有成功，放弃该任务。");
            }
            e.printStackTrace();
        } catch (ContentParseException e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("内容解析失败，请修正。");
            e.printStackTrace();
        } catch (UnSupportWebsiteException e) {
            fetchResult.setTaskStatus(TaskStatus.STOPPED);
            fetchResult.setErrMsg("暂不支持该网站抓取。");
            e.printStackTrace();
        }
    }

}
