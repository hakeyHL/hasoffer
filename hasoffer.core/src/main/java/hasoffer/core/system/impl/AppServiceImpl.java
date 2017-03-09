package hasoffer.core.system.impl;

import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.AffliIdHelper;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.*;
import hasoffer.core.persistence.po.app.mongo.AppOfferRecord;
import hasoffer.core.persistence.po.h5.KeywordCollection;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.urm.*;
import hasoffer.core.system.IAppService;
import hasoffer.core.utils.ConstantUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created on 2015/12/30.
 */
@Service
@Transactional
public class AppServiceImpl implements IAppService {
    static final String API_SQL_GET_APPOFFERST_BY_MARKETCHANNEL_AND_YMD = "select t from AppOfferStatistics t where t.marketChannel=?0 and t.ymd=?1";
    static final String API_SQL_GET_APPOFFERST_BY_MARKETCHANNEL_AND_YMDBT = "select t from AppOfferStatistics t where t.marketChannel=?0 and t.ymd>=?1 and t.ymd<=?2";
    static final String API_SQL_GET_ORDERS_BY_AFFID = "select t from OrderStatsAnalysisPO t where  t.orderStatus='approved' and channelSrc=?2 and t.orderTime >=?0 and t.orderTime <=?1";

    private final static Logger loggerIndexUrl = LoggerFactory.getLogger("hasoffer.IndexUrl");

    private static final String Q_APP_VERSION =
            "SELECT t FROM AppVersion t " +
                    " WHERE t.appType = ?0 and marketChannel =?1" +
                    " ORDER BY t.publishTime DESC";

    private static final String Q_CHANNEL_APP_VERSION =
            "SELECT t FROM AppVersion t " +
                    " WHERE t.appType = ?0  and marketChannel = ?1" +
                    " ORDER BY t.publishTime DESC";

    private static final String Q_APP_WEBSITE =
            "SELECT t FROM AppWebsite t " +
                    " WHERE t.appshow = ?0 ";

    private static final String Q_APP_ORDERS =
            "SELECT t FROM OrderStatsAnalysisPO t " +
                    " WHERE t.userId = ?0 ";

    private static final String Q_APP_GETUSER =
            "SELECT t FROM UrmUser t " +
                    " WHERE t.userToken = ?0 ";

    private static final String Q_APP_ORDER =
            "SELECT t FROM OrderStatsAnalysisPO t " +
                    " WHERE t.orderId = ?0 and t.userId=?1";

    private static final String Q_APP_CATEGORY =
            "SELECT t FROM PtmCategory t where t.parentId=0 " +
                    " order by level ASC,rank ASC";

    private static final String Q_APP_GETUSERBYTHIRDID =
            "SELECT t FROM UrmUser t " +
                    " where t.thirdId=?0";

    private static final String Q_APP_GETUSER_BY_ID =
            "SELECT t FROM UrmUser t " +
                    " where t.id=?0";

    private static final String Q_APP_USER_GET_BY_NAME =
            "SELECT t FROM UrmUser t " +
                    " where t.userName=?0";

    private static final String Q_APP_URM_SIGNCOIN_BY_USERID =
            "SELECT t FROM UrmSignCoin t where t.userId=?0 ";

    private static final String Q_APP_URM_GET_SIGNCONFIG =
            "SELECT t FROM UrmSignAwdCfg t order by t.count desc  ";

    private static final String Q_APP_URMDEVICE_GETIDSBYDEVICEID =
            "SELECT t.id FROM UrmDevice t " +
                    " where t.deviceId=?0";

    private static final String Q_APP_URMUSERDEVICE_GETIDSBYUSERID =
            "SELECT t.deviceId FROM UrmUserDevice t " +
                    " where t.userId=?0";

    private static final String Q_APP_URMUSERDEVICE_GETIUSERIDSBYDEVICEID =
            "SELECT t.userId FROM UrmUserDevice t " +
                    " where t.deviceId=?0";

    private static final String Q_APP_GETDEALS =
            "SELECT t FROM AppDeal t where  t.display='1' and   " +
                    "t.expireTime >= ?0   and t.listPageImage is not null " +
                    " order by t.weight desc,t.createTime desc  ";

