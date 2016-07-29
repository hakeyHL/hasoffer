package hasoffer.dubbo.api.fetch.task;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.utils.JSONUtil;
import hasoffer.spider.api.IFetchService;
import hasoffer.spider.api.impl.FetchServiceImpl;
import hasoffer.spider.common.StringConstant;
import hasoffer.spider.exception.UnSupportWebsiteException;
import hasoffer.spider.model.FetchUrlResult;
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
                    TimeUnit.MINUTES.sleep(1);
                } else {
                    FetchUrlResult fetchUrlResult = JSONUtil.toObject(pop.toString(), FetchUrlResult.class);
                    fetch(fetchUrlResult);
                    fetchCacheService.cacheResult(FetchUrlResult.getCacheKey(fetchUrlResult), fetchUrlResult);
                }
            } catch (Exception e) {
                logger.error("FetchKeywordWorker is error. Error Msg: ", e);
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
            e.printStackTrace();
        }
    }

}
