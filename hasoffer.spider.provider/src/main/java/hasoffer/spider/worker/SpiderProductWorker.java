package hasoffer.spider.worker;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.spider.constants.Constants;
import hasoffer.spider.context.SpiderConfigInitContext;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.model.SpiderProductTask;
import hasoffer.spider.pp.common.AbstractPageProcessor;
import hasoffer.spider.service.ISpiderScheduleService;
import hasoffer.spider.service.impl.SpiderProductScheduleServiceImpl;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpiderProductWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SpiderProductWorker.class);

    private SpiderConfig spiderConfig;

    private int i = 0;

    private ISpiderScheduleService spiderProductScheduler;

    private IRedisListService<String> redisListService;


    public SpiderProductWorker(SpiderConfig spiderConfig, AbstractPageProcessor pageProcessor, Pipeline pipeline) {
        this.spiderConfig = spiderConfig;
        redisListService = (IRedisListService<String>) SpringContextHolder.getBean(RedisListServiceImpl.class);
        spiderProductScheduler = new SpiderProductScheduleServiceImpl(spiderConfig, pageProcessor, pipeline);
        spiderProductScheduler.startSpiderTask();
    }

    @Override
    public void run() {
        String redisListName = SpiderConfigInitContext.getRedisListName(spiderConfig.getWebsite(), PageType.LIST);
        logger.info("Website:{}, Redis List Name:{}", spiderConfig.getWebsite(), redisListName);
        if (redisListName == null || "".equals(redisListName)) {
            return;
        }
        while (true) {
            String productTaskStr = redisListService.pop(redisListName);
            if (productTaskStr == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            SpiderProductTask productTask = null;
            try {
                productTask = JSONUtil.toObject(productTaskStr, SpiderProductTask.class);
            } catch (IOException e) {
                logger.error("Json parse error. error msg {}", e);
            }
            if (productTask == null) {
                return;
            }
            Map<String, Object> extraMap = new HashMap<>();
            String productId = productTask.getId();
            if (productId == null) {
                return;
            }

            //为任务附加初始默认参数。
            extraMap.put(Constants.SPIDER_EXTRA_PRO_ID, productId);
            extraMap.put(Constants.SPIDER_EXTRA_WEB_SITE, spiderConfig.getWebsite());
            extraMap.put(Constants.SPIDER_PARSE_TRY_TIMES, 0);

            if (Spider.Status.Stopped.equals(spiderProductScheduler.runStatus())) {
                logger.debug("start " + spiderConfig.getWebsite() + ":" + i++);
                spiderProductScheduler.startSpiderTask();
            }
            spiderProductScheduler.pushRequest(productTask.getUrl(), extraMap);
        }
    }
}
