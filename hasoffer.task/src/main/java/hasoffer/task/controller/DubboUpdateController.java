package hasoffer.task.controller;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.model.FetchResult;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.task.worker.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 2016/6/22.
 */
@Controller
@RequestMapping(value = "/dubbofetchtask")
public class DubboUpdateController {

    private static AtomicBoolean taskRunning1 = new AtomicBoolean(false);
    private static AtomicBoolean taskRunning2 = new AtomicBoolean(false);
    private static AtomicBoolean taskRunning3 = new AtomicBoolean(false);
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


    /**
     * Date：2016-11-1 10:34更新改成一直在更新，从redis中读取数据
     */
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    @ResponseBody
    public String start() {
        if (taskRunning4.get()) {
            return "task running.";
        }

        long cacheSeconds = TimeUtils.SECONDS_OF_1_HOUR * 2;

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new ListNeedUpdateFromRedisWorker(queue, fetchDubboService, redisListService, cmpSkuService, cacheSeconds));

        for (int i = 0; i < 60; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService, redisListService, cacheSeconds));
        }


        taskRunning4.set(true);
        return "ok";
    }

    /**
     * 该任务用来更新用户订阅的sku，如果价格变化加入缓存队列，等待push
     *
     * @return
     */
    //dubbofetchtask/priceoffnotice
    @RequestMapping(value = "/priceoffnotice", method = RequestMethod.GET)
    @ResponseBody
    public String priceoffnotice() {

        if (taskRunning3.get()) {
            return "task running.";
        }

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

        es.execute(new PriceOffNoticeListWorker(dbm, queue, fetchDubboService));

        //保证list任务优先执行
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            es.execute(new PriceOffNoticeProcessorWorker(queue, fetchDubboService, redisListService, cmpSkuService, dbm));
        }

        taskRunning3.set(true);

        return "ok";
    }


    /**
     * sku的日常更新（目前策略热搜）
     *
     * @return
     */
    //dubbofetchtask/updatestart
    @RequestMapping(value = "/updatestart", method = RequestMethod.GET)
    @ResponseBody
    public String updatestart() {

        if (taskRunning1.get()) {
            return "task running.";
        }

        long cacheSeconds = TimeUtils.SECONDS_OF_1_DAY;

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new SrmProductSearchCountListWorker(dbm, queue, fetchDubboService, redisListService));

        //保证list任务优先执行
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 60; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService, redisListService, cacheSeconds));
        }

        taskRunning1.set(true);

        return "ok";
    }

//    //dubbofetchtask/updateTopSellingSpec
//    @RequestMapping(value = "/updateTopSellingSpec", method = RequestMethod.GET)
//    @ResponseBody
//    public String updateTopSellingSpec() {
//
//        if (taskRunning2.get()) {
//            return "task running.";
//        }
//
//
//
//        ExecutorService es = Executors.newCachedThreadPool();
//
//        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();
//
//        es.execute(new TopSellingListWorker(dbm, queue, fetchDubboService));
//
//        for (int i = 0; i < 30; i++) {
//            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService, redisListService,cacheSeconds));
//        }
//
//        taskRunning2.set(true);
//
//        return "ok";
//    }

    //dubbofetchtask/testSingle
    @RequestMapping(value = "/testSingle/{skuid}", method = RequestMethod.GET)
    @ResponseBody
    public String testSingle(@PathVariable long skuid) {

        PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, skuid);

        if (ptmCmpSku == null) {
            return "sku do not exists";
        }

        Website website = ptmCmpSku.getWebsite();
        String url = ptmCmpSku.getUrl();

        fetchDubboService.sendUrlTask(website, url, TimeUtils.SECONDS_OF_1_DAY, TaskLevel.LEVEL_1);
        System.out.println("send single url success for " + skuid);

        while (true) {
            TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(website, url, TimeUtils.SECONDS_OF_1_DAY);
            if (TaskStatus.FINISH.equals(taskStatus) || TaskStatus.EXCEPTION.equals(taskStatus)) {
                break;
            } else {
                System.out.println(taskStatus);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {

                }
            }
        }

        FetchUrlResult fetchUrlResult = fetchDubboService.getProductsByUrl(website, url, TimeUtils.SECONDS_OF_1_DAY);

        FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

        System.out.println(JSONUtil.toJSON(fetchedProduct).toString() + "id=" + skuid);

        try {
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuid, fetchedProduct);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cmpSkuService.createPtmCmpSkuImage(skuid, fetchedProduct);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            PtmProduct ptmProduct = dbm.get(PtmProduct.class, ptmCmpSku.getProductId());

            if (ptmProduct != null) {

                //保存sku的描述信息
                cmpSkuService.createSkuDescription(ptmCmpSku, fetchedProduct);

                String productTitle = ptmProduct.getTitle();

                if (StringUtils.isEqual(productTitle, ptmCmpSku.getTitle())) {
                    //保存product的描述信息
                    cmpSkuService.createProductDescription(ptmCmpSku, fetchedProduct);
                    System.out.println("update product spec success for " + ptmProduct.getId());
                } else {
                    System.out.println("product spec should remove " + ptmProduct.getId());
                }
            } else {
                System.out.println(skuid + " product is null");
            }
        } catch (Exception e) {
            System.out.println("createDescription fail " + skuid);
        }

        return "ok";
    }

    //dubbofetchtask/testListSingle
    @RequestMapping(value = "/testListSingle/{websiteString}/{keyword}", method = RequestMethod.GET)
    @ResponseBody
    public String testListSingle(@PathVariable String websiteString, @PathVariable String keyword) {

        Website website = Website.valueOf(websiteString.toUpperCase());

        fetchDubboService.sendKeyWordTask(website, keyword);
        System.out.println("send single list processor success for " + websiteString + " " + keyword);

        while (true) {
            TaskStatus taskStatus = fetchDubboService.getKeyWordTaskStatus(website, keyword);
            if (TaskStatus.FINISH.equals(taskStatus) || TaskStatus.EXCEPTION.equals(taskStatus)) {
                break;
            } else {
                System.out.println(taskStatus);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {

                }
            }
        }

        FetchResult fetchResult = fetchDubboService.getProductsKeyWord(website, keyword);

        List<FetchedProduct> products = fetchResult.getFetchProducts();

        for (FetchedProduct product : products) {
            System.out.println(product);
        }

        return "ok";
    }
}