    private static final String Q_APP_GETDEALS_BY_THUMB =
            "SELECT t FROM AppDeal t where  t.display='1' and   " +
                    "t.expireTime >= ?0   and t.listPageImage is not null and t.dealThumbNumber>0 " +
                    " order by t.weight desc,t.createTime desc  ";
    private static final String Q_APP_DEAL_GET_SIMILAR =
            "SELECT t FROM AppDeal t where  t.display='1' and  t.createTime>=?0 and " +
                    "t.expireTime >= ?1   and t.listPageImage is not null " +
                    " order by t.dealClickCount,t.createTime desc  ";


    private static final String Q_APP_GETUSERS = "SELECT t FROM  UrmSignCoin t where t.conSignNum>1 or t.maxConSignNum > 1 ";

    private static final String Q_APP_GETDEALS_TEMP =
            "SELECT t FROM AppDeal t where  t.appdealSource='PRICE_OFF' and  t.display='0' and    t.expireTime >= ?0   and t.listPageImage is not null  order by id desc   ";


    //    private static final String Q_APP_GETDEALS =
//            "SELECT t FROM AppDeal t where  t.display='1' and  and t.originPrice >0  and   t.expireTime >= ?0   and t.listPageImage is not null  order by id desc   ";
    private static final String Q_APP_GETBANNERS =
            " SELECT t from AppBanner t where t.deadline >=?0 ORDER BY t.id desc";

    private static final String Q_APP_GETBANNERS_NINEAPP =
            " SELECT t from AppBanner t where t.deadline >=?0 and t.bannerFrom='DEAL' ORDER BY t.createTime desc";

    private static final String Q_APP_GEDEALDETAIL =
            " SELECT t from AppDeal t where t.id=?0";

    private static final String Q_APP_GETCHILDCATEGORY =
            "SELECT t FROM PtmCategory t where t.parentId=?0 and level<=3 " +
                    " order by level ASC,rank ASC";

    private static final String Q_APP_CATEGORY_ISHASCHILDNODE =
            "SELECT t FROM PtmCategory t where t.parentId=?0 ";

    private static final String Q_APP_CATEGORY_BYID =
            "SELECT t FROM PtmCategory t where t.id=?0 ";

    private static final String Q_APP_GIFT_LIST =
            "SELECT t FROM HasofferCoinsExchangeGift t order by t.coinPrice asc ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    MongoDbManager mongoDbManager;
    private String Q_APP_GETPRODUCTS =
            "SELECT t FROM PtmProduct t " +
                    " where 1=1 and ";

    public static String getLiveDemo(Website website, MarketChannel marketChannel, String deviceId) {
        Map<Website, String> liveDemoMap = new HashMap<>();
        Random random = new Random();
        String flipkartAffid = AffliIdHelper.FLIKART_YEAHMOBI_FLIDS[random.nextInt(AffliIdHelper.FLIKART_YEAHMOBI_FLIDS.length)];
        String extParam1 = AffliIdHelper.getMarketId(marketChannel);
        if (Arrays.asList(AffliIdHelper.FLIKART_YEAHMOBI_FLIDS).contains(flipkartAffid)) {
            String[] affExtParams = new String[]{"103662", "103650", "103647", "103643"};
            extParam1 = affExtParams[random.nextInt(affExtParams.length)];
        }
        //liveDemoMap.put(Website.FLIPKART, "http://dl.flipkart.com/dl/apple-iphone-5s/p/itme8ra4f4twtsva?affid=" + flipkartAffid + "&affExtParam1=" + extParam1 + "&affExtParam2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0");
        liveDemoMap.put(Website.FLIPKART, "https://dl.flipkart.com/dl/apple-iphone-6-space-grey-64-gb/p/itme8gfcs2dhysgq?pid=MOBEYHZ28FRMNDCW&affid=" + flipkartAffid + "&affExtParam1=" + extParam1 + "&affExtParam2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0");
        liveDemoMap.put(Website.SNAPDEAL, "android-app://com.snapdeal.main/snapdeal/m.snapdeal.com/product/apple-iphone-5s-16-gb/1204769399?aff_id=82856&utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_sub=" + extParam1 + "&aff_sub2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0");
//        liveDemoMap.put(Website.SHOPCLUES, "http://www.shopclues.com/apple-iphone-5s-16gb-44.html?ty=0&id=none&mcid=aff&utm_source=Hasoffer&OfferId=15");
        liveDemoMap.put(Website.SHOPCLUES, "http://affiliateshopclues.com/?a=2892&c=69&p=r&s1=" + marketChannel.name() + "&ckmrdr=" + "http://www.shopclues.com/apple-iphone-5s-16gb-44.html");
        //liveDemoMap.put(Website.EBAY, "http://genlin.ss");
        return liveDemoMap.get(website);
    }

