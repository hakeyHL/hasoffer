package hasoffer.core.task;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.task.worker.IProcess;
import hasoffer.core.task.worker.ListWorker;
import hasoffer.core.task.worker.ProcessWorker;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/3
 * Function :
 */
public class ListAndProcessTask<T> {

    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(ListAndProcessTask.class);

    public ListAndProcessTask(IDataBaseManager dbm) {
        this.dbm = dbm;
    }

    public void go(String sql, IProcess processor) {
        ListAndProcessWorkerStatus<T> ws = new ListAndProcessWorkerStatus<T>();

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new ListWorker(ws, dbm, sql));
        for (int i = 0; i < 20; i++) {
            es.execute(new ProcessWorker(ws, processor));
        }

        while (true) {
            if (ws.getSdQueue().size() == 0 && ws.isListWorkFinished()) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
            continue;
        }

        logger.debug("work finished.");
    }

}
