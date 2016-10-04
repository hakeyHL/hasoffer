package hasoffer.spider.worker;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.spider.constants.Constants;
import hasoffer.spider.context.SpiderConfigInitContext;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.model.SpiderSkuTask;
import hasoffer.spider.pp.common.AbstractPageProcessor;
import hasoffer.spider.service.ISpiderScheduleService;
import hasoffer.spider.service.impl.SpiderSkuScheduleServiceImpl;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpiderSkuWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SpiderSkuWorker.class);

    private SpiderConfig spiderConfig;

    private int i = 0;

    private ISpiderScheduleService spiderSkuScheduler;

    private IRedisListService<String> redisListService;


    public SpiderSkuWorker(SpiderConfig spiderConfig, AbstractPageProcessor pageProcessor, Pipeline pipeline) {
        this.spiderConfig = spiderConfig;
        redisListService = (IRedisListService<String>) SpringContextHolder.getBean(RedisListServiceImpl.class);
        spiderSkuScheduler = new SpiderSkuScheduleServiceImpl(spiderConfig, pageProcessor, pipeline);
    }

    @Override
    public void run() {
        String redisListName = SpiderConfigInitContext.getRedisListName(spiderConfig.getWebsite(), PageType.DETAIL);
        if (redisListName == null || "".equals(redisListName)) {
            return;
        }
        logger.info("NEW Website:{}, Redis List Name:{}", spiderConfig.getWebsite(), redisListName);
        while (true) {
            String skuTaskStr = redisListService.pop(redisListName);
            logger.info("Get a task:{}", skuTaskStr);
            if (skuTaskStr == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            SpiderSkuTask skuTask = null;
            try {
                skuTask = JSONUtil.toObject(skuTaskStr, SpiderSkuTask.class);
            } catch (IOException e) {
                logger.error("Json parse error. error msg {}", e);
            }
            logger.info("execute this task:{}", skuTask);
            if (skuTask == null) {
                return;
            }
            Map<String, Object> extraMap = new HashMap<>();
            Long skuId = skuTask.getSkuId();
            if (skuId == null) {
                return;
            }

            //为任务附加初始默认参数。
            extraMap.put(Constants.SPIDER_EXTRA_SKU_ID, skuId);
            extraMap.put(Constants.SPIDER_PARSE_TRY_TIMES, 0);

            spiderSkuScheduler.pushRequest(skuTask.getUrl(), extraMap);

            spiderSkuScheduler.startSpiderTask();
        }
    }
}