    public static void main(String[] args) {
        System.out.println(getLiveDemo(Website.FLIPKART, MarketChannel.LEO, "asdfaskwesdkf"));
    }

    @Override
    public AppVersion getLatestVersion(AppType appType, MarketChannel marketChannel) {
        List<AppVersion> versions = dbm.query(Q_APP_VERSION, Arrays.asList(appType, marketChannel));

        return ArrayUtils.hasObjs(versions) ? versions.get(0) : null;
    }

    @Override
    public AppVersion getLatestVersion(MarketChannel marketChannel, AppType appType) {
        List<AppVersion> versions = dbm.query(Q_CHANNEL_APP_VERSION, Arrays.asList(appType, marketChannel));
        return ArrayUtils.hasObjs(versions) ? versions.get(0) : null;
    }

    @Override
    public List<AppWebsite> getWebsites(boolean appshow) {
        return dbm.query(Q_APP_WEBSITE, Arrays.asList(appshow));
    }

    @Override
    public List<OrderStatsAnalysisPO> getBackDetails(String userId) {
        List li = new ArrayList();
        li.add(userId);
        return dbm.query(Q_APP_ORDERS, li);
    }

    @Override
    public UrmUser getUserByUserToken(String userToken) {
        List li = new ArrayList();
        li.add(userToken);
        UrmUser user = dbm.querySingle(Q_APP_GETUSER, li);
        return user;
    }

    @Override
    public OrderStatsAnalysisPO getOrderDetail(String orderId, String userId) {
        List li = new ArrayList();
        li.add(orderId);
        li.add(userId);
        return dbm.querySingle(Q_APP_ORDER, li);
    }

    @Override
    public PageableResult getDeals(int page, int pageSize) {
        return dbm.queryPage(Q_APP_GETDEALS, page <= 1 ? 1 : page, pageSize, Arrays.asList(new Date()));
//        return dbm.queryPage(Q_APP_GETDEALS, page.intValue() <= 1 ? 1 : page.intValue(), pageSize.intValue(), Arrays.asList(new Date()));
    }

    @Override
    public PageableResult getDeals(Long page, Long pageSize, int temp) {
        return dbm.queryPage(Q_APP_GETDEALS_TEMP, page.intValue() <= 1 ? 1 : page.intValue(), pageSize.intValue(), Arrays.asList(TimeUtils.getDayStart()));
    }

    @Override
    public List<PtmCategory> getCategory() {
        return dbm.query(Q_APP_CATEGORY);
    }

    @Override
    public AppDeal getDealDetail(long id) {
        return dbm.querySingle(Q_APP_GEDEALDETAIL, Arrays.asList(id));
    }

    @Override
    public UrmUser getUserByThirdId(String thirdId) {
        List li = Arrays.asList(thirdId);
        List<UrmUser> urmUsers = dbm.query(Q_APP_GETUSERBYTHIRDID, li);
        if (urmUsers != null && urmUsers.size() > 0) {
            return urmUsers.get(0);
        }
        return null;
    }

    @Override
    public UrmUser getUserById(Long id) {
        List<UrmUser> urmUsers = dbm.query(Q_APP_GETUSER_BY_ID, Collections.singletonList(id));
        if (urmUsers != null && urmUsers.size() > 0) {
            return urmUsers.get(0);
        }
        return null;
    }

    @Override
    public List<UrmUser> getIdDescUserListByThirdId(String thirdId) {
        return dbm.query("SELECT t FROM UrmUser t WHERE t.thirdId = ?0 ORDER BY t.id DESC", Arrays.asList(thirdId));
    }

