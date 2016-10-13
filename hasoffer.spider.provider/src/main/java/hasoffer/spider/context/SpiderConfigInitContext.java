package hasoffer.spider.context;

import hasoffer.base.model.Website;
import hasoffer.base.thread.HasofferThreadFactory;
import hasoffer.base.utils.StringUtils;
import hasoffer.dubbo.spider.task.api.ISpiderConfigService;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderConfig;
import hasoffer.spider.pp.common.AbstractListProcessor;
import hasoffer.spider.pp.common.AbstractPageProcessor;
import hasoffer.spider.worker.SpiderProductWorker;
import hasoffer.spider.worker.SpiderSkuWorker;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;
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
        initPageThread(PageType.DETAIL);

        initRedis(PageType.LIST);
        initPageThread(PageType.LIST);
    }

    public static String getRedisListName(Website website, PageType pageType) {
        return redisNameMap.get(website + "_" + pageType);
    }

    private void initRedis(PageType pageType) {
        List<SpiderConfig> list = spiderConfigService.findByPageType(pageType, "Y");

        for (SpiderConfig spiderConfig : list) {
            redisNameMap.put(spiderConfig.getWebsite() + "_" + pageType, WAIT_URL_SET + "_" + spiderConfig.getWebsite() + "_" + pageType.toString());
        }

        logger.info("cache sku task redis map:{}" + redisNameMap.values());
    }

    private void initPageThread(PageType pageType) {
        String threadName = "SpiderSkuWorker";
        if (PageType.LIST.equals(pageType)) {
            threadName = "SpiderProductWorker";
        }
        HasofferThreadFactory factory = new HasofferThreadFactory(threadName);
        ExecutorService es = Executors.newCachedThreadPool(factory);
        List<SpiderConfig> list = spiderConfigService.findByPageType(pageType, "Y");
        for (SpiderConfig spiderConfig : list) {
            if (isInitWebsite(spiderConfig)) {
                if (PageType.DETAIL.equals(pageType)) {
                    es.execute(new SpiderSkuWorker(spiderConfig, (AbstractPageProcessor) getPageProcessor(spiderConfig.getProcessorClass()), (Pipeline) getPipeline(spiderConfig.getPipelineClass())));
                } else if (PageType.LIST.equals(pageType)) {
                    es.execute(new SpiderProductWorker(spiderConfig, (AbstractListProcessor) getPageProcessor(spiderConfig.getProcessorClass()), (Pipeline) getPipeline(spiderConfig.getPipelineClass())));
                }
            }

        }
    }

    private boolean isInitWebsite(SpiderConfig spiderConfig) {
        return (spiderConfig == null || spiderConfig.getWebsite() == null || getPageProcessor(spiderConfig.getProcessorClass()) == null) ? false : true;
    }

    private Pipeline getPipeline(String className) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }
        if (className.trim().length() == 0) {
            return null;
        }
        try {
            Class onwClass = Class.forName(className);
            return (Pipeline) onwClass.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("getClass error. the class:{}.", className, e);
            return null;
        } catch (InstantiationException e) {
            logger.error("getClass error. the class:{}.", className, e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("getClass error. the class:{}.", className, e);
            return null;
        }
    }

    private PageProcessor getPageProcessor(String className) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }
        if (className.trim().length() == 0) {
            return null;
        }
        try {
            Class onwClass = Class.forName(className);
            return (PageProcessor) onwClass.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("getClass error. the class:{}.", className, e);
            return null;
        } catch (InstantiationException e) {
            logger.error("getClass error. the class:{}.", className, e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("getClass error. the class:{}.", className, e);
            return null;
        }
    }

}
