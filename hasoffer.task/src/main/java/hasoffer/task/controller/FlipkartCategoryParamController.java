package hasoffer.task.controller;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import hasoffer.task.worker.FKCateAndParamWorker;
import hasoffer.task.worker.MysqlListWorker;
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
 * Created on 2016/6/20.
 */
@Controller
@RequestMapping(value = "/flipkart")
public class FlipkartCategoryParamController {

    private static final String Q_FLIPKART_CMP = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' ORDER BY t.id";
    private static AtomicBoolean taskRunning1 = new AtomicBoolean(false);
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ICategoryService categoryService;

    //flipkart/cateandparam
    @RequestMapping(value = "/cateandparam", method = RequestMethod.GET)
    @ResponseBody
    public String getFKCategoryParam() {

        if (taskRunning1.get()) {
            return "task running.";
        }

//        ConcurrentLinkedQueue<SrmSearchLog> logQueue = new ConcurrentLinkedQueue<SrmSearchLog>();

        ExecutorService es = Executors.newCachedThreadPool();

        ListAndProcessWorkerStatus<PtmCmpSku> ws = new ListAndProcessWorkerStatus<PtmCmpSku>();

        es.execute(new MysqlListWorker<PtmCmpSku>(Q_FLIPKART_CMP, ws, dbm));

        for (int i = 0; i < 20; i++) {
            es.execute(new FKCateAndParamWorker(dbm, ws, categoryService));// mdm,
        }

        taskRunning1.set(true);

        return "ok";
    }
}
