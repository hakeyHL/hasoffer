package hasoffer.task.worker;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/5/3.
 */
public class MongoSkuInitWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(MongoSkuInitWorker.class);

    private ListAndProcessWorkerStatus<PtmCmpSku> ws;
    private IMongoDbManager mdm;
    private ICmpSkuService cmpSkuService;

    public MongoSkuInitWorker(ListAndProcessWorkerStatus<PtmCmpSku> ws, IMongoDbManager mdm, ICmpSkuService cmpSkuService) {
        this.ws = ws;
        this.mdm = mdm;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {
        while (true) {
            PtmCmpSku cmpSku = ws.getSdQueue().poll();

            if (cmpSku == null) {
                if (ws.isListWorkFinished() && ws.getSdQueue().size() == 0) {
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }

//            if (Website.FLIPKART.equals(cmpSku.getWebsite()) || Website.SNAPDEAL.equals(cmpSku.getWebsite())) {
//                if(!StringUtils.isEmpty(cmpSku.getOriUrl())){
//                    cmpSkuService.createPtmCmpSkuIndexToMongo(cmpSku);
//                }
//            }

            if (Website.SHOPCLUES.equals(cmpSku.getWebsite()) || Website.EBAY.equals(cmpSku.getWebsite())||Website.INFIBEAM.equals(cmpSku.getWebsite())) {
                SummaryProduct summaryProduct = new SummaryProduct();
                summaryProduct.setId(cmpSku.getId());
                summaryProduct.setUrl(cmpSku.getUrl());
                summaryProduct.setWebsite(cmpSku.getWebsite());

                mdm.save(summaryProduct);
                logger.debug(cmpSku.getId() + " init success");
            }
        }
        logger.debug("work finished.");
    }

}
