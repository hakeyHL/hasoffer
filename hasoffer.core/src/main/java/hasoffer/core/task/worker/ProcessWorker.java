package hasoffer.core.task.worker;

import hasoffer.core.worker.ListAndProcessWorkerStatus;

import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/3
 * Function :
 */
public class ProcessWorker<T> implements Runnable {

    ListAndProcessWorkerStatus<T> ws;
    IProcess<T> processor;

    public ProcessWorker(ListAndProcessWorkerStatus<T> ws,
                         IProcess<T> processor) {
        this.ws = ws;
        this.processor = processor;
    }

    @Override
    public void run() {
        while (true) {
            T t = ws.getSdQueue().poll();
            if (t == null) {
                if (ws.isListWorkFinished()) {
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                continue;
            }

            processor.process(t);
        }
    }
}
