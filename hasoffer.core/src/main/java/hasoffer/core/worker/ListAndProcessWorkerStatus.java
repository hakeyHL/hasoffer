package hasoffer.core.worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/4/22
 * Function :
 */
public class ListAndProcessWorkerStatus<T> {

    private LinkedBlockingQueue<T> sdQueue;
    private boolean listWorkFinished;

    private AtomicInteger count;

    public ListAndProcessWorkerStatus() {
        sdQueue = new LinkedBlockingQueue<T>();
        listWorkFinished = false;
        count = new AtomicInteger(0);
    }

    public int addCount(int c){
        return count.addAndGet(c);
    }

    public int getCount(){
        return count.get();
    }

    public LinkedBlockingQueue<T> getSdQueue() {
        return sdQueue;
    }

    public void setSdQueue(LinkedBlockingQueue<T> sdQueue) {
        this.sdQueue = sdQueue;
    }

    public boolean isListWorkFinished() {
        return listWorkFinished;
    }

    public void setListWorkFinished(boolean listWorkFinished) {
        this.listWorkFinished = listWorkFinished;
    }

}
