package hasoffer.job.controller;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IFetchService;
import hasoffer.job.worker.CmpSkuVisitUpdateWorker;
import hasoffer.job.worker.SrmSearchLogListWorker;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 2016/6/21.
 */
@Controller
@RequestMapping(value = "/ptmcmpskuupdate")
public class PtmCmpSkuUpdateController {

    private static AtomicBoolean taskRunning1 = new AtomicBoolean(false);

    @Resource
    IDataBaseManager dbm;
    @Resource
    IFetchService fetchService;
    @Resource
    ICmpSkuService cmpSkuService;

    @RequestMapping(value = "/startflipkart", method = RequestMethod.GET)
    @ResponseBody
    public String startflipkart() {
        if (taskRunning1.get()) {
            return "task running.";
        }

        ConcurrentLinkedQueue<SrmSearchLog> logQueue = new ConcurrentLinkedQueue<SrmSearchLog>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new SrmSearchLogListWorker(dbm, logQueue));

        for (int i = 0; i < 10; i++) {
            es.execute(new CmpSkuVisitUpdateWorker(dbm,fetchService,cmpSkuService,logQueue));
        }

        taskRunning1.set(true);
        return "ok";
    }

}
