package hasoffer.task.controller;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.IRedisSetService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.task.worker.CmpSkuDubboUpdate2Worker;
import hasoffer.task.worker.ListNeedUpdateFromRedisWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    @Resource
    IRedisSetService redisSetService;
    @Resource
    ProductCacheManager productCacheManager;

    /**
     * Date：2016-11-1 10:34更新改成一直在更新，从redis中读取数据
     */
    //dubbofetchtask/start
    @RequestMapping(value = "/start/{number}", method = RequestMethod.GET)
    @ResponseBody
    public String start(@PathVariable long number) {
        if (taskRunning4.get()) {
            return "task running.";
        }

        long cacheSeconds = TimeUtils.MILLISECONDS_OF_1_HOUR * 2;

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new ListNeedUpdateFromRedisWorker(fetchDubboService, redisListService, redisSetService, cmpSkuService, cacheSeconds, productCacheManager));
//        es.execute(new ListNeedUpdateFromRedisWorker(fetchDubboService, redisListService, redisSetService, cmpSkuService, cacheSeconds, productCacheManager, number));//for test

//        CmpSkuDubboUpdate2Worker.popNumber = number;
        for (int i = 0; i < 10; i++) {
//            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService, redisListService, cacheSeconds));
            es.execute(new CmpSkuDubboUpdate2Worker(fetchDubboService, cmpSkuService, redisListService));
        }


        taskRunning4.set(true);
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

}
