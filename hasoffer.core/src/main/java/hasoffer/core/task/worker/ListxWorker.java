package hasoffer.core.task.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/3
 * Function :
 */
public class ListxWorker<T> implements Runnable {

    ListAndProcessWorkerStatus<T> ws;
    IList list;
    private Logger logger = LoggerFactory.getLogger(ListxWorker.class);

    public ListxWorker(ListAndProcessWorkerStatus<T> ws, IList list) {
        this.ws = ws;
        this.list = list;
    }

    @Override
    public void run() {
        int page = 1;

        PageableResult<T> pagedDatas = list.getData(page);

        final long TOTAL_PAGE = pagedDatas.getTotalPage();

        while (page <= TOTAL_PAGE || list.isRunForever()) {

            if (ws.getSdQueue().size() > 3000) {
                try {
                    logger.debug("sd queue size = " + ws.getSdQueue().size());
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                continue;
            }

            System.out.println(String.format(" list page (%d/%d)", page, TOTAL_PAGE));

            List<T> datas;

            if (page > 1) {
                try {
                    pagedDatas = list.getData(page);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                    continue;
                }
            }
            datas = pagedDatas.getData();

            if (ArrayUtils.hasObjs(datas)) {
                ws.getSdQueue().addAll(datas);
            } else {
                try {
                    logger.debug("sd queue size = " + ws.getSdQueue().size());
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
            }

            page++;
        }

        ws.setListWorkFinished(true);
    }
}
