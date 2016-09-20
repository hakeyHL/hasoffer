package hasoffer.core.user.impl;

import com.alibaba.fastjson.JSONObject;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.push.*;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.persistence.po.urm.updater.PriceOffNoticeUpdater;
import hasoffer.core.push.IPushService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.fetch.helper.WebsiteHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2016/8/30.
 */
@Service
public class PriceOffNoticeServiceImpl implements IPriceOffNoticeService {

    private static final String QUERY_PRICEOFF_BY_USERID_SKUID = "SELECT t FROM PriceOffNotice t WHERE t.userid = ?0 and t.skuid = ?1 ";
    private static final String QUERY_PRICEOFF_BY_SKUID = "SELECT t FROM PriceOffNotice t WHERE t.skuid = ?0 ";
    private static final String QUERY_DEVICE_BY_USERID = "SELECT t FROM UrmUserDevice t WHERE t.userId = ?0 ";
    private static final String PUSH_FAIL_PRICEOFFNOTICE_ID = "PUSH_FAIL_PRICEOFFNOTICE_ID";

    @Resource
    IDataBaseManager dbm;
    @Resource
    IPushService pushService;
    @Resource
    IRedisListService redisListService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPriceOffNotice(String userId, long skuid, float originPrice, float noticePrice) {

        PriceOffNotice priceOffNotice = getPriceOffNotice(userId, skuid);

        if (priceOffNotice != null) {
            return false;
        } else {

            priceOffNotice = new PriceOffNotice();

            priceOffNotice.setUserid(userId);
            priceOffNotice.setSkuid(skuid);
            priceOffNotice.setOriginPrice(originPrice);
            //如果提醒价格小等于0，设置提醒价格为关注价格
            if (noticePrice <= 0.0) {
                priceOffNotice.setNoticePrice(originPrice);
            } else {
                priceOffNotice.setNoticePrice(noticePrice);
            }

            dbm.create(priceOffNotice);

            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePriceOffNotice(String userId, long skuid) {

        PriceOffNotice priceOffNotice = getPriceOffNotice(userId, skuid);

        if (priceOffNotice == null) {
            return;
        }

        dbm.delete(PriceOffNotice.class, priceOffNotice.getId());
    }

    @Override
    public PriceOffNotice getPriceOffNotice(String userId, long skuid) {
        return dbm.querySingle(QUERY_PRICEOFF_BY_USERID_SKUID, Arrays.asList(userId, skuid));
    }

    @Override
    public PriceOffNotice getPriceOffNotice(long priceOffNoticeId) {
        return dbm.get(PriceOffNotice.class, priceOffNoticeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePriceOffNoticeStatus(long id, boolean lastPushStatus) {

        PriceOffNoticeUpdater updater = new PriceOffNoticeUpdater(id);

        updater.getPo().setLatestPushStatus(lastPushStatus);

        dbm.update(updater);

    }

    @Override
    public void priceOffCheck(long skuid) {

        PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, skuid);
        System.out.println("get sku success for " + skuid);
        if (ptmCmpSku == null) {
            System.out.println("sku has bean removed " + skuid);
            return;
        }

        //检测价格是否需要发push
        int curpage = 1;
        int pagesize = 100;
        PageableResult<PriceOffNotice> pageableResult = dbm.queryPage(QUERY_PRICEOFF_BY_SKUID, curpage, pagesize, Arrays.asList(skuid));

        long totalPage = pageableResult.getTotalPage();
        System.out.println("totalpage " + totalPage);

        while (curpage <= totalPage) {

            System.out.println("curpage " + curpage);
            if (curpage > 1) {
                pageableResult = dbm.queryPage(QUERY_PRICEOFF_BY_SKUID, curpage, pagesize, Arrays.asList(skuid));
            }

            List<PriceOffNotice> priceOffNoticeList = pageableResult.getData();
            if (priceOffNoticeList == null) {
                System.out.println("priceoffnoticelist is null");
            } else {
                System.out.println("priceoffnoticelist size " + priceOffNoticeList.size());
            }

            for (PriceOffNotice priceOffNotice : priceOffNoticeList) {

                if (ptmCmpSku.getPrice() > priceOffNotice.getNoticePrice()) {
                    System.out.println("ptmcmpsku price " + ptmCmpSku.getPrice());
                    System.out.println("notice price " + priceOffNotice.getNoticePrice());
                    System.out.println("skuprice > noticeprice continue");
                    continue;
                }

                System.out.println("skuprice < noticeprice push");
                pushNew(priceOffNotice, ptmCmpSku, true);
            }
            curpage++;
        }
    }

    @Override
    public boolean pushFailRePush(long id, boolean cacheFail) {

        PriceOffNotice priceOffNotice = getPriceOffNotice(id);

        long skuid = priceOffNotice.getSkuid();

        PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, skuid);

        boolean pushStatus = pushNew(priceOffNotice, ptmCmpSku, cacheFail);

        return pushStatus;
    }

    /**
     * 按照用户push的方法，
     *
     * @param priceOffNotice
     * @param ptmCmpSku
     * @param cacheFail      push失败是否缓存到失败队列重发
     * @return
     */
    private boolean pushNew(PriceOffNotice priceOffNotice, PtmCmpSku ptmCmpSku, boolean cacheFail) {

        Long id = priceOffNotice.getId();

        String useridString = priceOffNotice.getUserid();
        Long userid = Long.valueOf(useridString);

        //获取用户信息
        UrmUser urmUser = dbm.get(UrmUser.class, userid);

        System.out.println("userid:skuid__" + useridString + ":" + ptmCmpSku.getId());
        boolean pushStatus = false;

        if (urmUser == null) {
            System.out.println("urmuser is null");
        } else {

            String gcmToken = urmUser.getGcmToken();

            System.out.println("gcmToken:" + gcmToken);
            if (!StringUtils.isEmpty(gcmToken)) {

                List<UrmUserDevice> userDeviceList = dbm.query("SELECT t FROM UrmUserDevice t WHERE t.userid = ?0", Arrays.asList(userid));

                if (userDeviceList == null) {
                    System.out.println("userDeviceList is null");
                } else {
                    System.out.println("userDeviceList size " + userDeviceList.size());
                }

                if (userDeviceList != null && userDeviceList.size() != 0) {

                    String deviceId = userDeviceList.get(0).getDeviceId();

                    //deeplink
                    String deepLinkUrl = WebsiteHelper.getDeeplinkWithAff(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), new String[]{MarketChannel.GOOGLEPLAY.name(), deviceId, useridString});
                    System.out.println("deepLinkUrl " + deepLinkUrl);

                    String title = "PRICE DROP ALERT! " + ptmCmpSku.getTitle();
                    String content = "Now available at Rs." + (int) ptmCmpSku.getPrice() + ". Check details now.";

                    AppPushMessage message = new AppPushMessage(
                            new AppMsgDisplay(title + content, title, content),
                            new AppMsgClick(AppMsgClickType.DEEPLINK, deepLinkUrl, WebsiteHelper.getPackage(ptmCmpSku.getWebsite()))
                    );


                    AppPushBo appPushBo = new AppPushBo("5x1", "15:10", message);

                    String response = pushService.push(gcmToken, appPushBo);

                    System.out.println("response " + response);
                    JSONObject jsonResponse = JSONObject.parseObject(response.trim());

                    Integer success = jsonResponse.getInteger("success");
                    Integer failure = jsonResponse.getInteger("failure");
                    if (success == 1) {
                        //推送成功
                        pushStatus = true;
                        System.out.println("push success for " + userid + " " + ptmCmpSku.getId());
                    }

                    if (failure == 1) {
                        //推送失败
                        System.out.println("push fail for " + userid + " " + ptmCmpSku.getId());
                    }
                }

                if (pushStatus) {
                    updatePriceOffNoticeStatus(id, true);
                    System.out.println("update lastpushstatus push success for priceOffNoticeid" + id);
                } else {
                    updatePriceOffNoticeStatus(id, false);
                    System.out.println("update lastpushstatus push fail for priceOffNoticeid" + id);
                    //是否需要将失败写入缓存
                    if (cacheFail) {
                        redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_ID, priceOffNotice.getId() + "");
                        System.out.println("cache push fail success for " + priceOffNotice.getId());
                    }
                }

            }

        }

        return pushStatus;
    }

