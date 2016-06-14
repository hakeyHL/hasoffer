package hasoffer.core.task;


import hasoffer.core.persistence.po.thd.ThdFetchTask;

import java.util.List;

/**
 * Created on 2016/2/24.
 */
public interface ITaskService {

    void createTask(ThdFetchTask thdFetchTask);

    List<ThdFetchTask> listFetchTask();

    ThdFetchTask findThdFetchTaskById(long id);

}
