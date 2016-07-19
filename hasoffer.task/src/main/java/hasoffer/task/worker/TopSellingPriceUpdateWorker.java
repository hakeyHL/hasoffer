package hasoffer.task.worker;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
import hasoffer.core.product.IProductService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/7/19.
 */
public class TopSellingPriceUpdateWorker implements Runnable {

    private IProductService productService;
    private ListAndProcessWorkerStatus<PtmTopSelling> ws;
    private IDataBaseManager dbm;

    public TopSellingPriceUpdateWorker(IDataBaseManager dbm, ListAndProcessWorkerStatus<PtmTopSelling> ws, IProductService productService) {
        this.dbm = dbm;
        this.ws = ws;
        this.productService = productService;
    }

    @Override
    public void run() {
        while (true) {

            PtmTopSelling topSelling = ws.getSdQueue().poll();

            if (topSelling == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {

                }
                continue;
            }

            productService.updatePtmProductPrice(topSelling.getId());

            System.out.println("update success for [" + topSelling.getId() + "]");

        }
    }
}
