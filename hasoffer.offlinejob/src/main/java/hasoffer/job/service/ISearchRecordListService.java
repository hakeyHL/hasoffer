package hasoffer.job.service;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;

public interface ISearchRecordListService {
    PageableResult<SrmSearchLog> getLastNoProductLog();
}
