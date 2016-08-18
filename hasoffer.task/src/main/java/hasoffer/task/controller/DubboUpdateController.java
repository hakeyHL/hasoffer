package hasoffer.task.controller;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.task.worker.CmpSkuDubboUpdateWorker;
import hasoffer.task.worker.SrmProductSearchCountListWorker;
import hasoffer.task.worker.TopSellingListWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
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

    //dubbofetchtask/updatestart
    @RequestMapping(value = "/updatestart", method = RequestMethod.GET)
    @ResponseBody
    public String updatestart() {

        if (taskRunning1.get()) {
            return "task running.";
        }

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new SrmProductSearchCountListWorker(dbm, queue, fetchDubboService));

        //保证list任务优先执行
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 100; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, productService, cmpSkuService));
        }

        taskRunning1.set(true);

        return "ok";
    }

    //dubbofetchtask/updateTopSellingSpec
    @RequestMapping(value = "/updateTopSellingSpec", method = RequestMethod.GET)
    @ResponseBody
    public String updateTopSellingSpec() {

        if (taskRunning2.get()) {
            return "task running.";
        }

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> queue = new ConcurrentLinkedQueue<>();

        es.execute(new TopSellingListWorker(dbm, queue, fetchDubboService));

        for (int i = 0; i < 30; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, productService, cmpSkuService));
        }

        taskRunning2.set(true);

        return "ok";
    }
}
