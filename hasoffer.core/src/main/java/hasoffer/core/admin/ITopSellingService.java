package hasoffer.core.admin;

import hasoffer.core.persistence.po.ptm.PtmTopSelling;

import java.util.List;

/**
 * Created on 2016/7/6.
 */
public interface ITopSellingService {

    /**
     * 该方法用来按照时间查询一段时间内的top selling的数据,该方法最多返回20条
     *
     * @param longStartTime
     * @param longEndTime
     * @return
     */
    List<PtmTopSelling> findTopSellingListByDate(long longStartTime, Long longEndTime);

}
