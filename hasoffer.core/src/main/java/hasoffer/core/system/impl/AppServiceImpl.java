package hasoffer.core.system.impl;

import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.AffliIdHelper;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.*;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.urm.*;
import hasoffer.core.system.IAppService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    private static final String Q_APP_VERSION =
            "SELECT t FROM AppVersion t " +
                    " WHERE t.appType = ?0 and marketChannel != 'ZUK'" +
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
            "SELECT t FROM AppDeal t where  t.display='1' and    t.expireTime >= ?0   and t.listPageImage is not null  order by id desc   ";


    private static final String Q_APP_GETDEALS_TEMP =
            "SELECT t FROM AppDeal t where  t.appdealSource='PRICE_OFF' and  t.display='0' and    t.expireTime >= ?0   and t.listPageImage is not null  order by id desc   ";


    //    private static final String Q_APP_GETDEALS =
//            "SELECT t FROM AppDeal t where  t.display='1' and  and t.originPrice >0  and   t.expireTime >= ?0   and t.listPageImage is not null  order by id desc   ";
    private static final String Q_APP_GETBANNERS =
            " SELECT t from AppBanner t where t.deadline >=?0 ORDER BY t.id desc";

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

    private String Q_APP_GETPRODUCTS =
            "SELECT t FROM PtmProduct t " +
                    " where 1=1 and ";
    private List<UrmSignCoin> userSignRecord;

    @Override
    public AppVersion getLatestVersion(AppType appType) {
//        return dbm.get(AppVersion.class, 3L);
        List<AppVersion> versions = dbm.query(Q_APP_VERSION, Arrays.asList(appType));

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
    public PageableResult getDeals(Long page, Long pageSize) {
        return dbm.queryPage(Q_APP_GETDEALS, page.intValue() <= 1 ? 1 : page.intValue(), pageSize.intValue(), Arrays.asList(TimeUtils.getDayStart()));
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
            sb.append(" categoryId = ?" + i + "");
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
        appDeal.setDealClickCount(appDeal.getDealClickCount() + 1);
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
        //X: 网站编号；Y：联盟ID；Z：渠道编号
        flipkartMap.put("X", getPackageName(Website.FLIPKART));
        flipkartMap.put("Y", getIndexUrl(Website.FLIPKART, marketChannel, deviceId));
        flipkartMap.put("Z", getLiveDemo(Website.FLIPKART, marketChannel, deviceId));
        flipkartMap.put("I", getInstallUrl(Website.FLIPKART));
        flipkartMap.put("J", Website.FLIPKART.toString());

        mapList.add(flipkartMap);

        Map<String, String> snapDealMap = new HashMap<>();
        snapDealMap.put("X", getPackageName(Website.SNAPDEAL));
        snapDealMap.put("Y", getIndexUrl(Website.SNAPDEAL, marketChannel, deviceId));
        snapDealMap.put("Z", getLiveDemo(Website.SNAPDEAL, marketChannel, deviceId));
        snapDealMap.put("I", getInstallUrl(Website.SNAPDEAL));
        snapDealMap.put("J", Website.SNAPDEAL.toString());
        mapList.add(snapDealMap);

        Map<String, String> shopClueMap = new HashMap<>();
        shopClueMap.put("X", getPackageName(Website.SHOPCLUES));
        shopClueMap.put("Y", getIndexUrl(Website.SHOPCLUES, marketChannel, deviceId));
        shopClueMap.put("Z", getLiveDemo(Website.SHOPCLUES, marketChannel, deviceId));
        shopClueMap.put("I", getInstallUrl(Website.SHOPCLUES));
        shopClueMap.put("J", Website.SHOPCLUES.toString());
        mapList.add(shopClueMap);

        return mapList;
    }


    private String getInstallUrl(Website website) {
        String[] flipkart = new String[]{"zhangchen", "wangshuom"};
        Random random = new Random();
        Map<Website, String> packageMap = new HashMap<>();
        packageMap.put(Website.FLIPKART, "http://dl.flipkart.com/dl/install-app?affid=" + flipkart[random.nextInt(flipkart.length)]);
        packageMap.put(Website.SNAPDEAL, "");
        packageMap.put(Website.SHOPCLUES, "");
        //packageMap.put(Website.EBAY, "");
        return packageMap.get(website);
    }

    private String getPackageName(Website website) {
        Map<Website, String> packageMap = new HashMap<>();
        packageMap.put(Website.FLIPKART, "com.flipkart.android");
        packageMap.put(Website.SNAPDEAL, "com.snapdeal.main");
        packageMap.put(Website.SHOPCLUES, "com.shopclues");
        //packageMap.put(Website.EBAY, "com.ebay.mobile");
        return packageMap.get(website);
    }

    private String getLiveDemo(Website website, MarketChannel marketChannel, String deviceId) {
        Map<Website, String> liveDemoMap = new HashMap<>();
        String extParam1 = AffliIdHelper.getMarketId(marketChannel);
        liveDemoMap.put(Website.FLIPKART, "http://dl.flipkart.com/dl/apple-iphone-5s/p/itme8ra4f4twtsva?affid=affiliate357&affExtParam1=" + extParam1 + "&affExtParam2=" + extParam1 + "_" + deviceId + "_0");
        liveDemoMap.put(Website.SNAPDEAL, "android-app://com.snapdeal.main/snapdeal/m.snapdeal.com/product/apple-iphone-5s-16-gb/1204769399?aff_id=82856&utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_sub=" + extParam1 + "&aff_sub2=" + extParam1 + "_" + deviceId + "_0");
        liveDemoMap.put(Website.SHOPCLUES, "http://www.shopclues.com/apple-iphone-5s-16gb-44.html?ty=0&id=none&mcid=aff&utm_source=Hasoffer&OfferId=15");
        //liveDemoMap.put(Website.EBAY, "http://genlin.ss");
        return liveDemoMap.get(website);
    }

    private String getIndexUrl(Website website, MarketChannel marketChannel, String deviceId) {
        String[] flipkart = new String[]{"raymondzh", "zhangchen", "wangshuom"};
        Random random = new Random();
        Map<Website, String> indexUrlMap = new HashMap<>();
        String flipkartAffid = flipkart[random.nextInt(flipkart.length)];
        String flipkartExtParam1 = AffliIdHelper.getMarketId(marketChannel);
        if ("raymondzh".equals(flipkartAffid)) {
            flipkartExtParam1 = "103662";
        }
        indexUrlMap.put(Website.FLIPKART, "http://dl.flipkart.com/dl/?affid=" + flipkartAffid + "&affExtParam1=" + flipkartExtParam1 + "&affExtParam2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0");

        String[] snapDealAffids = new String[]{"112338"};
        String snapDealAffid = flipkart[random.nextInt(snapDealAffids.length)];
        String snapDealExtParam1 = AffliIdHelper.getMarketId(marketChannel);
        if ("112338".equals(flipkartAffid)) {
            snapDealExtParam1 = "103662";
        }
        indexUrlMap.put(Website.SNAPDEAL, "android-app://com.snapdeal.main/snapdeal/m.snapdeal.com?aff_id=" + snapDealAffid + "&utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_sub=" + snapDealExtParam1 + "&aff_sub2=" + AffliIdHelper.getMarketId(marketChannel) + "_" + deviceId + "_0");
        indexUrlMap.put(Website.SHOPCLUES, "http://www.shopclues.com/?ty=0&id=none&mcid=aff&utm_source=Hasoffer&OfferId=15");
        //indexUrlMap.put(Website.EBAY, "http://genlin.ss/?ty=0&id=none&mcid=aff&utm_source=Hasoffer&OfferId=15");
        String s = indexUrlMap.get(website);
        logger.info("url:{}", s);
        if (s == null) {
            return "";
        } else {
            return new String(org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes(Charset.forName("UTF-8"))));
        }
    }

    @Override
    public List<UrmSignCoin> getUserSignRecord() {
        return dbm.query("select t from UrmSignCoin t ");
    }

}