    /**
     * 按照用户找设备，然后按照设备推送的方法
     *
     * @param priceOffNotice
     * @param ptmCmpSku
     * @param cacheFail
     * @return
     */
    private boolean push(PriceOffNotice priceOffNotice, PtmCmpSku ptmCmpSku, boolean cacheFail) {

        Long id = priceOffNotice.getId();
        String userid = priceOffNotice.getUserid();
        System.out.println("userid " + userid);

        List<UrmUserDevice> urmUserDeviceList = dbm.query(QUERY_DEVICE_BY_USERID, Arrays.asList(userid));
        if (urmUserDeviceList == null) {
            System.out.println("urmuserdevicelist is null");
        } else {
            System.out.println("urmuserdevicelist size " + urmUserDeviceList.size());
        }

        boolean pushStatus = false;

        for (UrmUserDevice urmUserDevice : urmUserDeviceList) {

            String deviceId = urmUserDevice.getDeviceId();
            if (StringUtils.isEmpty(deviceId)) {
                System.out.println("deviceId is empty for " + urmUserDevice.getId());
                continue;
            }

            UrmDevice urmDevice = dbm.get(UrmDevice.class, deviceId);
            if (urmDevice == null) {
                System.out.println("urmDevice is null for " + deviceId);
                continue;
            }

            String gcmToken = urmDevice.getGcmToken();
            if (StringUtils.isEmpty(gcmToken)) {
                System.out.println("gcmToken is null for urmdeviceid" + urmDevice.getId());
                continue;
            }

            MarketChannel marketChannel = urmDevice.getMarketChannel();
            if (marketChannel == null) {
                System.out.println("marketChannel is null for urmdeviceid " + urmDevice.getId());
                continue;
            }

            //deeplink
            String deepLinkUrl = WebsiteHelper.getDeeplinkWithAff(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), new String[]{marketChannel.name(), deviceId});
            System.out.println("deepLinkUrl " + deepLinkUrl);


            String title = "PRICE DROP ALERT! " + ptmCmpSku.getTitle();
            String content = "Now available at Rs." + (int) ptmCmpSku.getPrice() + ". Check details now.";

            AppPushMessage message = new AppPushMessage(
                    new AppMsgDisplay(title + content, title, content),
                    new AppMsgClick(AppMsgClickType.DEEPLINK, deepLinkUrl, WebsiteHelper.getPackage(ptmCmpSku.getWebsite()))
            );


            AppPushBo appPushBo = new AppPushBo("5x1", "15:10", message);

            String response = pushService.push(gcmToken, appPushBo);

            System.out.println("response " + response);
            JSONObject jsonResponse = JSONObject.parseObject(response.trim());

            Integer success = jsonResponse.getInteger("success");
            Integer failure = jsonResponse.getInteger("failure");
            if (success == 1) {
                //推送成功
                pushStatus = true;
                System.out.println("push success for priceOffNotice" + id);

            }

            if (failure == 1) {
                //推送失败
                System.out.println("push fail for priceOffNotice " + id);
                System.out.println("push fail urmdevice " + urmDevice.getId());
            }
        }

        if (pushStatus) {
            updatePriceOffNoticeStatus(id, true);
            System.out.println("update lastpushstatus push success for priceOffNoticeid" + id);
        } else {
            updatePriceOffNoticeStatus(id, false);
            System.out.println("update lastpushstatus push fail for priceOffNoticeid" + id);
            //是否需要将失败写入缓存
            if (cacheFail) {
                redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_ID, priceOffNotice.getId() + "");
                System.out.println("cache push fail success for " + priceOffNotice.getId());
            }
        }

        return pushStatus;
    }
}
