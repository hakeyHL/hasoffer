package hasoffer.core.task.worker.impl;

import hasoffer.core.task.worker.ISaveWorkerStatus;
import hasoffer.core.task.worker.ISaver;

import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/3
 * Function :
 */
public class SaveWorker<S> implements Runnable {

    ISaveWorkerStatus<S> ws;
    ISaver<S> saver;

    public SaveWorker(ISaveWorkerStatus<S> ws,
                      ISaver<S> saver) {
        this.ws = ws;
        this.saver = saver;
    }

    @Override
    public void run() {
        while (true) {
            S t = ws.getSaveQueue().poll();

            if (t == null) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                continue;
            } else {
                saver.save(t);
            }
        }
    }
}
