package hasoffer.task.controller;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.IRedisSetService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.task.worker.CmpSkuDubboUpdate2Worker;
import hasoffer.task.worker.ListNeedUpdateFromRedisWorker;
import hasoffer.task.worker.StdPriceDubboUpdateWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 2016/6/22.
 */
@Controller
@RequestMapping(value = "/dubbofetchtask")
public class DubboUpdateController {

    private static Logger logger = LoggerFactory.getLogger(DubboUpdateController.class);
    private static AtomicBoolean taskRunning1 = new AtomicBoolean(false);
    private static AtomicBoolean taskRunning4 = new AtomicBoolean(false);


    @Resource
    @Qualifier("fetchDubboService")
    IFetchDubboService fetchDubboService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IProductService productService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    IPtmCmpSkuImageService ptmCmpSkuImageService;
    @Resource
    IPriceOffNoticeService priceOffNoticeService;
    @Resource
    IRedisListService redisListService;
    @Resource
    IRedisSetService redisSetService;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    IPtmStdPriceService ptmStdPriceService;

    /**
     * Date：2016-11-1 10:34更新改成一直在更新，从redis中读取数据
     */
    //dubbofetchtask/start
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    @ResponseBody
    public String start() {
        if (taskRunning4.get()) {
            return "task running.";
        }

        long cacheSeconds = TimeUtils.MILLISECONDS_OF_1_HOUR * 2;

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new ListNeedUpdateFromRedisWorker(fetchDubboService, redisListService, redisSetService, cmpSkuService, cacheSeconds, productCacheManager));

        for (int i = 0; i < 10; i++) {
            es.execute(new CmpSkuDubboUpdate2Worker(fetchDubboService, cmpSkuService, redisListService));
        }

        taskRunning4.set(true);
        return "ok";
    }

    //dubbofetchtask/stdPriceStart
    @RequestMapping(value = "/stdPriceStart", method = RequestMethod.GET)
    @ResponseBody
    public String stdPriceStart() {
        if (taskRunning1.get()) {
            return "task running.";
        }

        ExecutorService es = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++) {
            es.execute(new StdPriceDubboUpdateWorker(fetchDubboService, redisListService, ptmStdPriceService));
        }


        taskRunning1.set(true);
        return "ok";
    }

    //dubbofetchtask/updateSingleSkuById
    @RequestMapping(value = "/updateSingleSkuById/{skuid}", method = RequestMethod.GET)
    @ResponseBody
    public String updateSingleSkuById(@PathVariable long skuid) {

        PtmCmpSku sku = dbm.get(PtmCmpSku.class, skuid);
        if (sku == null) {
            logger.info("sku is null id = " + skuid);
        }

        Website website = sku.getWebsite();
        if (website == null) {
            logger.info("sku website is null id = " + skuid);
        }

        if (StringUtils.isEmpty(sku.getUrl())) {
            logger.info("sku website is null id = " + skuid);
        }

        if (Website.SNAPDEAL.equals(website)) {
            String url = sku.getUrl();
            url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
            sku.setUrl(url);
        }
        //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
        if (Website.AMAZON.equals(website)) {
            String url = sku.getUrl();
            url = url.replace("gp/offer-listing", "dp");
            sku.setUrl(url);
        }

        fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskTarget.SKU_UPDATE, TaskLevel.LEVEL_2);
        logger.info("updateSingleSkuById send url request succes for " + sku.getWebsite() + " sku id is _" + sku.getId() + "_");

        return "ok";
    }

    //dubbofetchtask/mobile91SingleUrlReFetch
    @RequestMapping(value = "/mobile91SingleUrlReFetch/{ptmStdSkuId}", method = RequestMethod.GET)
    @ResponseBody
    public String mobile91SingleUrlReFetch(@PathVariable long ptmStdSkuId) {

        PtmStdSku ptmStdSku = dbm.get(PtmStdSku.class, ptmStdSkuId);

        String sourceUrl = ptmStdSku.getSourceUrl();
        long categoryId = ptmStdSku.getCategoryId();

        fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, sourceUrl, TaskLevel.LEVEL_1, categoryId);

        return "ok";
    }

}
