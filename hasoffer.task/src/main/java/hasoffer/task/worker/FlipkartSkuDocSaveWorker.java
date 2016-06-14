package hasoffer.task.worker;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuFetchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/4/27.
 */
public class FlipkartSkuDocSaveWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(FlipkartSkuDocSaveWorker.class);
    private static ConcurrentLinkedQueue<PtmCmpSkuFetchResult> resultQueue = new ConcurrentLinkedQueue<PtmCmpSkuFetchResult>();
    private IMongoDbManager mdm;

    public FlipkartSkuDocSaveWorker(IMongoDbManager mdm) {
        this.mdm = mdm;
    }

    public static void add(PtmCmpSkuFetchResult ptmCmpSkuFetchResult) {
        resultQueue.add(ptmCmpSkuFetchResult);
    }

    @Override
    public void run() {

        while (true) {

            PtmCmpSkuFetchResult fetchResult = resultQueue.poll();
            if (fetchResult == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    logger.debug("update job has no jobs. go to sleep!");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            mdm.save(fetchResult);
            logger.debug("save success for [" + fetchResult.getId() + "]");
        }

    }
}
