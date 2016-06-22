package hasoffer.task.controller;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.task.worker.CmpSkuDubboUpdateWorker;
import hasoffer.task.worker.MysqlListWorker;
import org.springframework.stereotype.Controller;
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
    private static final String Q_PTMCMPSKU_FLIPKART = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' ORDER BY t.id ";

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;

    @RequestMapping(value = "/flipkartupdatestart", method = RequestMethod.GET)
    @ResponseBody
    public String flipkartupdatestart() {

        if (taskRunning1.get()) {
            return "task running.";
        }

        ExecutorService es = Executors.newCachedThreadPool();

        ListAndProcessWorkerStatus<PtmCmpSku> ws = new ListAndProcessWorkerStatus<PtmCmpSku>();

        es.execute(new MysqlListWorker<PtmCmpSku>(Q_PTMCMPSKU_FLIPKART, ws, dbm));

        for (int i = 0; i < 10; i++) {
            es.execute(new CmpSkuDubboUpdateWorker(ws, cmpSkuService, fetchDubboService));
        }

        taskRunning1.set(true);

        return "ok";
    }

}
