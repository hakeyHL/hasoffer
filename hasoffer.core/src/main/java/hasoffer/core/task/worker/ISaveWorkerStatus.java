package hasoffer.core.task.worker;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chevy on 2016/8/30.
 */
public interface ISaveWorkerStatus<S> {

    LinkedBlockingQueue<S> getSaveQueue();

    void setSaveQueue(LinkedBlockingQueue<S> saveQueue);

    boolean isSaveWorkFinished();

    void setSaveWorkFinished(boolean saveWorkFinished);

}
