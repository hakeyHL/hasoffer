package hasoffer.api.service;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.AppOfferStatistics;
import hasoffer.core.persistence.po.app.mongo.AppOfferRecord;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.webcommon.context.Context;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by hs on 2017年01月04日.
 * Time 15:32
 * 这里用于处理apiUtils不能处理的逻辑(主要是循环依赖)
 */
@Component
public class ApiHelperService {
    static final String API_SQL_GET_APPOFFERST_BY_MARKETCHANNEL_AND_YMD = "select t from AppOfferStatistics t where t.marketChannel=?0 and t.ymd=?1";
    @Resource
    AppServiceImpl appService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    MongoDbManager mongoDbManager;
    @Resource
    private ICacheService<UrmUser> userCacheService;

    /**
     * 获取当前用户
     *
     * @return
     */
    public UrmUser getCurrentUser() {
        UrmUser urmUser = null;
        String userToken = Context.currentContext().getHeader("usertoken");
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(userToken)) {
            String key = "user_" + userToken;
            urmUser = userCacheService.get(UrmUser.class, key, 0);
            if (urmUser == null) {
                urmUser = appService.getUserByUserToken(userToken);
                if (urmUser != null) {
                    userCacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
                }
            }
        }
        return urmUser;
    }

    /**
     * 记录某个渠道的offer返回次数
     *
     * @param marketChannel
     */
    void recordOfferReturnCount(MarketChannel marketChannel) {
        boolean newFlag = false;
        AppOfferStatistics appOfferStatistics = dbm.querySingle(API_SQL_GET_APPOFFERST_BY_MARKETCHANNEL_AND_YMD, Arrays.asList(marketChannel, TimeUtils.parse(new Date(), "yyyyMMdd")));
        if (appOfferStatistics == null) {
            newFlag = true;
            //创建一个
            appOfferStatistics = new AppOfferStatistics();
            appOfferStatistics.setMarketChannel(marketChannel);
        }
        Long offerScanCount = appOfferStatistics.getOfferScanCount();
        appOfferStatistics.setOfferScanCount(offerScanCount + 1);
        if (newFlag) {
            dbm.create(appOfferStatistics);
        } else {
            dbm.update(appOfferStatistics);
        }
    }

    /**
     * 记录offer的点击
     *
     * @param marketChannel 渠道
     * @param offerId       offer的id
     */
    void recordOfferClickCount(MarketChannel marketChannel, long offerId) {
        String yyyyMMdd = TimeUtils.parse(new Date(), "yyyyMMdd");
        long time = new Date().getTime();
        //按理说,只有获取了列表才能点击,但是不排除直接-点击-的情况,所以要创建
        boolean newFlag = false;
        AppOfferStatistics appOfferStatistics = dbm.querySingle(API_SQL_GET_APPOFFERST_BY_MARKETCHANNEL_AND_YMD, Arrays.asList(marketChannel, yyyyMMdd));
        if (appOfferStatistics == null) {
            newFlag = true;
            //创建一个
            appOfferStatistics = new AppOfferStatistics();
            appOfferStatistics.setMarketChannel(marketChannel);
        }
        Long clickCount = appOfferStatistics.getOfferClickCount();
        appOfferStatistics.setOfferClickCount(clickCount + 1);
        if (newFlag) {
            dbm.create(appOfferStatistics);
        } else {
            dbm.update(appOfferStatistics);
        }

        //记录到mongo
        //1. 根据deal的id和ymd以及渠道获取每天唯一的记录
        //2. 不存在则创建
        //3. 存在则更新
        AppOfferRecord appOfferRecord;
        List<AppOfferRecord> offerRecords = mongoDbManager.query(AppOfferRecord.class, new Query().addCriteria(Criteria.where("ymd").is(yyyyMMdd).and("marketChannel").is(marketChannel.name()).and("offerId").is(offerId)));
        if (offerRecords != null && offerRecords.size() > 0) {
            if (offerRecords.size() > 1) {
                System.out.println("error , more than one record  " + yyyyMMdd + "  " + offerId + "  " + marketChannel);
                return;
            }
            appOfferRecord = offerRecords.get(0);
            appOfferRecord.setClickCount(appOfferRecord.getClickCount() + 1);
            appOfferRecord.setCurrentTime(time);
            dbm.update(appOfferRecord);
        } else {
            //之前没有
            appOfferRecord = new AppOfferRecord();
            appOfferRecord.setYmd(yyyyMMdd);
            appOfferRecord.setClickCount(1);
            appOfferRecord.setCurrentTime(time);
            appOfferRecord.setMarketChannel(marketChannel.name());
            appOfferRecord.setOfferId(offerId);
            dbm.create(appOfferStatistics);
        }
    }
}
