package hasoffer.core.admin.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.ITopSellingService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Created on 2016/7/6.
 */
@Service
public class TopSellingServiceImpl implements ITopSellingService {

    private static final String Q_TOPSELLING_BYDATE1 = "SELECT t FROM PtmTopSelling t WHERE t.ymd >= ?0 ";
    private static final String Q_TOPSELLING_BYDATE2 = "SELECT t FROM PtmTopSelling t WHERE t.ymd > ?0 AND t.ymd < ?1";

    @Resource
    IDataBaseManager dbm;

    @Override
    public PageableResult<PtmTopSelling> findTopSellingListByDate(long longStartTime, Long longEndTime, int page, int size) {

        String startTimeString = TimeUtils.parse(longStartTime, "yyyyMMdd");
        String endTimeString = longEndTime == null ? "" : TimeUtils.parse(longEndTime, "yyyymmdd");

        PageableResult<PtmTopSelling> pageableResult;

        if (longEndTime == null) {
            pageableResult = dbm.queryPage(Q_TOPSELLING_BYDATE1, page, size, Arrays.asList(startTimeString));
        } else {
            pageableResult = dbm.queryPage(Q_TOPSELLING_BYDATE2, page, size, Arrays.asList(startTimeString, endTimeString));
        }

        return pageableResult;
    }
}
