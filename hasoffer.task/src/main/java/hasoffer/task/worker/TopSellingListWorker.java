package hasoffer.task.worker;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

//        List<PtmTopSelling> topSellingList = dbm.query("SELECT t FROM PtmTopSelling t ORDER BY t.count DESC,t.lUpdateTime DESC");

//        for (PtmTopSelling topSelling : topSellingList) {
        List<Long> idList = new ArrayList<>();

        idList.add(1742371L);
        idList.add(1866396L);
        idList.add(3198L);
        idList.add(1897310L);
        idList.add(1762335L);

        for (Long productid : idList) {

//             productid = topSelling.getId();

            SrmSearchLog log = new SrmSearchLog();
            log.setPtmProductId(productid);
            queue.add(log);

            logger.info("topselling add success _" + productid);
        }

    }
}
