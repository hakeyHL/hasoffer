package hasoffer.core.task.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.worker.ListAndProcessWorkerStatus;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/3
 * Function :
 */
public class ListWorker<T> implements Runnable {

    IDataBaseManager dbm;

    ListAndProcessWorkerStatus<T> ws;
    String sql;

    public ListWorker(ListAndProcessWorkerStatus<T> ws,
                      IDataBaseManager dbm,
                      String sql) {
        this.dbm = dbm;
        this.ws = ws;
        this.sql = sql;
    }

    @Override
    public void run() {
        int page = 1;
        final int PAGE_SIZE = 2000;

        PageableResult<T> pagedDatas = dbm.queryPage(sql, page, PAGE_SIZE);

        final long TOTAL_PAGE = pagedDatas.getTotalPage();

        while (page <= TOTAL_PAGE) {
            if (ws.getSdQueue().size() > 3000) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                continue;
            }

            System.out.println(String.format(" list page (%d/%d)", page, TOTAL_PAGE));

            List<T> datas;

            if (page == 1) {
                datas = pagedDatas.getData();
            } else {
                datas = dbm.query(sql, page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(datas)) {
                ws.getSdQueue().addAll(datas);
            }

            page++;
        }

        ws.setListWorkFinished(true);
    }
}
