package hasoffer.timer.product.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/21
 * Function :
 */
public class CmpSkuListWorker implements Runnable {

    private static final String Q_PTM_CMPSKU =
            "SELECT t FROM PtmCmpSku t " +
                    " ORDER BY t.id ";
    private Logger logger = LoggerFactory.getLogger(CmpSkuListWorker.class);
    private IDataBaseManager dbm;
    private ICmpSkuService cmpSkuService;
    private ConcurrentLinkedQueue<PtmCmpSku> skuQueue;

    public CmpSkuListWorker(IDataBaseManager dbm, ICmpSkuService cmpSkuService, ConcurrentLinkedQueue<PtmCmpSku> skuQueue) {
        this.dbm = dbm;
        this.cmpSkuService = cmpSkuService;
        this.skuQueue = skuQueue;
    }

    public void run() {
        while (true) {
            long today = TimeUtils.today();

            r();

            long sleep = TimeUtils.now() - today;
            if (sleep < TimeUtils.MILLISECONDS_OF_1_DAY) {
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
    }

    public void r() {
        int pageNum = 1, PAGE_SIZE = 500;

        PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_PTM_CMPSKU, pageNum, PAGE_SIZE);

        int pageCount = (int) pageableResult.getTotalPage();

        List<PtmCmpSku> cmpSkus = pageableResult.getData();
        while (pageNum <= pageCount) {

            if (skuQueue.size() > 600) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            logger.info(String.format("update sku : %d/%d .", pageNum, pageCount));

            if (pageNum > 1) {
                cmpSkus = dbm.query(Q_PTM_CMPSKU, pageNum, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(cmpSkus)) {
                for (PtmCmpSku cmpSku : cmpSkus) {
                    if (cmpSku.getUpdateTime().getTime() < TimeUtils.today()) {
                        skuQueue.add(cmpSku);
                    }
                }
            }

            pageNum++;
        }
    }
}
