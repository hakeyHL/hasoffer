package hasoffer.job.service.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.job.service.ISearchRecordListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("searchRecordListService")
public class SearchRecordListServiceImpl implements ISearchRecordListService {

    private Logger logger = LoggerFactory.getLogger(SearchRecordListServiceImpl.class);

    @Resource
    private IDataBaseManager dbm;

    @DataSource(DataSourceType.Slave)
    @Override
    public PageableResult<SrmSearchLog> getLastNoProductLog() {
        String SQL_SEARCHLOG = "select t from SrmSearchLog t where  t.ptmProductId=0  order by t.updateTime desc ";
        if (dbm != null) {
            logger.info("query search log.");
            return dbm.queryPage(SQL_SEARCHLOG, 1, 1000);
        }
        return null;
    }
}
