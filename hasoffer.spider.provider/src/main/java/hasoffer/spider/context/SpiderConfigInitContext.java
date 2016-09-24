package hasoffer.spider.context;

import hasoffer.base.model.Website;
import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.spider.detail.pp.*;
import hasoffer.spider.detail.ppl.SkuPagePipeline;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.list.pp.IndiaAmazonListProcessor;
import hasoffer.spider.list.ppl.ProductListPipeline;
import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.service.ISpiderConfigService;
import hasoffer.spider.worker.SpiderProductWorker;
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

    private static final String WAIT_URL_SET = "WAIT_SPIDER_SET";

    private static Map<String, String> redisNameMap = new HashMap<>();

    private ISpiderConfigService spiderConfigService;

    public SpiderConfigInitContext() {
        this.spiderConfigService = (ISpiderConfigService) SpringContextHolder.getBean("spiderConfigService");

        initRedis(PageType.DETAIL);
        initPageThread();

        initRedis(PageType.LIST);
        initListThread();
    }

    private void initRedis(PageType pageType) {
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON, pageType);

        if (spiderConfig != null) {
            redisNameMap.put(Website.AMAZON + "_" + pageType, WAIT_URL_SET + "_" + Website.AMAZON + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART, pageType);
        if (spiderConfig != null) {
            redisNameMap.put(Website.FLIPKART + "_" + pageType, WAIT_URL_SET + "_" + Website.FLIPKART + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL, pageType);
        if (spiderConfig != null) {
            redisNameMap.put(Website.SNAPDEAL + "_" + pageType, WAIT_URL_SET + "_" + Website.SNAPDEAL + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.PAYTM, pageType);
        if (spiderConfig != null) {
            redisNameMap.put(Website.PAYTM + "_" + pageType, WAIT_URL_SET + "_" + Website.PAYTM + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SHOPCLUES, pageType);
        if (spiderConfig != null) {
            redisNameMap.put(Website.SHOPCLUES + "_" + pageType, WAIT_URL_SET + "_" + Website.SHOPCLUES + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.EBAY, pageType);
        if (spiderConfig != null) {
            redisNameMap.put(Website.EBAY + "_" + pageType, WAIT_URL_SET + "_" + Website.EBAY + "_" + pageType.toString());
        }

        logger.info("cache sku task redis map:{}" + redisNameMap.values());

    }

    private void initPageThread() {

        HasofferThreadFactory factory = new HasofferThreadFactory("SpiderSkuWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON, PageType.DETAIL);

        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaAmazonPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART, PageType.DETAIL);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaFlipKartPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL, PageType.DETAIL);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaSnapdealPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.PAYTM, PageType.DETAIL);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaPaytmPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SHOPCLUES, PageType.DETAIL);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaShopcluesPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.EBAY, PageType.DETAIL);
        if (spiderConfig != null) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaEbayInPageProcessor(), new SkuPagePipeline()));
        }

    }

    private void initListThread() {

        HasofferThreadFactory factory = new HasofferThreadFactory("SpiderProductWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON, PageType.LIST);

        if (spiderConfig != null) {
            es.execute(new SpiderProductWorker(spiderConfig, new IndiaAmazonListProcessor(), new ProductListPipeline()));
        }

    }

    public static String getRedisListName(Website website, PageType pageType) {
        return redisNameMap.get(website + "_" + pageType);
    }
}
