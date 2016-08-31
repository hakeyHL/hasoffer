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

        //检测价格是否需要发push
        int curpage = 1;
        int pagesize = 100;
        PageableResult<PriceOffNotice> pageableResult = dbm.queryPage(QUERY_PRICEOFF_BY_SKUID, curpage, pagesize, Arrays.asList(skuid));

        long totalPage = pageableResult.getTotalPage();

        while (curpage <= totalPage) {

            if (curpage > 1) {
                pageableResult = dbm.queryPage(QUERY_PRICEOFF_BY_SKUID, curpage, pagesize, Arrays.asList(skuid));
            }

            List<PriceOffNotice> priceOffNoticeList = pageableResult.getData();

            for (PriceOffNotice priceOffNotice : priceOffNoticeList) {

                if (ptmCmpSku.getPrice() > priceOffNotice.getNoticePrice()) {
                    continue;
                }

                push(priceOffNotice, ptmCmpSku, true);
            }
            curpage++;
        }
    }

    @Override
    public void pushFailRePush(long id) {

        PriceOffNotice priceOffNotice = getPriceOffNotice(id);

        long skuid = priceOffNotice.getSkuid();

        PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, skuid);

        push(priceOffNotice, ptmCmpSku, false);
    }

    private void push(PriceOffNotice priceOffNotice, PtmCmpSku ptmCmpSku, boolean cacheFail) {

        String userid = priceOffNotice.getUserid();

        List<UrmUserDevice> urmUserDeviceList = dbm.query(QUERY_DEVICE_BY_USERID, Arrays.asList(userid));

        for (UrmUserDevice urmUserDevice : urmUserDeviceList) {

            String deviceId = urmUserDevice.getDeviceId();
            if (StringUtils.isEmpty(deviceId)) {
                continue;
            }

            UrmDevice urmDevice = dbm.get(UrmDevice.class, deviceId);
            if (urmDevice == null) {
                continue;
            }

            String gcmToken = urmDevice.getGcmToken();
            if (StringUtils.isEmpty(gcmToken)) {
                continue;
            }

            MarketChannel marketChannel = urmDevice.getMarketChannel();
            if (marketChannel == null) {
                continue;
            }

            String deepLinkUrl = WebsiteHelper.getDealUrlWithAff(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), new String[]{marketChannel.name()});

            String title = "PRICE DROP :" + ptmCmpSku.getTitle();
            String content = "Now available at Rs." + ptmCmpSku.getPrice();

            AppPushMessage message = new AppPushMessage(
                    new AppMsgDisplay(title + content, title, content),
                    new AppMsgClick(AppMsgClickType.DEEPLINK, deepLinkUrl, WebsiteHelper.getPackage(ptmCmpSku.getWebsite()))
            );


            AppPushBo appPushBo = new AppPushBo("5x1", "15:10", message);

            //for test
            gcmToken = "cf1xQ0M3jE4:APA91bH1Sn9ajC7PZN7S0547o0LWXRtgqnE0xsj8kXlf8XqmJGmKQPLTRnHABcY6bOMxSGdXonlPt4vPIk6WwVK0-h5GmgRpTRfYW3Yd5yU0UQYdAO6Aun8IH8TZaURS3EXP4gDHj-Li";

            String response = pushService.push(gcmToken, appPushBo);

            JSONObject jsonResponse = JSONObject.parseObject(response.trim());

            Integer success = jsonResponse.getInteger("success");
            Integer failure = jsonResponse.getInteger("failure");
            if (success == 1) {
                //推送成功
                Long id = priceOffNotice.getId();
                updatePriceOffNoticeStatus(id, true);
            }

            if (failure == 1) {
                //推送失败
                updatePriceOffNoticeStatus(priceOffNotice.getId(), false);
                //缓存失败队列
                if (cacheFail) {
                    redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_ID, priceOffNotice.getId() + "");
                }
            }
        }
    }
}
