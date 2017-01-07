package hasoffer.core.user;

import hasoffer.base.model.Website;
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

    /**
     * 根据id获取一条降价提醒记录
     *
     * @param priceOffNoticeId
     * @return
     */
    PriceOffNotice getPriceOffNotice(long priceOffNoticeId);

    void updatePriceOffNoticeStatus(long id, boolean lastPushStatus);

    //针对关注某个skuid的所有用户，检查价格是否需要推送
    void priceOffCheck(long skuid);

    //针对push失败的用户重新发送
    boolean pushFailRePush(long id, boolean cacheFail);

    /**
     * @param nowPrice
     * @param website
     * @param url
     * @param fetchedTitle
     * @param priceOffNoticeId
     * @param addToPriceOffNoticeQueue 如果是第一次发送，true表示第一次发送，false表示重发，不加入新push队列
     * @return
     */
    boolean priceOffNoticeSinglePush(float nowPrice, Website website, String url, String fetchedTitle, long priceOffNoticeId, boolean addToPriceOffNoticeQueue);
}
