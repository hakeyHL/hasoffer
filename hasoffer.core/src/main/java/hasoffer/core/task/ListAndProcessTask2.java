package hasoffer.core.task;

import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.core.task.worker.ListxWorker;
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
public class ListAndProcessTask2<T> {

    IList list;
    IProcess processor;
    int processorCount = 20;
    long queueMaxSize = 3000;
    private Logger logger = LoggerFactory.getLogger(ListAndProcessTask2.class);

    public ListAndProcessTask2(IList list,
                               IProcess processor) {
        this.list = list;
        this.processor = processor;
    }

    public void go() {
        ListAndProcessWorkerStatus<T> ws = new ListAndProcessWorkerStatus<T>();

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new ListxWorker<T>(ws, list, queueMaxSize));

        for (int i = 0; i < processorCount; i++) {
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

    public int getProcessorCount() {
        return processorCount;
    }

    public void setProcessorCount(int processorCount) {
        this.processorCount = processorCount;
    }

    public long getQueueMaxSize() {
        return queueMaxSize;
    }

    public void setQueueMaxSize(long queueMaxSize) {
        this.queueMaxSize = queueMaxSize;
    }
}
