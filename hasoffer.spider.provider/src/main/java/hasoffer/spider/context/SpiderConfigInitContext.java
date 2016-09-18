package hasoffer.spider.context;

import hasoffer.base.model.Website;
import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.spider.detail.pp.*;
import hasoffer.spider.detail.ppl.SkuPagePipeline;
import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.service.ISpiderConfigService;
import hasoffer.spider.worker.SpiderSkuWorker;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpiderConfigInitContext {

    private static final Logger logger = LoggerFactory.getLogger(SpiderConfigInitContext.class);

    private static final String WAIT_SKU_URL_SET = "WAIT_SKU_SPIDER_SET";

    private static Map<Website, String> redisNameMap = new HashMap<>();

    private ISpiderConfigService spiderConfigService;

    public SpiderConfigInitContext() {
        this.spiderConfigService = (ISpiderConfigService) SpringContextHolder.getBean("spiderConfigService");
        initRedis();
        initThread();
    }

    private void initRedis() {
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON);

        if (spiderConfig != null) {
            redisNameMap.put(Website.AMAZON, WAIT_SKU_URL_SET + "_" + Website.AMAZON);
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART);
        if (spiderConfig != null) {
            redisNameMap.put(Website.FLIPKART, WAIT_SKU_URL_SET + "_" + Website.FLIPKART);
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL);
        if (spiderConfig != null) {
            redisNameMap.put(Website.SNAPDEAL, WAIT_SKU_URL_SET + "_" + Website.SNAPDEAL);
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.PAYTM);
        if (spiderConfig != null) {
            redisNameMap.put(Website.PAYTM, WAIT_SKU_URL_SET + "_" + Website.PAYTM);
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SHOPCLUES);
        if (spiderConfig != null) {
            redisNameMap.put(Website.SHOPCLUES, WAIT_SKU_URL_SET + "_" + Website.SHOPCLUES);
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.EBAY);
        if (spiderConfig != null) {
            redisNameMap.put(Website.EBAY, WAIT_SKU_URL_SET + "_" + Website.EBAY);
        }

        logger.info("cache sku task redis map:{}" + redisNameMap.values());

    }

    private void initThread() {

        HasofferThreadFactory factory = new HasofferThreadFactory("SpiderSkuWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON);

        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaAmazonPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaFlipKartPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaSnapdealPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.PAYTM);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaPaytmPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SHOPCLUES);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaShopcluesPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.EBAY);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaEbayInPageProcessor(), new SkuPagePipeline()));
        }

    }

    public static String getRedisListName(Website website) {
        return redisNameMap.get(website);
    }
}
