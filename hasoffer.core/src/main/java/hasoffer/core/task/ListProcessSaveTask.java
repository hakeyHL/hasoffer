package hasoffer.core.task;

import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import hasoffer.core.task.worker.ISaver;
import hasoffer.core.task.worker.impl.ListSaveWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/3
 * Function :
 */
public class ListProcessSaveTask<L, S> {

    ILister list;
    IProcessor processor;
    ISaver saver;

    int processorCount = 20;
    int saverCount = 2;
    long queueMaxSize = 3000;

    private Logger logger = LoggerFactory.getLogger(ListProcessSaveTask.class);

    public ListProcessSaveTask(ILister list,
                               IProcessor processor,
                               ISaver saver) {
        this.list = list;
        this.processor = processor;
        this.saver = saver;
    }

    public void go() {
        ListSaveWorkerStatus<L, S> ws = new ListSaveWorkerStatus<L, S>();

        ExecutorService es = Executors.newCachedThreadPool();
//        es.execute(new ListxWorker<L>(ws, list, queueMaxSize));

        for (int i = 0; i < processorCount; i++) {
//            es.execute(new ProcessWorker(ws, processor));
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
