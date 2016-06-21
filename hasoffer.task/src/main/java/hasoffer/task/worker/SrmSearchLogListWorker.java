package hasoffer.task.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.fetch.helper.WebsiteHelper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/5/13.
 */
public class SrmSearchLogListWorker implements Runnable {

    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> queue;

    public SrmSearchLogListWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue) {
        this.dbm = dbm;
        this.queue = queue;
    }

    @Override
    public void run() {

        //设置起始时间，起始时间的初始值是当前时间的24小时前
        Date startDate = TimeUtils.addDay(TimeUtils.nowDate(), -1);

        while (true) {

            String startDateString = TimeUtils.parse(startDate, "yyyy-MM-dd HH:mm:ss");

            String Q_LOG_BYUPDATETIME = "SELECT t FROM SrmSearchLog t WHERE t.updateTime > '" + startDateString + "' ORDER BY t.updateTime ASC";

            PageableResult<SrmSearchLog> pageableResult = dbm.queryPage(Q_LOG_BYUPDATETIME, 0, 1000);
            List<SrmSearchLog> dataList = pageableResult.getData();

            if (ArrayUtils.hasObjs(dataList)) {
                startDate = dataList.get(dataList.size() - 1).getUpdateTime();

                for (SrmSearchLog log : dataList) {

                    Website website = Website.valueOf(log.getSite());
                    if (WebsiteHelper.DEFAULT_WEBSITES.contains(website)) {
                        queue.add(log);
                    }
                }
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
