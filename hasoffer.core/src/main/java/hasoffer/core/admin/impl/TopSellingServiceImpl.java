package hasoffer.core.admin.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.ITopSellingService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmSearchCount;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2016/7/6.
 */
@Service
public class TopSellingServiceImpl implements ITopSellingService {

    private static final String Q_TOPSELLING_BYDATE1 = "SELECT t FROM SrmSearchCount t WHERE t.ymd > ?0 ";
    private static final String Q_TOPSELLING_BYDATE2 = "SELECT t FROM SrmSearchCount t WHERE t.ymd > ?0 AND t.ymd < ?1";

    @Resource
    IDataBaseManager dbm;

    @Override
    public List<SrmSearchCount> findTopSellingListByDate(long longStartTime, Long longEndTime) {

        String startTimeString = TimeUtils.parse(longStartTime, "yyyyMMdd");
        String endTimeString = longEndTime == null ? "" : TimeUtils.parse(longEndTime, "yyyymmdd");

        PageableResult<SrmSearchCount> pageableResult;

        if (longEndTime == null) {
            pageableResult = dbm.queryPage(Q_TOPSELLING_BYDATE1, 1, 20, Arrays.asList(startTimeString));
        } else {
            pageableResult = dbm.queryPage(Q_TOPSELLING_BYDATE2, 1, 20, Arrays.asList(startTimeString, endTimeString));
        }

        List<SrmSearchCount> topSellingList = pageableResult.getData();

        return topSellingList;
    }
}
