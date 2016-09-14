package hasoffer.spider.worker;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.spider.context.SpiderConfigInitContext;
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
        spiderSkuScheduler.startSpiderTask();
    }

    @Override
    public void run() {
        String redisListName = SpiderConfigInitContext.getRedisListName(spiderConfig.getWebsite());
        if (redisListName == null || "".equals(redisListName)) {
            return;
        }
        while (true) {
            String skuTaskStr = redisListService.pop(redisListName);
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
            Map<String, Object> extraMap = new HashMap<>();
            extraMap.put("skuId", skuTask.getSkuId());
            spiderSkuScheduler.pushRequest(skuTask.getUrl(), extraMap);

            if (Spider.Status.Stopped.equals(spiderSkuScheduler.runStatus())) {
                System.out.println("start " + spiderConfig.getWebsite() + ":" + i++);
                spiderSkuScheduler.startSpiderTask();
            }
        }
    }
}