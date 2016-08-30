package hasoffer.core.user;

import hasoffer.core.persistence.po.urm.PriceOffNotice;

/**
 * Created on 2016/8/30.
 */
public interface IPriceOffNoticeService {

    /**
     * 创建一条降价提醒记录
     *
     * @return
     */
    boolean createPriceOffNotice(String userId, long skuid, float originPrice, float noticePrice);

    /**
     * 删除一条降价提醒记录
     *
     * @param userId
     * @param skuid
     */
    void deletePriceOffNotice(String userId, long skuid);

    /**
     * 获取一个降价提醒记录
     *
     * @param userId
     * @param skuid
     * @return
     */
    PriceOffNotice getPriceOffNotice(String userId, long skuid);


    void priceOffCheck(long skuid);
}
