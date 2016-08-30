package hasoffer.core.task.worker.impl;

import hasoffer.core.task.worker.IListWorkerStatus;
import hasoffer.core.task.worker.ISaveWorkerStatus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/4/22
 * Function :
 */
public class ListSaveWorkerStatus<L, S> implements IListWorkerStatus<L>, ISaveWorkerStatus<S> {

    private LinkedBlockingQueue<L> sdQueue;
    private LinkedBlockingQueue<S> saveQueue;
    private boolean listWorkFinished;
    private boolean saveWorkFinished;

    private AtomicInteger count;

    public ListSaveWorkerStatus() {
        count = new AtomicInteger(0);
        sdQueue = new LinkedBlockingQueue<L>();
        saveQueue = new LinkedBlockingQueue<S>();
        listWorkFinished = false;
        saveWorkFinished = false;
    }

    public int addCount(int c) {
        return count.addAndGet(c);
    }

    public int getCount() {
        return count.get();
    }

    public LinkedBlockingQueue<L> getSdQueue() {
        return sdQueue;
    }

    public void setSdQueue(LinkedBlockingQueue<L> sdQueue) {
        this.sdQueue = sdQueue;
    }

    public boolean isListWorkFinished() {
        return listWorkFinished;
    }

    public void setListWorkFinished(boolean listWorkFinished) {
        this.listWorkFinished = listWorkFinished;
    }

    public LinkedBlockingQueue<S> getSaveQueue() {
        return saveQueue;
    }

    public void setSaveQueue(LinkedBlockingQueue<S> saveQueue) {
        this.saveQueue = saveQueue;
    }

    public boolean isSaveWorkFinished() {
        return saveWorkFinished;
    }

    public void setSaveWorkFinished(boolean saveWorkFinished) {
        this.saveWorkFinished = saveWorkFinished;
    }
}
