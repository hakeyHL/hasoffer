package hasoffer.admin.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.task.worker.impl.ListProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created  on 2016/5/27.
 */
//改worker可以根据指定的sql一直while，true list
public class MysqlListWorker2 implements Runnable {

    private Logger logger = LoggerFactory.getLogger(MysqlListWorker2.class);
    private String queryString;
    private IDataBaseManager dbm;
    private ListProcessWorkerStatus ws;
    private long startId;

    public MysqlListWorker2(String queryString, ListProcessWorkerStatus ws, IDataBaseManager dbm) {
        this.queryString = queryString;
        this.ws = ws;
        this.dbm = dbm;
    }

    public MysqlListWorker2(String queryString, ListProcessWorkerStatus ws, IDataBaseManager dbm, long startId) {
        this.queryString = queryString;
        this.ws = ws;
        this.dbm = dbm;
        this.startId = startId;
    }

    @Override
    public void run() {

        int page = 1;
        int pageSize = 1000;

        PageableResult pagedResults = null;

        if (startId == 0) {
            pagedResults = dbm.queryPage(queryString, page, pageSize);
        } else {
            pagedResults = dbm.queryPage(queryString, page, pageSize, Arrays.asList(startId));
        }


        long totalPage = pagedResults.getTotalPage();

        while (page <= totalPage) {

            if (page > 1) {
                if (startId == 0) {
                    pagedResults = dbm.queryPage(queryString, page, pageSize);
                } else {
                    pagedResults = dbm.queryPage(queryString, page, pageSize, Arrays.asList(startId));
                }
            }

            List cmpSkus = pagedResults.getData();

            if (ws.getSdQueue().size() > 2000) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }

            ws.getSdQueue().addAll(cmpSkus);
            page++;
        }

    }
}
