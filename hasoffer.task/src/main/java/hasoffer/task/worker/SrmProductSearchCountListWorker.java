package hasoffer.task.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/5/13.
 */
public class SrmProductSearchCountListWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(SrmProductSearchCountListWorker.class);

    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> queue;

    public SrmProductSearchCountListWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue) {
        this.dbm = dbm;
        this.queue = queue;
    }

    @Override
    public void run() {

        while (true) {

            String startDateString = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_DAY, "yyyyMMdd");

            String Q_LOG_BYUPDATETIME = "SELECT t FROM SrmProductSearchCount  t WHERE t.ymd > ?0 AND t.count > 10 ORDER BY t.id ASC";

            PageableResult<SrmProductSearchCount> pageableResult = dbm.queryPage(Q_LOG_BYUPDATETIME, 0, 1000, Arrays.asList(startDateString));
            List<SrmProductSearchCount> dataList = pageableResult.getData();

            if (ArrayUtils.hasObjs(dataList)) {

                //暂时先拼凑一个srmsearchlog用于适配更新的接口
                for (SrmProductSearchCount log : dataList) {

                    SrmSearchLog srmSearchLog = new SrmSearchLog();

                    log.setProductId(log.getProductId());

                    queue.add(srmSearchLog);
                }

                logger.info("add succes size = " + dataList.size());
            } else {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (queue.size() > 10000) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
