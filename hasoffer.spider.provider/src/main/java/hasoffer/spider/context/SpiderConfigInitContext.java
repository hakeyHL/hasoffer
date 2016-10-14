package hasoffer.spider.context;

import hasoffer.base.model.Website;
import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.spider.detail.pp.*;
import hasoffer.spider.detail.ppl.SkuPagePipeline;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.list.pp.*;
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

        if (isInitWebsite(spiderConfig)) {
            redisNameMap.put(Website.AMAZON + "_" + pageType, WAIT_URL_SET + "_" + Website.AMAZON + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART, pageType);
        if (isInitWebsite(spiderConfig)) {
            redisNameMap.put(Website.FLIPKART + "_" + pageType, WAIT_URL_SET + "_" + Website.FLIPKART + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL, pageType);
        if (isInitWebsite(spiderConfig)) {
            redisNameMap.put(Website.SNAPDEAL + "_" + pageType, WAIT_URL_SET + "_" + Website.SNAPDEAL + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.PAYTM, pageType);
        if (isInitWebsite(spiderConfig)) {
            redisNameMap.put(Website.PAYTM + "_" + pageType, WAIT_URL_SET + "_" + Website.PAYTM + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SHOPCLUES, pageType);
        if (isInitWebsite(spiderConfig)) {
            redisNameMap.put(Website.SHOPCLUES + "_" + pageType, WAIT_URL_SET + "_" + Website.SHOPCLUES + "_" + pageType.toString());
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.EBAY, pageType);
        if (isInitWebsite(spiderConfig)) {
            redisNameMap.put(Website.EBAY + "_" + pageType, WAIT_URL_SET + "_" + Website.EBAY + "_" + pageType.toString());
        }

        logger.info("cache sku task redis map:{}" + redisNameMap.values());

    }

    private void initPageThread() {

        HasofferThreadFactory factory = new HasofferThreadFactory("SpiderSkuWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON, PageType.DETAIL);

        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaAmazonPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART, PageType.DETAIL);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaFlipKartPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL, PageType.DETAIL);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaSnapdealPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.PAYTM, PageType.DETAIL);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaPaytmPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.SHOPCLUES, PageType.DETAIL);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaShopcluesPageProcessor(), new SkuPagePipeline()));
        }

        spiderConfig = spiderConfigService.findByWebsite(Website.EBAY, PageType.DETAIL);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderSkuWorker(spiderConfig, new IndiaEbayInPageProcessor(), new SkuPagePipeline()));
        }

    }

    private void initListThread() {

        HasofferThreadFactory factory = new HasofferThreadFactory("SpiderProductWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);

        // 1. 初始化Amazon
        SpiderConfig spiderConfig = spiderConfigService.findByWebsite(Website.AMAZON, PageType.LIST);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderProductWorker(spiderConfig, new IndiaAmazonListProcessor(), new ProductListPipeline()));
        }

        // 2. 初始化Flipkart
        spiderConfig = spiderConfigService.findByWebsite(Website.FLIPKART, PageType.LIST);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderProductWorker(spiderConfig, new IndiaFlipkartListProcessor(), new ProductListPipeline()));
        }

        // 3. 初始化SnapDeal
        spiderConfig = spiderConfigService.findByWebsite(Website.SNAPDEAL, PageType.LIST);
        if (isInitWebsite(spiderConfig)) {
            es.execute(new SpiderProductWorker(spiderConfig, new IndiaSnapdealListProcessor(), new ProductListPipeline()));
        }

        //// 4. 初始化HomeShop18
        //spiderConfig = spiderConfigService.findByWebsite(Website.HOMESHOP18, PageType.LIST);
        //if (isInitWebsite(spiderConfig)) {
        //    es.execute(new SpiderProductWorker(spiderConfig, new IndiaHomeShop18ListProcessor(), new ProductListPipeline()));
        //}

        //// 5. 初始化JABONG
        //spiderConfig = spiderConfigService.findByWebsite(Website.JABONG, PageType.LIST);
        //if (isInitWebsite(spiderConfig)) {
        //    es.execute(new SpiderProductWorker(spiderConfig, new IndiaJabongListProcessor(), new ProductListPipeline()));
        //}

        //// 5. 初始化LimeRoad
        //spiderConfig = spiderConfigService.findByWebsite(Website.LIMEROAD, PageType.LIST);
        //if (isInitWebsite(spiderConfig)) {
        //    es.execute(new SpiderProductWorker(spiderConfig, new IndiaLimeRoadListProcessor(), new ProductListPipeline()));
        //}

        //// 6. 初始化Voonik
        //spiderConfig = spiderConfigService.findByWebsite(Website.VOONIK, PageType.LIST);
        //if (isInitWebsite(spiderConfig)) {
        //    es.execute(new SpiderProductWorker(spiderConfig, new IndiaVoonikListProcessor(), new ProductListPipeline()));
        //}


    }

    private boolean isInitWebsite(SpiderConfig spiderConfig) {
        return spiderConfig != null && spiderConfig.getApply();
    }

    public static String getRedisListName(Website website, PageType pageType) {
        return redisNameMap.get(website + "_" + pageType);
    }
}
