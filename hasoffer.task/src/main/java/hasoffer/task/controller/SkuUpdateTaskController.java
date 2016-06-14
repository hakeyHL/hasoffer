package hasoffer.task.controller;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.*;
import hasoffer.core.search.ISearchService;
import hasoffer.task.worker.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Date : 2016/4/11
 * Function :
 */
@Controller
@RequestMapping(value = "/skuupdatetask")
public class SkuUpdateTaskController {

    private static AtomicBoolean taskRunning1 = new AtomicBoolean(false);
    private static AtomicBoolean taskRunning2 = new AtomicBoolean(false);
    private static AtomicBoolean taskRunning3 = new AtomicBoolean(false);
    private static AtomicBoolean taskRunning4 = new AtomicBoolean(false);

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    ISummaryProductService summaryProductService;
    @Resource
    IProductService productService;
    @Resource
    ISearchService searchService;
    @Resource
    IFetchService fetchService;
    @Resource
    ICmpSkuUpdateStatService cmpSkuUpdateStatService;


    //skuupdatetask/visitupdate
    @RequestMapping(value = "/visitupdate", method = RequestMethod.GET)
    @ResponseBody
    public String visitUpdate() {

        ConcurrentLinkedQueue<SrmSearchLog> logQueue = new ConcurrentLinkedQueue<SrmSearchLog>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new SrmSearchLogListWorker(dbm, logQueue));

        for (int i = 0; i < 20; i++) {
            es.execute(new CmpSkuVisitUpdateWorker(dbm, fetchService, cmpSkuService, cmpSkuUpdateStatService, logQueue));
        }

        return "ok";
    }


    //skuupdatetask/start
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public
    @ResponseBody
    String f1() {
        if (taskRunning1.get()) {
            return "task running.";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String Q_CMPSKU_FLIPKART = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' ";
        String Q_CMPSKU_SNAPDEAL = "SELECT t FROM PtmCmpSku t WHERE t.website = 'SNAPDEAL' ";
        String Q_CMPSKU_PAYTM = "SELECT t FROM PtmCmpSku t WHERE t.website = 'PAYTM' ";

        ExecutorService es = Executors.newCachedThreadPool();

        // cmp sku 更新
        ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        es.execute(new CmpSkuListWorker(dbm, skuQueue, Q_CMPSKU_FLIPKART));
        es.execute(new CmpSkuListWorker(dbm, skuQueue, Q_CMPSKU_SNAPDEAL));
        es.execute(new CmpSkuListWorker(dbm, skuQueue, Q_CMPSKU_PAYTM));

        for (int i = 0; i < 60; i++) {
            es.execute(new CmpSkuUpdateWorker(skuQueue, cmpSkuService, fetchService));
        }

        taskRunning1.set(true);

        return "";
    }


    @RequestMapping(value = "/startamazon", method = RequestMethod.GET)
    public
    @ResponseBody
    String f2() {
        if (taskRunning2.get()) {
            return "task running.";
        }

        String Q_CMPSKU_AMAZON =
                "SELECT t FROM PtmCmpSku t " +
                        "WHERE t.website = 'AMAZON' " +
                        " ORDER BY t.id ";

        ExecutorService es = Executors.newCachedThreadPool();

        // cmp sku 更新
        ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();
        es.execute(new CmpSkuListWorker(dbm, skuQueue, Q_CMPSKU_AMAZON));

        //amazon改为单线程更新，控制访问时间
        es.execute(new CmpSkuUpdateWorker(skuQueue, cmpSkuService, fetchService));


        taskRunning2.set(true);

        return "";
    }

    @RequestMapping(value = "/startflipkartfetch", method = RequestMethod.GET)
    public
    @ResponseBody
    String f3() {
        if (taskRunning3.get()) {
            return "task running.";
        }

        String Q_CMPSKU_FLIPKART =
                "SELECT t FROM PtmCmpSku t " +
//                        "WHERE t.website = 'FLIPKART' " +
                        " ORDER BY t.id ";

        ExecutorService es = Executors.newCachedThreadPool();

        // flipkart sku fetch and save mongo
        ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();
        es.execute(new CmpSkuListWorker(dbm, skuQueue, Q_CMPSKU_FLIPKART));
        for (int i = 0; i < 100; i++) {
            es.execute(new FlipakartSkuSaveWorker(skuQueue, mdm));
        }

        taskRunning3.set(true);

        return "";
    }

    //skuupdatetask/startseimitupdate
    @RequestMapping(value = "/startseimitupdate", method = RequestMethod.GET)
    public
    @ResponseBody
    String f4() {

        if (taskRunning4.get()) {
            return "task running.";
        }

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<SummaryProduct> skuQueue = new ConcurrentLinkedQueue<SummaryProduct>();

        AtomicLong listCount = new AtomicLong(0);
        AtomicLong updateCount = new AtomicLong(0);

        es.execute(new PtmCmpSkuFetchResultListWorker(summaryProductService, skuQueue, listCount));
        for (int i = 0; i < 30; i++) {
            es.execute(new PtmCmpSkuFetchResultUpdateWorker(skuQueue, summaryProductService, updateCount, dbm));
        }

        taskRunning4.set(true);

        return "";
    }

//    @RequestMapping(value = "/seimi", method = RequestMethod.GETpa)
//    @ResponseBody
//    public String seimiupdateamazon() {
//
//        Seimi s = new Seimi();
//
//        s.start("FlipkartSkuDocFetch");
//
//        ExecutorService es = Executors.newCachedThreadPool();
//
//        for (int i = 0; i < 10; i++) {
//            es.execute(new FlipkartSkuDocSaveWorker(mdm));
//        }
//
//        return "";
//    }
}