    @Override
    public List getProductByCriteria(SearchCriteria criteria) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        String categoryId = criteria.getCategoryId();
        if (StringUtils.isNotBlank(categoryId)) {
            sb.append(" categoryId = ?" + i + ConstantUtil.API_DATA_EMPTYSTRING);
            i++;
        }
        int comment = criteria.getComment();
        if (comment == 0) {
            sb.append(" order by comment desc ");
        } else {
            sb.append(" order by comment asc ");
        }
        String keyword = criteria.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            sb.append(" title like %" + i + "%");
            i++;
        }
        Long maxPrice = criteria.getMaxPrice();
        Long minPrice = criteria.getMinPrice();

        int page = criteria.getPage();
        int pageSize = criteria.getPageSize();


        Q_APP_GETPRODUCTS = Q_APP_GETPRODUCTS + "ee";
        return null;
    }

    @Override
    public List<String> getUserDevicesByUserId(String userId) {
        return dbm.query(Q_APP_URMUSERDEVICE_GETIDSBYUSERID, Arrays.asList(userId));
    }

    @Override
    public List<String> getUserIdsByDeviceId(String urmDeviceId) {
        return dbm.query(Q_APP_URMUSERDEVICE_GETIUSERIDSBYDEVICEID, Arrays.asList(urmDeviceId));
    }

    @Override
    @Transactional
    public int addUrmUserDevice(List<UrmUserDevice> urmUserDevices) {
        return dbm.batchSave(urmUserDevices);
    }

    @Override
    public int addUser(UrmUser user) {
        List li = new ArrayList();
        li.add(user);
        return dbm.batchSave(li);
    }

    @Override
    public List<String> getUserDevices(String deviceId) {
        //有的用户绑定设备列表
        return dbm.query(Q_APP_URMDEVICE_GETIDSBYDEVICEID, 1, 10, Arrays.asList(deviceId));
    }

    @Override
    public void updateUserInfo(UrmUser uUser) {
        //List li = new ArrayList();
        //li.add(uUser);
        //dbm.update(li);
        dbm.update(uUser);
    }

    @Override
    public List<AppBanner> getBanners() {
        return dbm.query(Q_APP_GETBANNERS, Arrays.asList(new Date()));
    }

    @Override
    public List<PtmCategory> getChildCategorys(String categoryId) {
        List li = new ArrayList();
        li.add(Long.valueOf(categoryId));
        return dbm.query(Q_APP_GETCHILDCATEGORY, li);
    }

    @Override
    public int isHasChildNode(Long id) {
        List li = new ArrayList();
        li.add(id);
        List<PtmCategory> category = (List) dbm.query(Q_APP_CATEGORY_ISHASCHILDNODE, li);
        if (category == null || category.size() < 1) {
            return 0;
        }
        return 1;
    }

    @Override
    public void countDealClickCount(AppDeal appDeal) {
        appDeal.setDealClickCount(appDeal.getDealClickCount() == null ? 0 : appDeal.getDealClickCount() + 1);
        List deals = Arrays.asList(appDeal);
        dbm.update(deals);
    }

    @Override
    public List<UrmUser> getUsersByUserName(String userName) {
        return dbm.query(Q_APP_USER_GET_BY_NAME, Arrays.asList(userName));
    }

    @Override
    public Map<Integer, Integer> getSignAwardNum() {
        List<UrmSignAwdCfg> signAwardNum = dbm.query(Q_APP_URM_GET_SIGNCONFIG);
        Map<Integer, Integer> afwCfgMap = new HashMap<>();
        if (signAwardNum != null) {
            for (UrmSignAwdCfg urmSignAwdCfg : signAwardNum) {
                afwCfgMap.put(urmSignAwdCfg.getCount(), urmSignAwdCfg.getAwardCoin());
            }
        }
        //TODO 考虑加缓存
        //for (int i = 1; i < 8; i++) {
        //    if (afwCfgMap.get(i) == null) {
        //        afwCfgMap.put(i, 5 + 5 * i);
        //    }
        //}
        return afwCfgMap;
    }

    @Override
    public List<HasofferCoinsExchangeGift> getGiftList() {
        return dbm.query(Q_APP_GIFT_LIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bakUserInfo(UrmUser urmUser) {

        UrmUserBak userBak = new UrmUserBak();

        userBak.setId(urmUser.getId());
        userBak.setAvatarPath(urmUser.getAvatarPath());
        //userBak.setConSignNum(urmUser.getConSignNum());
        userBak.setCreateTime(urmUser.getCreateTime());
        userBak.setGcmToken(urmUser.getGcmToken());
        //userBak.setLastSignTime(urmUser.getLastSignTime());
        //userBak.setMaxConSignNum(urmUser.getMaxConSignNum());
        //userBak.setSignCoin(urmUser.getSignCoin());
        userBak.setTelephone(urmUser.getTelephone());
        userBak.setThirdId(urmUser.getThirdId());
        userBak.setThirdPlatform(urmUser.getThirdPlatform());
        userBak.setThirdToken(urmUser.getThirdToken());
        userBak.setUserName(urmUser.getUserName());
        userBak.setUserToken(urmUser.getUserToken());

        dbm.create(userBak);
        dbm.delete(UrmUser.class, urmUser.getId());
    }

    public PtmCategory getCategoryInfo(Long cateId) {
        List<PtmCategory> query = dbm.query(Q_APP_CATEGORY_BYID, Arrays.asList(cateId));
        if (query != null && query.size() > 0) {
            return query.get(0);
        }
        return null;
    }

    @Override
    public UrmSignCoin getSignCoinByUserId(Long id) {
        List<UrmSignCoin> signCoins = dbm.query(Q_APP_URM_SIGNCOIN_BY_USERID, Collections.singletonList(id));
        if (signCoins != null && signCoins.size() > 0) {
            return signCoins.get(0);
        }
        return null;
    }

    @Override
    public void updateUrmSignCoin(UrmSignCoin urmSignCoin) {
        if (urmSignCoin.getLastSignTime() != null) {
            urmSignCoin.setSignZhTime(new Date(urmSignCoin.getLastSignTime()));
            urmSignCoin.setSignIndTime(new Date(urmSignCoin.getLastSignTime() - TimeUtils.MILLISECONDS_OF_1_MINUTE * 150));
        }
        dbm.saveOrUpdate(urmSignCoin);
    }

    @Override
    public void checkAndAlertUser2Sign() {
        //record opreate history ,so that i could anwser xu's question .


        //1. check who had't sign till today 22:00:00


        //2. send push message to alert them to sign in .

        //3. record this history into mongo or mysql .


        //4. over .
    }

    @Override
    public List<Map<String, String>> getIndexPage(MarketChannel marketChannel, String deviceId) {
        List<Map<String, String>> mapList = new ArrayList<>();

        Map<String, String> flipkartMap = new HashMap<>();
        //X: 网站包名；Y：加密后的劫持link；Z：liveDemo J site名称 I是安装url
        flipkartMap.put("X", getPackageName(Website.FLIPKART));
        flipkartMap.put("Y", getFlipkartIndexUrl(marketChannel, deviceId));
        flipkartMap.put("Z", getLiveDemo(Website.FLIPKART, marketChannel, deviceId));
        flipkartMap.put("I", getInstallUrl(Website.FLIPKART));
        flipkartMap.put("J", Website.FLIPKART.toString());
        mapList.add(flipkartMap);

        Map<String, String> snapDealMap = new HashMap<>();
        snapDealMap.put("X", getPackageName(Website.SNAPDEAL));
        snapDealMap.put("Y", getSnapDealIndexUrl(marketChannel, deviceId));
        snapDealMap.put("Z", getLiveDemo(Website.SNAPDEAL, marketChannel, deviceId));
        snapDealMap.put("I", getInstallUrl(Website.SNAPDEAL));
        snapDealMap.put("J", Website.SNAPDEAL.toString());
        mapList.add(snapDealMap);

        Map<String, String> shopClueMap = new HashMap<>();
        shopClueMap.put("X", getPackageName(Website.SHOPCLUES));
        //shopClueMap.put("Y", getShopCluesIndexUrl());
//        shopClueMap.put("Y", ConstantUtil.API_DATA_EMPTYSTRING);
        shopClueMap.put("Y", getShopCluesIndexUrl());
        shopClueMap.put("Z", getLiveDemo(Website.SHOPCLUES, marketChannel, deviceId));
        shopClueMap.put("I", getInstallUrl(Website.SHOPCLUES));
        shopClueMap.put("J", Website.SHOPCLUES.toString());
        mapList.add(shopClueMap);

        Map<String, String> aliExpressMap = new HashMap<>();
        aliExpressMap.put("X", getPackageName(Website.ALIEXPRESS));
        aliExpressMap.put("Y", getAliExpressIndexUrl(marketChannel, deviceId));
        aliExpressMap.put("Z", ConstantUtil.API_DATA_EMPTYSTRING);
        aliExpressMap.put("I", ConstantUtil.API_DATA_EMPTYSTRING);
        aliExpressMap.put("J", Website.ALIEXPRESS.toString());
        mapList.add(aliExpressMap);

        Map<String, String> amazonMap = new HashMap<>();
        amazonMap.put("X", getPackageName(Website.AMAZON));
        amazonMap.put("Y", getAmazonIndexUrl(marketChannel, deviceId));
        amazonMap.put("Z", ConstantUtil.API_DATA_EMPTYSTRING);
        amazonMap.put("I", ConstantUtil.API_DATA_EMPTYSTRING);
        amazonMap.put("J", Website.AMAZON.toString());
        mapList.add(amazonMap);

        Map<String, String> myntraMap = new HashMap<>();
        myntraMap.put("X", getPackageName(Website.MYNTRA));
        myntraMap.put("Y", getMyntraIndexUrl(marketChannel, deviceId));
        myntraMap.put("Z", ConstantUtil.API_DATA_EMPTYSTRING);
        myntraMap.put("I", ConstantUtil.API_DATA_EMPTYSTRING);
        myntraMap.put("J", Website.MYNTRA.toString());
        mapList.add(myntraMap);

        return mapList;
    }

    private String getMyntraIndexUrl(MarketChannel marketChannel, String deviceId) {
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64("android-app://com.myntra.android/myntra/myntra.com/".getBytes(Charset.forName("UTF-8"))));
    }

    private String getAmazonIndexUrl(MarketChannel marketChannel, String deviceId) {
//        Random random = new Random();
//        String amazonUrl = ConstantUtil.API_DATA_EMPTYSTRING;
//        if (random.nextInt(3) == 1) {
//        }
        String affId = AffliIdHelper.getAffIdByChannelForAmazon(marketChannel);
        //if (StringUtils.isNotBlank(affId)) {
        //    affId = AffliIdHelper.getAffIdByChannelForAmazon(MarketChannel.NONE);
        //}
        String amazonUrl = "intent://amazon.in/?tag=" + affId + "&camp=3638&ref=as_li_tl#Intent;scheme=com.amazon.mobile.shopping;package=in.amazon.mShop.android.shopping;S.browser_fallback_url=https://play.google.com/store/apps/details?id=in.amazon.mShop.android.shopping;end";
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(amazonUrl.getBytes(Charset.forName("UTF-8"))));
    }

    private String getInstallUrl(Website website) {
        String[] flipkart = new String[]{"zhangchen", "wangshuom"};
        Random random = new Random();
        Map<Website, String> packageMap = new HashMap<>();
        packageMap.put(Website.FLIPKART, "http://dl.flipkart.com/dl/install-app?affid=" + flipkart[random.nextInt(flipkart.length)]);
        packageMap.put(Website.SNAPDEAL, ConstantUtil.API_DATA_EMPTYSTRING);
        packageMap.put(Website.SHOPCLUES, ConstantUtil.API_DATA_EMPTYSTRING);
        //packageMap.put(Website.EBAY, ConstantUtil.API_DATA_EMPTYSTRING);
        return packageMap.get(website);
    }

    private String getPackageName(Website website) {
        Map<Website, String> packageMap = new HashMap<>();
        packageMap.put(Website.FLIPKART, "com.flipkart.android");
        packageMap.put(Website.SNAPDEAL, "com.snapdeal.main");
        packageMap.put(Website.SHOPCLUES, "com.shopclues");
        packageMap.put(Website.ALIEXPRESS, "com.alibaba.aliexpresshd");
        packageMap.put(Website.AMAZON, "in.amazon.mShop.android.shopping");
        packageMap.put(Website.MYNTRA, "com.myntra.android");
        //packageMap.put(Website.EBAY, "com.ebay.mobile");
        return packageMap.get(website);
    }

    private String getFlipkartIndexUrl(MarketChannel marketChannel, String deviceId) {

        Random random = new Random();
        String flipkartAffid = AffliIdHelper.FLIKART_YEAHMOBI_FLIDS[random.nextInt(AffliIdHelper.FLIKART_YEAHMOBI_FLIDS.length)];
        String flipkartExtParam1 = AffliIdHelper.getMarketId(marketChannel);
        if (Arrays.asList(AffliIdHelper.FLIKART_YEAHMOBI_FLIDS).contains(flipkartAffid)) {
            String[] affExtParams = new String[]{"103662", "103650", "103647", "103643"};
            flipkartExtParam1 = affExtParams[random.nextInt(affExtParams.length)];
        }
        String url = "http://dl.flipkart.com/dl/?affid=" + flipkartAffid + "&affExtParam1=" + flipkartExtParam1 + "&affExtParam2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0";
        loggerIndexUrl.info("INDEXPAGE: marketChannel: {}, deviceId:{}, AffId:{}", marketChannel, deviceId, flipkartAffid);
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(url.getBytes(Charset.forName("UTF-8"))));
    }

    private String getSnapDealIndexUrl(MarketChannel marketChannel, String deviceId) {
        Random random = new Random();
        String[] snapDealAffids = new String[]{"112338"};
        String snapDealAffid = snapDealAffids[random.nextInt(snapDealAffids.length)];
        String snapDealExtParam1 = AffliIdHelper.getMarketId(marketChannel);
        // 112338是yeahmobi申请的snapdeal帐号
        if ("112338".equals(snapDealAffid)) {
            snapDealExtParam1 = AffliIdHelper.MARKET_CHANNEL_YEAHMOBI[random.nextInt(AffliIdHelper.MARKET_CHANNEL_YEAHMOBI.length)];
        }
        String url = "android-app://com.snapdeal.main/snapdeal/m.snapdeal.com?aff_id=" + snapDealAffid + "&utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_sub=" + snapDealExtParam1 + "&aff_sub2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0";
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(url.getBytes(Charset.forName("UTF-8"))));
    }

    private String getShopCluesIndexUrl() {
        Random random = new Random();
        //"http://affiliateshopclues.com/?a=2892&c=69&p=r&s1=VC&ckmrdr="
        String shopcludeId = AffliIdHelper.SHOPCLUDE_IDS[random.nextInt(AffliIdHelper.SHOPCLUDE_IDS.length)];
        String url = "http://www.shopclues.com/?ty=0&id=" + shopcludeId + "&mcid=aff&utm_source=Hasoffer&OfferId=15";
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(url.getBytes(Charset.forName("UTF-8"))));
    }

    private String getAliExpressIndexUrl(MarketChannel marketChannel, String deviceId) {
        Random random = new Random();
        String url = ConstantUtil.API_DATA_EMPTYSTRING;
        if (random.nextInt(5) != 1) {
            url = "http://s.click.aliexpress.com/e/qbA6QFyv3";
        }
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(url.getBytes(Charset.forName("UTF-8"))));
    }

    @Override
    public List<UrmSignCoin> getUserSignRecord() {
        return dbm.query("select t from UrmSignCoin t ");
    }

    @Override
    public PageableResult<UrmSignCoin> getUserList(int page, int pageSize) {

        return dbm.queryPage(Q_APP_GETUSERS, page, pageSize);
    }

    @Override
    @Transactional
    public int addUserRedeemGroup(List<UrmUserRedeemGroup> groupList) {
        return dbm.batchSave(groupList);
    }

    @Override
    public UrmUserRedeemGroup getUrmRedeemGroupById(Long id) {
        return dbm.get(UrmUserRedeemGroup.class, id);
    }

    @Override
    public List<AppDeal> getSimilarDeals(int initSize) {
        if (initSize < 1) {
            initSize = 3;
        }
        List<AppDeal> similarDealList = new ArrayList<>();
        //从当天和昨天未过期的deal随机
        //1. 获取昨天00:00:00 到现在的未过期的deal列表
        //2. 从其size中随机三个值然后取出返回

        List<AppDeal> deals = dbm.query(Q_APP_DEAL_GET_SIMILAR, Arrays.asList(new Date(TimeUtils.yesterday(0, 0, 0)), new Date()));

        if (deals == null) {
            return similarDealList;
        }
        int size = deals.size();
        if (size > 0) {
            List<Integer> dealIndexArray = new ArrayList<>();
            for (int i = 0; i < initSize; i++) {
                int tempRandom = new Random().nextInt(size);
                if (similarDealList.size() == initSize) {
                    break;
                }
                if (!dealIndexArray.contains(tempRandom) && dealIndexArray.size() < initSize) {
                    dealIndexArray.add(tempRandom);
                    similarDealList.add(deals.get(tempRandom));
                }
            }
        }
        return similarDealList;

    }

    @Override
    @Transactional
    public void updateDeal(AppDeal appDeal) {
        dbm.update(appDeal);
    }

    @Transactional
    @Override
    public void updateKeyResultCount(KeywordCollection keywordCollection) {
        dbm.update(keywordCollection);
    }

    @Override
    public PageableResult<AppDeal> getDealsForMexico(int page, int pageSize) {
        return dbm.queryPage(Q_APP_GETDEALS_BY_THUMB, page <= 1 ? 1 : page, pageSize, Arrays.asList(new Date()));
    }

    /**
     * 记录某个渠道的offer返回次数
     * 会返回当天的offer的返回次数
     *
     * @param marketChannel
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long recordOfferReturnCount(MarketChannel marketChannel) {
        if (marketChannel == null) {
            System.out.println("marketChannel is null");
            return 0l;
        }
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
        return appOfferStatistics.getOfferScanCount();
    }

    /**
     * 根据联盟id获取订单记录
     *
     * @return
     */
    @Override
    public List<OrderStatsAnalysisPO> getOrderDetailByAffId(Date startDate, Date endDate, MarketChannel marketChannel) {
//        affID
        return dbm.query(API_SQL_GET_ORDERS_BY_AFFID, Arrays.asList(startDate, endDate, marketChannel.name()));
    }

    /**
     * 按照日期返回和渠道获取offer点击和返回记录
     *
     * @param dateStart
     * @param dateEnd
     * @param marketChannel
     * @return
     */
    @Override
    public List<AppOfferStatistics> getOfferClickCountBetDate(Date dateStart, Date dateEnd, MarketChannel marketChannel) {
        String startYmd = TimeUtils.parse(dateStart, "yyyyMMdd");
        String endYmd = TimeUtils.parse(dateEnd, "yyyyMMdd");
        return dbm.query(API_SQL_GET_APPOFFERST_BY_MARKETCHANNEL_AND_YMDBT, Arrays.asList(marketChannel, startYmd, endYmd));
    }

    /**
     * 获取Banner列表NineApp
     *
     * @return
     */
    @Override
    public PageableResult<AppBanner> getBannersForNineApp() {
        return dbm.queryPage(Q_APP_GETBANNERS_NINEAPP, 1, 5, Arrays.asList(new Date()));
    }

    /**
     * 记录offer的点击
     *
     * @param marketChannel 渠道
     * @param offerId       offer的id
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
//    @Transactional
    public void recordOfferClickCount(MarketChannel marketChannel, long offerId) {
        System.out.println("current thread name is : " + Thread.currentThread().getName());
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
        System.out.println(Thread.currentThread().getName() + "  :get clickCount " + clickCount);
        appOfferStatistics.setOfferClickCount(clickCount + 1);
        System.out.println(Thread.currentThread().getName() + "  : +1 clickCount " + appOfferStatistics.getOfferClickCount());
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
            Update update = new Update();
            update.set("clickCount", appOfferRecord.getClickCount() + 1);
            update.set("currentTime", time);
            mongoDbManager.update(AppOfferRecord.class, appOfferRecord.getId(), update);
        } else {
            //之前没有
            appOfferRecord = new AppOfferRecord();
            appOfferRecord.setYmd(yyyyMMdd);
            appOfferRecord.setClickCount(1);
            appOfferRecord.setCurrentTime(time);
            appOfferRecord.setMarketChannel(marketChannel.name());
            appOfferRecord.setOfferId(offerId);
            mongoDbManager.save(appOfferRecord);
        }
    }
}
