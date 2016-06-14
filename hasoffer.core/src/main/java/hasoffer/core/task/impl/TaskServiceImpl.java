package hasoffer.core.task.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.thd.ThdFetchTask;
import hasoffer.core.task.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created on 2016/2/24.
 */
@Service
public class TaskServiceImpl implements ITaskService {

    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Override
    public void createTask(ThdFetchTask thdFetchTask) {
        dbm.create(thdFetchTask);
    }

    @Override
    public List<ThdFetchTask> listFetchTask() {

        StringBuilder sb = new StringBuilder("SELECT t FROM ThdFetchTask t");

        return dbm.query(sb.toString());
    }

    @Override
    public ThdFetchTask findThdFetchTaskById(long id) {
        return dbm.get(ThdFetchTask.class,id);
    }
}
