package hasoffer.core.user.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.push.*;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.push.IPushService;
import hasoffer.core.user.IPriceOffNoticeService;
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

    @Resource
    IDataBaseManager dbm;
    @Resource
    IPushService pushService;

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

        PriceOffNotice priceOffNotice = dbm.querySingle(QUERY_PRICEOFF_BY_USERID_SKUID, Arrays.asList(userId, skuid));

        return priceOffNotice;
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

                    //todo
                    AppPushMessage message = new AppPushMessage(
                            new AppMsgDisplay("Hurry on!Redmi 3S On Sale! 12:00 noon|Starts at Rs.6,999  ", "Hurry on!Redmi 3S On Sale!", "12:00 noon|Starts at Rs.6,999 "),
                            new AppMsgClick(AppMsgClickType.DEAL, "99000154", "com.flipkart.android")
                    );

                    AppPushBo appPushBo = new AppPushBo("5x1", "15:10", message);
                    pushService.push(gcmToken, appPushBo);
                }
            }
            curpage++;
        }
    }
}
