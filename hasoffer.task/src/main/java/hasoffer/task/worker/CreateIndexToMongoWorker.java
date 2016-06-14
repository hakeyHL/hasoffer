package hasoffer.task.worker;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/5/17.
 */
public class CreateIndexToMongoWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(MongoSkuInitWorker.class);

    private ListAndProcessWorkerStatus<PtmCmpSku> ws;
    private IMongoDbManager mdm;
    private ICmpSkuService cmpSkuService;

    public CreateIndexToMongoWorker(ListAndProcessWorkerStatus<PtmCmpSku> ws, IMongoDbManager mdm,ICmpSkuService cmpSkuService) {
        this.ws = ws;
        this.mdm = mdm;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        while(true){

            PtmCmpSku sku = ws.getSdQueue().poll();

            if (sku == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    logger.debug("CreateIndexToMongoWorker has no jobs. go to sleep!");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            cmpSkuService.createPtmCmpSkuIndexToMongo(sku);

        }

    }

}
