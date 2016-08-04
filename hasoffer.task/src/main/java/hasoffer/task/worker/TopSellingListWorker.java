package hasoffer.task.worker;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2016/8/3.
 */
public class TopSellingListWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(SrmSearchLogListWorker.class);

    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> queue;

    public TopSellingListWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue) {
        this.dbm = dbm;
        this.queue = queue;
    }

    @Override
    public void run() {

        List<PtmTopSelling> topSellingList = dbm.query("SELECT t FROM PtmTopSelling t ORDER BY t.count DESC,t.lUpdateTime DESC");

        for (PtmTopSelling topSelling : topSellingList) {

            long productid = topSelling.getId();

            SrmSearchLog log = new SrmSearchLog();
            log.setPtmProductId(productid);
            queue.add(log);

            logger.info("topselling add success _" + productid);
        }

    }
}
