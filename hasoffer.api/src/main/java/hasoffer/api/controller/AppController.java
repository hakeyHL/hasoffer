package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.Httphelper;
import hasoffer.api.helper.JsonHelper;
import hasoffer.api.helper.ParseConfigHelper;
import hasoffer.api.worker.SearchLogQueue;
import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.UrmUserGroup;
import hasoffer.base.model.Website;
import hasoffer.base.utils.AffliIdHelper;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import hasoffer.core.admin.impl.DealServiceImpl;
import hasoffer.core.app.vo.*;
import hasoffer.core.bo.product.Banners;
import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.bo.push.*;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.cache.AppCacheManager;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.*;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.urm.*;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.impl.ProductServiceImpl;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.push.IPushService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.IAppService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchedProductReview;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import jodd.util.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created on 2015/12/21.
 */
@Controller
@RequestMapping(value = "/app")
public class AppController {

    @Resource
    ICacheService<UrmUser> userICacheService;
    @Resource
    ICacheService iCacheService;
    private Logger logger = LoggerFactory.getLogger(AppController.class);
    @Resource
    private IAppService appService;
    @Resource
    private CmpSkuCacheManager cmpSkuCacheManager;
    @Resource
    private ProductCacheManager productCacheManager;
    @Resource
    private ProductIndex2ServiceImpl productIndex2Service;
    @Resource
    private ProductServiceImpl productService;
    @Resource
    private ICmpSkuService cmpSkuService;
    @Resource
    private AppCacheManager appCacheManager;
    @Resource
    private IPushService pushService;
    @Resource
    private MongoDbManager mongoDbManager;
    @Resource
    private IOrderStatsAnalysisService orderService;
    @Resource
    private DealServiceImpl dealService;
    @Resource
    private PtmStdSkuIndexServiceImpl ptmStdSkuIndexService;

    public static void main(String[] args) {

        //Date date1 = null;
        //try {
        //    date1 = DateUtils.parseDate("2016-10-05 19:11:00", "yyyy-MM-dd HH:mm:ss");
        //    Date date2 = DateUtils.parseDate("2016-10-02 21:11:00", "yyyy-MM-dd HH:mm:ss");
        //    long days = date1.getTime() / TimeUtils.MILLISECONDS_OF_1_DAY - date2.getTime() / TimeUtils.MILLISECONDS_OF_1_DAY;
        //    System.out.println(days);
        //} catch (ParseException e) {
        //    System.out.println(e.getMessage());
        //    System.out.println(e.getLocalizedMessage());
        //}

    }

    @RequestMapping(value = "/newconfig", method = RequestMethod.GET)
    public ModelAndView config(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();

        Map<Website, String> openDeepLinks = new HashMap<Website, String>();
        openDeepLinks.put(Website.FLIPKART, "http://dl.flipkart.com/dl/apple-iphone-6s/p/itmebysga78az3qh?affid=affiliate357");
        openDeepLinks.put(Website.SNAPDEAL, "https://m.snapdeal.com/product/iphone-6s-16gb/663413326062?utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_id=82856");
//        openDeepLinks.put(Website.SNAPDEAL, "android-app://com.snapdeal.main/snapdeal/m.snapdeal.com/product/iphone-6s-16gb/663413326062?utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_id=82856");

        Website[] siteSort = {Website.FLIPKART, Website.SNAPDEAL};

        Website[] noSelfJump = {Website.FLIPKART};

        AppConfigVo configVo = new AppConfigVo();
        // 打开新的价格视图
        configVo.setShowPrice(true);

        AppCooperative[] acs = new AppCooperative[]{
                new AppCooperative("test", "cn.test", 0, "com.hasoffer.plug.androrid.service.ServiceAccess"),
                new AppCooperative("hasoffer", "com.india.hasoffer", 1, "ndp.pindan.android.service.ServiceAccess"),
                new AppCooperative("shanchuan", "cn.xender", 2, "com.hasoffer.plug.androrid.service.ServiceAccess")
        };

        mav.addObject("openDeepLinks", openDeepLinks);
        mav.addObject("siteSort", siteSort);
        mav.addObject("test", configVo);
        mav.addObject("cooperations", acs);
        mav.addObject("noSelfJump", noSelfJump);

        return mav;
    }

    @RequestMapping(value = "/parseconfig", method = RequestMethod.GET)
    public ModelAndView parseconfig() {
        ModelAndView mav = new ModelAndView();

        mav.addObject("configs", ParseConfigHelper.getParseConfigs());

        return mav;
    }

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ModelAndView eventLog(HttpServletRequest request) {
        return new ModelAndView();
    }

    @RequestMapping(value = "/dot", method = RequestMethod.GET)
    public ModelAndView dot(HttpServletRequest request) {

        String action = request.getParameter("action");
        if ("rediToAffiliateUrl".equals(action)) {
            try {
                String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
                DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                cmpSkuCacheManager.recordFlowControll(deviceId, deviceInfo.getCurShopApp());
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }

        return new ModelAndView();
    }

    @RequestMapping(value = "/giftList", method = RequestMethod.GET)
    public ModelAndView getGiftList() {
        ModelAndView modelAndView = new ModelAndView();
        List<HasofferCoinsExchangeGift> gifts = appService.getGiftList();
        //查询用户是否已登录
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        UrmUser user = appService.getUserByUserToken(userToken);
        if (user != null) {
            modelAndView.addObject("id", user.getId());
            //判断用户属于哪个组
            UrmUserRedeemGroup urmUserRedeemGroup = appService.getUrmRedeemGroupById(user.getId());
            if (urmUserRedeemGroup != null && UrmUserGroup.U001.equals(urmUserRedeemGroup.getGroupName())) {
                //需要修改兑换值100*100=10000 --> 100*50=5000
                //即/2
                for (HasofferCoinsExchangeGift exchangeGift : gifts) {
                    exchangeGift.setCoinPrice(exchangeGift.getCoinPrice() / 2);
                }
            }
        }
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        Map map = new HashMap();
        map.put("gList", gifts == null ? null : gifts);
        modelAndView.addObject("data", map);
        return modelAndView;
    }

    /**
     * 客户端回调
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    public ModelAndView callback(HttpServletRequest request,
                                 @RequestParam CallbackAction action) {
        return callBackMethod(request, action);
    }

    @RequestMapping(value = "/sites", method = RequestMethod.GET)
    public ModelAndView site() {
        List<AppWebsite> appWebsites = appService.getWebsites(true);

        List<AppWebsiteVo> vos = new ArrayList<AppWebsiteVo>();

        ModelAndView mav = new ModelAndView();
        if (ArrayUtils.hasObjs(appWebsites)) {
            for (AppWebsite appWebsite : appWebsites) {
                vos.add(new AppWebsiteVo(appWebsite.getWebsite(),
                        appWebsite.getAppPackage(), WebsiteHelper.getLogoUrl(appWebsite.getWebsite())));
            }
        }

        mav.addObject("sites", vos);
        return mav;
    }

    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    public ModelAndView latest() {

        AppType appType = null;

        DeviceInfoVo deviceInfoVo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        MarketChannel marketChannel = deviceInfoVo.getMarketChannel();
        if (deviceInfoVo == null || deviceInfoVo.getAppType() == null) {
            appType = AppType.APP;
        } else {
            appType = deviceInfoVo.getAppType();
        }
        AppVersion appVersion = null;
        if (marketChannel != null && marketChannel.name().equals("ZUK")) {
            appVersion = appService.getLatestVersion(marketChannel, appType);
        } else {
            appVersion = appService.getLatestVersion(appType);
        }

        ModelAndView mav = new ModelAndView();

        mav.addObject("version", new AppVersionVo(appVersion));
        mav.addObject("getversion", appVersion != null);

        return mav;
    }

    @RequestMapping(value = "/accessinfo", method = RequestMethod.GET)
    public ModelAndView accessinfo() {
        ModelAndView mav = new ModelAndView();

        mav.addObject("searchLogs", SearchLogQueue.getCount());

        return mav;
    }

    @RequestMapping(value = "/backDetail", method = RequestMethod.GET)
    public ModelAndView backDetail() {
        ModelAndView mv = new ModelAndView();
        mv.addObject("errorCode", "00000");
        mv.addObject("errorCode", "msg");
        //若用户未登录显示为已连续签到0
        BackDetailVo data = new BackDetailVo();
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        UrmUser user = appService.getUserByUserToken(userToken);
        // 获取基本配置
        Map<Integer, Integer> afwCfgMap = appService.getSignAwardNum();
        if (user != null) {
            calculateHasofferCoin(Collections.singletonList(user), data);
            //添加返回:
            UrmSignCoin urmSignCoin = appService.getSignCoinByUserId(user.getId());

            Set<Integer> integers = afwCfgMap.keySet();
            Integer max = Collections.max(integers);
            if (urmSignCoin == null) {
                //如果为空,代表还没有签到过
                data.setThisTimeCoin(afwCfgMap.get(1));
                data.setNextTimeCoin(afwCfgMap.get(2));
            } else {
                data.setEverSign(false);
                //判断今天是否已经签过
                long current = new Date().getTime();
                long days = TimeUtils.getIndiaTime(current) / TimeUtils.MILLISECONDS_OF_1_DAY - TimeUtils.getIndiaTime(urmSignCoin.getLastSignTime()) / TimeUtils.MILLISECONDS_OF_1_DAY;
                if (days == 0) {
                    //如果今天已经签到过,返回已签到标识
                    data.setHasSign(true);
                    //明天签到的奖励
                    Integer conSignNum = urmSignCoin.getConSignNum();
                    //最大连续签到数会大于连续奖励数
                    //需要知道Map中的最大key值
                    if (conSignNum + 1 >= max) {
                        data.setThisTimeCoin(afwCfgMap.get(max));
                    } else {
                        data.setThisTimeCoin(afwCfgMap.get(conSignNum + 1));
                    }
                    data.setMaxConSignNum(urmSignCoin.getConSignNum());
                } else if (days == 1) {// 如果是连续签单，则按照连续签到返回数据；
                    //返回已签到+1作为本次,已连续+2作为下次返回
                    Integer conSignNum = urmSignCoin.getConSignNum();
                    //需要知道Map中的最大key值
                    if (conSignNum + 1 >= max) {
                        data.setThisTimeCoin(afwCfgMap.get(max));
                    } else {
                        data.setThisTimeCoin(afwCfgMap.get(conSignNum + 1));
                    }
                    if (conSignNum + 2 >= max) {
                        data.setNextTimeCoin(afwCfgMap.get(max));
                    } else {
                        data.setNextTimeCoin(afwCfgMap.get(conSignNum + 2));
                    }
                    data.setMaxConSignNum(urmSignCoin.getConSignNum());
                } else if (days > 1) {
                    // 如果不是，则重新从0开始；
                    data.setThisTimeCoin(afwCfgMap.get(1));
                    data.setNextTimeCoin(afwCfgMap.get(2));
                    // 当前最大连续签到次数
                    data.setMaxConSignNum(0);
                }
                //4. verified coin = approved*10+签到获得.
                data.setVerifiedCoins(data.getVerifiedCoins().add(BigDecimal.valueOf(urmSignCoin.getSignCoin())));
            }

        } else {
            data.setThisTimeCoin(afwCfgMap.get(1) == null ? 0 : afwCfgMap.get(1));
            //未登录返回连续签到次数为0
            data.setMaxConSignNum(0);
        }
//        data.setAuxiliaryCheck(true);
        data.setAuxiliaryCheck(true);
       /* DeviceInfoVo deviceInfoVo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        if (deviceInfoVo != null && StringUtils.isNotEmpty(deviceInfoVo.getAppVersion())) {
            String appVersion = deviceInfoVo.getAppVersion();
            try {
                int version = Integer.valueOf(appVersion);
                if (version > 0 && version == 31) {
                    data.setAuxiliaryCheck(false);
                }
            } catch (Exception e) {
                logger.debug(Thread.currentThread().getId() + " time {} , transfer string number {} to number failed .", new Date(), appVersion);
            }
        }*/
        //set sign in and rewards config map
        data.setSinDaysRewardsCfg(afwCfgMap);
        if (data.getSinDaysRewardsCfg() != null) {
            Map<Integer, Integer> sinDaysRewardsCfg = data.getSinDaysRewardsCfg();

            data.setSignConfigKeys(sinDaysRewardsCfg.keySet().toArray(new Integer[]{}));
            data.setSignConfigValus(sinDaysRewardsCfg.values().toArray(new Integer[]{}));

            if (sinDaysRewardsCfg.get(sinDaysRewardsCfg.size()) != null) {
                data.setSignMoreCoin(sinDaysRewardsCfg.get(sinDaysRewardsCfg.size()));
            } else {
                Iterator<Integer> iterator = sinDaysRewardsCfg.keySet().iterator();
                int lastDayReward = 1;
                while (iterator.hasNext()) {
                    lastDayReward = iterator.next();
                }
                data.setSignMoreCoin(sinDaysRewardsCfg.get(lastDayReward));
            }
        }
        mv.addObject("data", data);
        return mv;
    }


    /**
     * banners列表
     *
     * @return
     */
    @RequestMapping(value = "/banners", method = RequestMethod.GET)
    public ModelAndView banners() {
        ModelAndView mv = new ModelAndView();
        List banners = new ArrayList();
        List<AppBanner> list = appService.getBanners();
        for (AppBanner appBanner : list) {
            Banners banner = new Banners();
            banner.setRank(appBanner.getRank());
            banner.setSource(1);
            banner.setSourceUrl(appBanner.getImageUrl() == null ? "" : ImageUtil.getImageUrl(appBanner.getImageUrl()));
            banner.setExpireDate(appBanner.getDeadline());
            banner.setDealId(Long.valueOf(appBanner.getSourceId()));
            banners.add(banner);
        }
        Map map = new HashMap();
        map.put("banners", banners);
        mv.addObject("data", map);
        return mv;
    }

    /**
     * deal列表
     *
     * @return
     */
    @DataSource(value = DataSourceType.Slave)
    @RequestMapping(value = "/deals", method = RequestMethod.GET)
    public ModelAndView deals(@RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue = "8") String pageSize) {
        //1. 从数据库中查询到
        ModelAndView mv = new ModelAndView();
        //忽略前端传页面大小
        PageableResult Result = appService.getDeals(Long.valueOf(page), Long.valueOf(8));
        Map map = new HashMap();
        List li = new ArrayList();
       /* getDealsFromCache(li, Integer.parseInt(page), 8);
        boolean flag = true;
        if (li.size() < 1) {*/
        List<AppDeal> deals = Result.getData();
        Date currentDate = new Date();
        for (AppDeal appDeal : deals) {
            int dateCmpResult = currentDate.compareTo(appDeal.getExpireTime());
            DealVo dealVo = new DealVo();
            setDeal(appDeal, dealVo);
            if (dateCmpResult <= 0) {
                dealVo.setIsExpired(false);
                li.add(dealVo);
            } else {
                dealVo.setIsExpired(true);
                li.add(dealVo);
            }
//            }
//            flag = false;
        }
     /*   if (li.size() > 0 && flag) {
            setDeals2Cache(li, Integer.parseInt(page), 8);
        }*/
        map.put("deals", li);
        map.put("currentPage", Result.getCurrentPage());
        map.put("NumFund", Result.getNumFund());
        map.put("page", Result.getPageSize());
        map.put("pageSize", Result.getPageSize());
        map.put("totalPage", Result.getTotalPage());
        mv.addObject("data", map);
        return mv;
    }

    /**
     * deal详情
     *
     * @param id
     * @return
     */
    @DataSource(value = DataSourceType.Slave)
    @RequestMapping(value = "/dealInfo", method = RequestMethod.GET)
    public ModelAndView getdealInfo(@RequestParam String id) {
        //临时按照appVersion区分返回描述
        ModelAndView mv = new ModelAndView();
        mv.addObject("errorCode", "00000");
        mv.addObject("msg", "ok");
        return getDealInfoMethod(id, mv);
    }

    /**
     * 用户信息绑定
     *
     * @return
     */
    @RequestMapping(value = "/bindUserInfo", method = RequestMethod.POST)
    public String bindUserInfo(UserVo userVO,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        String lastTimeUserToken = request.getHeader("oldUserToken");

        Map map = new HashMap();
        String userToken = UUID.randomUUID().toString();
        String deviceId = JSON.parseObject(request.getHeader("deviceinfo")).getString("deviceId");
        //String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
//        System.out.println(" get deviceId is : " + deviceId);
        //1. 根据deviceId获得device 的id列表
        List<String> ids = appService.getUserDevices(deviceId);
//        System.out.println(" get ids by deviceId :" + ids.size());

        UrmUser uUser = appService.getUserByThirdId(StringUtils.isEmpty(userVO.getThirdId()) ? "-" : userVO.getThirdId());
        if (uUser == null) {
//            logger.debug("user is not exist before");
            uUser = new UrmUser();
            uUser.setUserToken(userToken);
            uUser.setAvatarPath(userVO.getUserIcon());
            uUser.setCreateTime(new Date());
            uUser.setTelephone(userVO.getTelephone() == null ? "" : userVO.getTelephone());
            uUser.setThirdPlatform(userVO.getPlatform());
            uUser.setThirdToken(userVO.getToken());
            uUser.setUserName(userVO.getUserName());
            uUser.setThirdId(userVO.getThirdId());
            appService.addUser(uUser);

        } else {
//            logger.debug("user exist ,update userInfo");
            uUser.setUserName(userVO.getUserName());
            uUser.setThirdPlatform(userVO.getPlatform());
            uUser.setTelephone(uUser.getTelephone());
            uUser.setAvatarPath(uUser.getAvatarPath());
            uUser.setThirdToken(uUser.getThirdToken());
            uUser.setUserToken(userToken);
            appService.updateUserInfo(uUser);
            //把最新的usertoken放进去
            if (!StringUtils.isEmpty(lastTimeUserToken)) {
                lastTimeUserToken = userToken;
            }
            List<String> deviceIds = appService.getUserDevicesByUserId(uUser.getId() + "");
            List<UrmUserDevice> urmUserDevices = new ArrayList<>();
            ApiUtils.bindUserAndDevices(uUser, ids, deviceIds, urmUserDevices);
            //将关联关系插入到关联表中
            appService.addUrmUserDevice(urmUserDevices);
        }
        map.put("userToken", userToken);
        jsonObject.put("data", map);
        String thirdId = userVO.getThirdId();
        if (StringUtils.isEmpty(lastTimeUserToken) || StringUtils.isEmpty(thirdId)) {//如果userToken或者thirdId为空
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        //如果是third不变的情况下,usertoken已经被更新了,应该用新的usertoken操作
        UrmUser userByLastUserToken = appService.getUserByUserToken(lastTimeUserToken);

        if (userByLastUserToken != null) {
            //如果老token有对应用户,存起来,方便二次处理
            String oldThirdId = userByLastUserToken.getThirdId();
            List<UrmUser> oldUserList = appService.getIdDescUserListByThirdId(oldThirdId);
            if (oldUserList == null || oldUserList.size() == 0) {
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }
            if (StringUtils.equals(thirdId, oldThirdId)) {//如果同样的userToken对应的记录只有一条并且thirdId一致，认为是正确的用户信息
                //如果老的thirdId和新的thirdId一样的话要清除此third下的多个记录的问题
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }
            if (!StringUtils.equals(thirdId, oldThirdId) && oldUserList.size() == 1) {
                //可能是老版本的用户升级,要将老版本用户的订单迁移到新版本
                for (int i = 0; i < oldUserList.size(); i++) {
                    orderService.mergeOldUserOrderToNewUser(oldUserList.get(i).getId() + "", uUser.getId() + "");//转移订单
                    appService.bakUserInfo(oldUserList.get(i));//备份用户数据
                }
            } else {
                //size一定是大于1的
                for (int i = 1; i < oldUserList.size(); i++) {
                    //取出id最大的用户记录,将其他记录的订单数据都更新到此记录中
                    orderService.mergeOldUserOrderToNewUser(oldUserList.get(i).getId() + "", oldUserList.get(0).getId() + "");//转移订单
                    appService.bakUserInfo(oldUserList.get(i));//备份用户数据
                }
            }
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    /**
     * 用户信息获取
     * 计算用户签到和返利总额
     *
     * @return
     */
    @DataSource(value = DataSourceType.Slave)
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public ModelAndView userInfo() {
        ModelAndView mv = new ModelAndView();
        BigDecimal coins = BigDecimal.ZERO;
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        UrmUser user = appService.getUserByUserToken(userToken);
        if (user != null) {
//            BackDetailVo backDetailVo = new BackDetailVo();
//            calculateHasofferCoin(Collections.singletonList(user), backDetailVo);
            UserVo userVo = new UserVo();
            userVo.setName(user.getUserName());
            List<OrderStatsAnalysisPO> orders = appService.getBackDetails(user.getId().toString());
            for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
                String orderStatus = orderStatsAnalysisPO.getOrderStatus();
                if (orderStatsAnalysisPO.getWebSite().equals(Website.FLIPKART.name()) && orderStatus != null && (orderStatus.equals("tentative") || orderStatus.equals("approved"))) {
                    BigDecimal tempPrice = orderStatsAnalysisPO.getSaleAmount().multiply(BigDecimal.valueOf(0.075)).min(orderStatsAnalysisPO.getTentativeAmount());
                    coins = coins.add(tempPrice);
                }
            }
            coins = coins.multiply(BigDecimal.TEN);
//            coins = coins.add(backDetailVo.getPendingCoins());
//            coins = coins.add(backDetailVo.getVerifiedCoins());
//            coins = coins.multiply(BigDecimal.TEN);
            UrmSignCoin urmSignCoin = appService.getSignCoinByUserId(user.getId());
            if (urmSignCoin != null) {
                coins = coins.add(BigDecimal.valueOf(urmSignCoin.getSignCoin()));
            }
            coins = coins.setScale(1, BigDecimal.ROUND_HALF_UP);
            userVo.setCoins(coins);
            userVo.setUserIcon(user.getAvatarPath());
            mv.addObject("data", userVo);
        }
        return mv;
    }

    /**
     * 商品类目
     *
     * @return
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public ModelAndView category(String categoryId) {
        ModelAndView mv = new ModelAndView();
        List categorys = null;
        categorys = appCacheManager.getCategorys(categoryId);
        mv.addObject("data", categorys);
        return mv;
    }

    /**
     * 商品列表
     *
     * @return
     */
    @DataSource(value = DataSourceType.Slave)
    @RequestMapping(value = "/productsList")
    public ModelAndView productsList(SearchCriteria criteria, @RequestParam(defaultValue = "4") int type) {
        long l = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " :  criteria : " + criteria.toString());
        ModelAndView mv = new ModelAndView();
        List li = new ArrayList();
        Map map = new HashMap();
        PageableResult products;
        //查询热卖商品
        switch (type) {
            case 0:
                List<PtmProduct> products2s = productCacheManager.getTopSellins(criteria.getPage(), criteria.getPageSize());
                addProductVo2List(li, products2s);
                if (products2s != null && products2s.size() > 4) {
                    li = li.subList(0, 5);
                }
                map.put("product", li);
                break;
            case 1:
                List<PtmProduct> topSellins = productCacheManager.getTopSellins(criteria.getPage(), criteria.getPageSize());
                addProductVo2List(li, topSellins);
                map.put("product", li);
                break;
            case 2:
                //search by title
                criteria.setPivotFields(Arrays.asList("cate2", "cate3"));
                PageableResult p;
                p = ptmStdSkuIndexService.searchProducts(criteria);
                if (p == null || p.getData() == null || p.getData().size() < 1) {
                    p = productIndex2Service.searchProducts(criteria);
                }
                if (p != null && p.getData().size() > 0) {
                    System.out.println("getPivotFieldVals  " + p.getPivotFieldVals().size());
                    getSkuListByKeyword(map, p);
                    //如果是价格由低到高排序或者按照价格区间排序不过滤配件信息
                    boolean filterProductFlag = true;
                    if (criteria.getSort().name().equals("PRICEL2H")) {
                        filterProductFlag = false;
                    }
                    //如果最低价和最高价有值,且是有效区间,不执行配件过滤
                    if (criteria.getMaxPrice() == null || criteria.getMaxPrice() <= 0) {
                        filterProductFlag = false;
                    }
                    if (filterProductFlag) {
                        filterProducts(p.getData(), criteria.getKeyword());
                    }
                    addProductVo2List(li, p.getData());
                }
                map.put("product", li);
                break;
            case 3:
                //类目搜索
                //category level page size
                if (StringUtils.isNotBlank(criteria.getCategoryId())) {
                    //search by category
                    products = ptmStdSkuIndexService.searchStdPricesByCategory(criteria);
                    if (products == null || products.getData() == null || products.getData().size() < 1) {
                        products = productIndex2Service.searchPro(criteria);
                    }
                    if (products != null && products.getData().size() > 0) {
                        addProductVo2List(li, products.getData());
                    }
                }
                break;
            case 4:
                //如果是默认值,则判断类目id和level是否传递了,传了就是类目搜索,适配老接口
                if (StringUtils.isNotBlank(criteria.getCategoryId())) {
                    //search by category
                    products = productIndex2Service.searchPro(criteria);
                    if (products != null && products.getData().size() > 0) {
                        addProductVo2List(li, products.getData());
                    }
                } else if (StringUtils.isNotBlank(criteria.getKeyword())) {
                    PageableResult pKeywordResult = productIndex2Service.searchProducts(criteria);
                    if (pKeywordResult != null && pKeywordResult.getData().size() > 0) {
                        filterProducts(pKeywordResult.getData(), criteria.getKeyword());
                        addProductVo2List(li, pKeywordResult.getData());
                        map.put("product", li);
                    }
                }
            default:
                break;
        }
        if (li != null && li.size() > 0) {
            map.put("product", li);
        }
        mv.addObject("data", map);
        System.out.println("time " + (System.currentTimeMillis() - l) / 1000);
        return mv;
    }

    private void getSkuListByKeyword(Map map, PageableResult p) {
        if (p.getPivotFieldVals() != null && p.getPivotFieldVals().size() > 0) {
            // List<CategoryVo>
            List<CategoryVo> secondCategoryList = new ArrayList();
            List<CategoryVo> categorys = new ArrayList();
            List<CategoryVo> thirdCategoryList = new ArrayList();
            Map pivotFieldVals = p.getPivotFieldVals();
            Set<Map.Entry> set = pivotFieldVals.entrySet();
            Iterator<Map.Entry> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry next = iterator.next();
                List<NameValue> nameValues = (List<NameValue>) next.getValue();
                System.out.println("cate " + next.getKey() + " ::: nameValues  :" + nameValues.size());
                for (NameValue nameValue : nameValues) {
                    Long cateId = Long.valueOf(nameValue.getName() + "");
                    //可能是二级也可能是三级 ,二级的放一块,三级的放一块
                    if (cateId > 0) {
                        PtmCategory ptmCategory = appCacheManager.getCategoryById(cateId);
                        if (ptmCategory != null && ptmCategory.getLevel() == 2) {
                            //处理二级类目
                            CategoryVo categoryVo = new CategoryVo();
                            categoryVo.setId(ptmCategory.getId());
                            categoryVo.setLevel(ptmCategory.getLevel());
                            categoryVo.setParentId(ptmCategory.getParentId());
                            categoryVo.setRank(ptmCategory.getRank());
                            categoryVo.setName(ptmCategory.getName());
                            categoryVo.setHasChildren(0);
                            secondCategoryList.add(categoryVo);
                        } else if (ptmCategory != null && ptmCategory.getLevel() == 3) {
                            //处理三级类目
                            CategoryVo categoryVo3 = new CategoryVo();
                            categoryVo3.setId(ptmCategory.getId());
                            categoryVo3.setLevel(ptmCategory.getLevel());
                            categoryVo3.setParentId(ptmCategory.getParentId());
                            categoryVo3.setRank(ptmCategory.getRank());
                            categoryVo3.setName(ptmCategory.getName());
                            categoryVo3.setHasChildren(0);
                            thirdCategoryList.add(categoryVo3);
                        }
                    }
                }
            }
            //获取到类目id appCacheManager.getCategorys(categoryId);
            //先获取一级类目列表
            List<CategoryVo> firstCategoryList = appCacheManager.getCategorys("");
            //对二级类目按照rank排序
            Collections.sort(secondCategoryList, new Comparator<CategoryVo>() {
                @Override
                public int compare(CategoryVo o1, CategoryVo o2) {
                    if (o1.getRank() > o2.getRank()) {
                        return 1;
                    } else if (o1.getRank() < o2.getRank()) {
                        return -1;
                    }
                    return 0;
                }
            });

            //遍历一级类目将二级类目匹配排序
            for (CategoryVo firstPtmCategory : firstCategoryList) {
                for (CategoryVo cate : secondCategoryList) {
                    //遍历所有,如果父类id是其则加入list
                    if (cate.getParentId().equals(firstPtmCategory.getId())) {
                        categorys.add(cate);
                    }
                }
            }

            //遍历二级类目,将三级类目匹配排序和归类
            Iterator<CategoryVo> iterator1 = categorys.iterator();
            while (iterator1.hasNext()) {
                List<CategoryVo> tempThirdCategoryList = new ArrayList();
                CategoryVo next = iterator1.next();
                for (CategoryVo cate : thirdCategoryList) {
                    //遍历所有,如果父类id是其则加入list
                    if (cate.getParentId().equals(next.getId())) {
                        tempThirdCategoryList.add(cate);
                    }
                }

                //对三级类目按照rank排序
                Collections.sort(tempThirdCategoryList, new Comparator<CategoryVo>() {
                    @Override
                    public int compare(CategoryVo o1, CategoryVo o2) {
                        if (o1.getRank() > o2.getRank()) {
                            return 1;
                        } else if (o1.getRank() < o2.getRank()) {
                            return -1;
                        }
                        return 0;
                    }
                });
                if (tempThirdCategoryList.size() > 0) {
                    next.setHasChildren(1);
                }
                next.setCategorys(tempThirdCategoryList);
            }
            map.put("categorys", categorys);
        }
    }

    public void addProductVo2List(List desList, List sourceList) {

        if (sourceList != null && sourceList.size() > 0) {
            if (PtmProduct.class.isInstance(sourceList.get(0))) {
                Iterator<PtmProduct> ptmList = sourceList.iterator();
                while (ptmList.hasNext()) {
                    PtmProduct ptmProduct = ptmList.next();
                    int count = cmpSkuService.getSkuSoldStoreNum(ptmProduct.getId());
                    if (count > 0) {
                        ProductListVo productListVo = new ProductListVo();
                        productListVo.setId(ptmProduct.getId());
                        productListVo.setImageUrl(productCacheManager.getProductMasterImageUrl(ptmProduct.getId()));
                        productListVo.setName(ptmProduct.getTitle());
                        productListVo.setPrice(Math.round(ptmProduct.getPrice()));
                        productListVo.setStoresNum(count);
                        setCommentNumAndRatins(productListVo);
                        desList.add(productListVo);
                    }
                }
            } else if (ProductModel2.class.isInstance(sourceList.get(0))) {
                Iterator<ProductModel2> ptmList = sourceList.iterator();
                while (ptmList.hasNext()) {
                    ProductModel2 ptmProduct = ptmList.next();
                    ProductListVo productListVo = new ProductListVo();
                    productListVo.setId(ptmProduct.getId());
                    productListVo.setImageUrl(productCacheManager.getProductMasterImageUrl(ptmProduct.getId()));
                    productListVo.setName(ptmProduct.getTitle());
                    productListVo.setPrice(Math.round(ptmProduct.getMinPrice()));
                    productListVo.setRatingNum(ptmProduct.getRating());
                    productListVo.setCommentNum(Long.valueOf(ptmProduct.getReview()));
                    productListVo.setStoresNum(ptmProduct.getStoreCount());
                    desList.add(productListVo);
                }
            } else if (PtmStdSkuModel.class.isInstance(sourceList.get(0))) {
                Iterator<PtmStdSkuModel> ptmList = sourceList.iterator();
                while (ptmList.hasNext()) {
                    PtmStdSkuModel ptmStdSkuModel = ptmList.next();
                    ProductListVo productListVo = new ProductListVo();
                    productListVo.setId(ptmStdSkuModel.getId());
                    productListVo.setImageUrl(productCacheManager.getPtmStdSkuImageUrl(ApiUtils.rmoveBillion(ptmStdSkuModel.getId())));
                    productListVo.setName(ptmStdSkuModel.getTitle());
                    productListVo.setPrice(Math.round(ptmStdSkuModel.getMinPrice()));
                    productListVo.setRatingNum(ptmStdSkuModel.getRating());
                    productListVo.setCommentNum(Long.valueOf(ptmStdSkuModel.getReview()));
                    productListVo.setStoresNum(ptmStdSkuModel.getStoreCount());
                    desList.add(productListVo);
                }
            }
        }
    }

    public void setCommentNumAndRatins(ProductListVo productListVo) {
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listPagedCmpSkus(productListVo.getId(), 1, 20);
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            List<PtmCmpSku> tempSkuList = pagedCmpskus.getData();
            //计算评论数*星级的总和
            int sum = 0;
            //统计site
            Set<Website> websiteSet = new HashSet<Website>();
            for (PtmCmpSku ptmCmpSku : tempSkuList) {
                websiteSet.add(ptmCmpSku.getWebsite());
            }
            Long totalCommentNum = Long.valueOf(0);
            for (PtmCmpSku ptmCmpSku2 : tempSkuList) {
                if (websiteSet.size() <= 0) {
                    break;
                }
                if (websiteSet.contains(ptmCmpSku2.getWebsite())) {
                    websiteSet.remove(ptmCmpSku2.getWebsite());
//                    System.out.println("count comment ans stats exclude  ebay ");
                    if (!ptmCmpSku2.getWebsite().equals(Website.EBAY)) {
                        //评论数*星级 累加 除以评论数和
                        sum += ptmCmpSku2.getRatings() * ptmCmpSku2.getCommentsNumber();
                        //去除列表中除此之外的其他此site的数据
                        totalCommentNum += ptmCmpSku2.getCommentsNumber();
                    }
                }
            }
//            System.out.println("totalCommentNum   " + totalCommentNum);
            productListVo.setCommentNum(totalCommentNum);
            int rating = ClientHelper.returnNumberBetween0And5(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(totalCommentNum == 0 ? 1 : totalCommentNum), 0, BigDecimal.ROUND_HALF_UP).longValue());
            productListVo.setRatingNum(rating <= 0 ? 90 : rating);
        }
    }

    @RequestMapping(value = "/push")
    public ModelAndView psuhMessage(String title, String content, String app, String version, String marketChannel, String outline, String packageName, String type, String id, int number) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("errorCode", "00000");
        mv.addObject("msg", "ok");
        try {
            List<String> gcmTokens = new ArrayList<String>();
            AppPushMessage message = new AppPushMessage(
                    new AppMsgDisplay(outline, title, content),
                    new AppMsgClick(AppMsgClickType.valueOf(type), id, packageName)
            );
            AppPushBo pushBo = new AppPushBo("5x1", "15:10", message);
            //安装了指定app的、指定数量、指定包名、指定类型、指定id推送
            List<UrmDevice> urmDevices = pushService.getGcmTokens(version);
            for (UrmDevice urmDevice : urmDevices) {
                String shopApps = urmDevice.getShopApp();
                String[] split = shopApps.split(",");
                for (String str : split) {
                    if (urmDevice.getMarketChannel() != null) {
                        if (str.equals(app) && urmDevice.getMarketChannel().name().equals(marketChannel)) {
                            if (gcmTokens.size() < number && !StringUtils.isEmpty(urmDevice.getGcmToken())) {
                                gcmTokens.add(urmDevice.getGcmToken());
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            int i = 0;
            for (String gcmToken : gcmTokens) {
                System.out.println("____  " + i + "  ____");
                pushService.push(gcmToken, pushBo);
                i++;
            }
        } catch (Exception e) {
            mv.addObject("msg", "faild " + e.getMessage());
            return mv;
        }
        return mv;
    }

    //搜索词提示
    @RequestMapping(value = "candidateKeyword", method = RequestMethod.GET)
    public ModelAndView getSearchKeyWordsTip(@RequestParam(defaultValue = "") String keyWord) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        Map map = new HashMap();
        List<String> spellcheck = productService.spellcheck(keyWord);
        int size = spellcheck.size() > 2 ? 3 : spellcheck.size();
        map.put("words", spellcheck.subList(0, size));
        modelAndView.addObject("data", map);
        return modelAndView;
    }

    public void filterProducts(List productList, String keyord) {
        if (productList != null && productList.size() > 0) {
            if (ProductModel2.class.isInstance(productList.get(0))) {
                System.out.println("enter enter enter .....");
                Iterator<ProductModel2> ptmList = productList.iterator();
                while (ptmList.hasNext()) {
                    //筛选title
                    ProductModel2 next = ptmList.next();
                    boolean b = ClientHelper.FilterProducts(next.getTitle(), keyord);
                    if (!b) {
                        //false移除
                        ptmList.remove();
                    }
                }
            }
        }
    }

    public void calculateHasofferCoin(List<UrmUser> users, BackDetailVo data) {
        List<OrderVo> transcations = new ArrayList<OrderVo>();
        BigDecimal pendingCoins = BigDecimal.ZERO;
        BigDecimal verifiedCoins = BigDecimal.ZERO;
        for (UrmUser user : users) {
            List<OrderStatsAnalysisPO> orders = appService.getBackDetails(user.getId().toString());
            for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
                if (orderStatsAnalysisPO.getWebSite().equals(Website.FLIPKART.name())) {
                    OrderVo orderVo = new OrderVo();
                    BigDecimal tempPrice = orderStatsAnalysisPO.getSaleAmount().multiply(BigDecimal.valueOf(0.075)).min(orderStatsAnalysisPO.getTentativeAmount());
                    //乘以10再取整
                    tempPrice = tempPrice.multiply(BigDecimal.TEN);
                    orderVo.setAccount(tempPrice.divide(BigDecimal.ONE, 1, BigDecimal.ROUND_HALF_UP));
                    orderVo.setChannel(orderStatsAnalysisPO.getChannel());
                    orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
                    orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
                    orderVo.setWebsite(orderStatsAnalysisPO.getWebSite());
                    orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
                    transcations.add(orderVo);
                    if (orderStatsAnalysisPO.getOrderStatus() != null) {
                        if (!orderStatsAnalysisPO.getOrderStatus().equals("cancelled") && !orderStatsAnalysisPO.getOrderStatus().equals("disapproved")) {
                            if (!orderStatsAnalysisPO.getOrderStatus().equals("approved")) {
                                pendingCoins = pendingCoins.add(tempPrice);
                            }
                        }
                        if (orderStatsAnalysisPO.getOrderStatus().equals("approved")) {
                            verifiedCoins = verifiedCoins.add(tempPrice);
                        }
                    }

                }
            }
        }
        //待定的
        data.setPendingCoins(pendingCoins.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        //可以使用的
        verifiedCoins = verifiedCoins.multiply(BigDecimal.TEN);
        data.setVerifiedCoins(verifiedCoins.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        data.setTranscations(transcations);
    }

    private ModelAndView callBackMethod(HttpServletRequest request, @RequestParam CallbackAction action) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        DeviceInfoVo deviceInfoVo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        MarketChannel marketChannel = MarketChannel.NONE;
        if (deviceInfoVo != null) {
            marketChannel = deviceInfoVo.getMarketChannel();
        }
        switch (action) {
            case FLOWCTRLSUCCESS:
                // 流量拦截成功
                try {
                    //DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                    String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
                    cmpSkuCacheManager.recordFlowControll(deviceId, deviceInfoVo.getCurShopApp());
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
                break;
            case HOMEPAGE:
                Map map = new HashMap();
                //DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                //marketChannel = deviceInfo.getMarketChannel();
                map.put("info", AffliIdHelper.getAffiIds(marketChannel));
                modelAndView.addObject("data", map);
                break;
            case INDEXPAGE:
                String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
                List<Map<String, String>> list = appService.getIndexPage(marketChannel, deviceId);
                modelAndView.addObject("data", list);
                break;
            case CLICKDEAL:
                String id = request.getParameter("value");
                System.out.println(" id  id  deal  " + id);
                AppDeal appDeal = appService.getDealDetail(Long.valueOf(id));
                if (appDeal != null) {
                    appService.countDealClickCount(appDeal);
                }
                break;
            case DOWNLOADBOOTCONFIG:
                //app下载引导
                List<Map<String, List<ThirdAppVo>>> apps = new ArrayList<Map<String, List<ThirdAppVo>>>();
                Map<String, List<ThirdAppVo>> NINEAPP = new HashMap<>();
                Map<String, List<ThirdAppVo>> GOOGLEPLAY = new HashMap<>();

                //添加GooglePlay渠道的app下载属性
                List<ThirdAppVo> tempGOOGLEPLAY = new ArrayList<ThirdAppVo>();
                ThirdAppVo googlePlayApps_Amazon = new ThirdAppVo(Website.AMAZON, AppAdController.packageMap.get(Website.AMAZON), "https://play.google.com/store/apps/details?id=com.amazon.mShop.android.shopping", WebsiteHelper.getLogoUrl(Website.AMAZON), "Browse,search & buy millions of products right from your Android device", 4.3f, "491,637", "50,000,000 - 100,000,000", "9.6MB");
                ThirdAppVo googlePlayApps_Flipkart = new ThirdAppVo(Website.FLIPKART, AppAdController.packageMap.get(Website.FLIPKART), "https://play.google.com/store/apps/details?id=com.flipkart.android", WebsiteHelper.getLogoUrl(Website.FLIPKART), "Shop for electronics,apparels & more using our Flipart app Free shipping & COD", 4.2f, "2,044,978", "50,000,000 - 100,000,000", "10.0MB");
                ThirdAppVo googlePlayApps_ShopClues = new ThirdAppVo(Website.SHOPCLUES, AppAdController.packageMap.get(Website.SHOPCLUES), "https://play.google.com/store/apps/details?id=com.shopclues", WebsiteHelper.getLogoUrl(Website.SHOPCLUES), "India's largest Online Marketplace is now in your Pocket - Install,Shop,Enjoy!", 3.9f, "235,468", "10,000,000 - 50,000,000", "7.1MB");
                ThirdAppVo googlePlayApps_eBay = new ThirdAppVo(Website.EBAY, AppAdController.packageMap.get(Website.EBAY), "https://play.google.com/store/apps/details?id=com.ebay.mobile", WebsiteHelper.getLogoUrl(Website.EBAY), "Buy,bid & sell! Deals & Discounts to Save Money on Home,Collectables & Cars", 4.2f, "1,759,547", "100,000,000 - 500,000,000", "20.6MB");
                ThirdAppVo googlePlayApps_Paytm = new ThirdAppVo(Website.PAYTM, AppAdController.packageMap.get(Website.PAYTM), "https://play.google.com/store/apps/details?id=net.one97.paytm", WebsiteHelper.getLogoUrl(Website.PAYTM), "Best Mobile Recharge and DTH Recharge, Bill Payment and Shipping Experience", 4.3f, "1,401,209", "10,000,000 - 50,000,000", "13.0MB");
                ThirdAppVo googlePlayApps_Snapdeal = new ThirdAppVo(Website.SNAPDEAL, AppAdController.packageMap.get(Website.SNAPDEAL), "https://play.google.com/store/apps/details?id=com.snapdeal.main", WebsiteHelper.getLogoUrl(Website.SNAPDEAL), "Best deals on women & men's fashion,home essentials,electronics & gadgets!", 4.1f, "1,035,900", "10,000,000 - 50,000,000", "12.0MB");
                ThirdAppVo googlePlayApps_Jabong = new ThirdAppVo(Website.JABONG, AppAdController.packageMap.get(Website.JABONG), "https://play.google.com/store/apps/details?id=com.jabong.android", WebsiteHelper.getLogoUrl(Website.JABONG), "India's Best Online Shopping App To Buy Latest Fashion for Men,Women,Kids", 3.9f, "171,487", "10,000,000 - 50,000,000", "6.1MB");
                ThirdAppVo googlePlayApps_VOONIK = new ThirdAppVo(Website.VOONIK, AppAdController.packageMap.get(Website.VOONIK), "https://play.google.com/store/apps/details?id=com.voonik.android", WebsiteHelper.getLogoUrl(Website.VOONIK), "Online Shopping for women clothing,ethnic wear,sarees,kurtis,lingere in India", 4.2f, "129,079", "5,000,000 - 10,000,000", "5.8MB");
                ThirdAppVo googlePlayApps_INFIBEAM = new ThirdAppVo(Website.INFIBEAM, AppAdController.packageMap.get(Website.INFIBEAM), "https://play.google.com/store/apps/details?id=com.infibeam.infibeamapp", WebsiteHelper.getLogoUrl(Website.INFIBEAM), "Infibeam.com-Buy Mobiles,Electronics,Books,Gifts,Clothes & more", 3.7f, "8,424", "1,000,000 - 5,000,000", "26.2MB");
                ThirdAppVo googlePlayApps_Myntra = new ThirdAppVo(Website.MYNTRA, AppAdController.packageMap.get(Website.MYNTRA), "https://play.google.com/store/apps/details?id=com.myntra.android&hl=en", WebsiteHelper.getLogoUrl(Website.MYNTRA), "Online shopping for fashion clothes,footwear,accessories for Men,Women & Kids", 4.1f, "509,053", "10,000,000 - 50,000,000", "17.2MB");

                tempGOOGLEPLAY.addAll(Arrays.asList(googlePlayApps_Amazon, googlePlayApps_Flipkart, googlePlayApps_ShopClues, googlePlayApps_eBay, googlePlayApps_Paytm, googlePlayApps_Snapdeal, googlePlayApps_Jabong, googlePlayApps_VOONIK, googlePlayApps_INFIBEAM, googlePlayApps_Myntra));
                GOOGLEPLAY.put("GOOGLEPLAY", tempGOOGLEPLAY);

                //添加9APP渠道的app下载属性
                List<ThirdAppVo> tempNINEAPP = new ArrayList<ThirdAppVo>();
                ThirdAppVo nineApp_Amazon = new ThirdAppVo(Website.AMAZON, AppAdController.packageMap.get(Website.AMAZON), "http://www.9apps.com/android-apps/Amazon-India-Shopping/", WebsiteHelper.getLogoUrl(Website.AMAZON), "Browse,search & buy millions of products right from your Android device", 4.3f, "491,637", "50,000,000 - 100,000,000", "9.6MB");
                ThirdAppVo nineApp_Flipkart = new ThirdAppVo(Website.FLIPKART, AppAdController.packageMap.get(Website.FLIPKART), "http://www.9apps.com/android-apps/Flipkart-Amazing-Discounts-Everyday/", WebsiteHelper.getLogoUrl(Website.FLIPKART), "Shop for electronics,apparels & more using our Flipart app Free shipping & COD", 4.2f, "2,044,978", "50,000,000 - 100,000,000", "10.0MB");
                ThirdAppVo nineApp_ShopClues = new ThirdAppVo(Website.SHOPCLUES, AppAdController.packageMap.get(Website.SHOPCLUES), "http://www.9apps.com/android-apps/ShopClues/", WebsiteHelper.getLogoUrl(Website.SHOPCLUES), "India's largest Online Marketplace is now in your Pocket - Install,Shop,Enjoy!", 3.9f, "235,468", "10,000,000 - 50,000,000", "7.1MB");
                ThirdAppVo nineApp_eBay = new ThirdAppVo(Website.EBAY, AppAdController.packageMap.get(Website.EBAY), "http://www.9apps.com/android-apps/eBay/", WebsiteHelper.getLogoUrl(Website.EBAY), "Buy,bid & sell! Deals & Discounts to Save Money on Home,Collectables & Cars", 4.2f, "1,759,547", "100,000,000 - 500,000,000", "20.6MB");
                ThirdAppVo nineApp_Paytm = new ThirdAppVo(Website.PAYTM, AppAdController.packageMap.get(Website.PAYTM), "http://www.9apps.com/android-apps/Recharge-Shop-and-Wallet-Paytm/", WebsiteHelper.getLogoUrl(Website.PAYTM), "Best Mobile Recharge and DTH Recharge, Bill Payment and Shipping Experience", 4.3f, "1,401,209", "10,000,000 - 50,000,000", "13.0MB");
                ThirdAppVo nineApp_Snapdeal = new ThirdAppVo(Website.SNAPDEAL, AppAdController.packageMap.get(Website.SNAPDEAL), "http://www.9apps.com/android-apps/Snapdeal-Online-Shopping-India/", WebsiteHelper.getLogoUrl(Website.SNAPDEAL), "Best deals on women & men's fashion,home essentials,electronics & gadgets!", 4.1f, "1,035,900", "10,000,000 - 50,000,000", "12.0MB");
                ThirdAppVo nineApp_Jabong = new ThirdAppVo(Website.JABONG, AppAdController.packageMap.get(Website.JABONG), "http://www.9apps.com/android-apps/Jabong-Online-Fashion-Shopping/", WebsiteHelper.getLogoUrl(Website.JABONG), "India's Best Online Shopping App To Buy Latest Fashion for Men,Women,Kids", 3.9f, "171,487", "10,000,000 - 50,000,000", "6.1MB");
                ThirdAppVo nineApp_VOONIK = new ThirdAppVo(Website.VOONIK, AppAdController.packageMap.get(Website.VOONIK), "http://www.9apps.com/android-apps/Voonik-Shopping-App-For-Women/", WebsiteHelper.getLogoUrl(Website.VOONIK), "Online Shopping for women clothing,ethnic wear,sarees,kurtis,lingere in India", 4.2f, "129,079", "5,000,000 - 10,000,000", "5.8MB");
                ThirdAppVo nineApp_INFIBEAM = new ThirdAppVo(Website.INFIBEAM, AppAdController.packageMap.get(Website.INFIBEAM), "http://www.9apps.com/android-apps/Infibeam-Online-Shopping-App/", WebsiteHelper.getLogoUrl(Website.INFIBEAM), "Infibeam.com-Buy Mobiles,Electronics,Books,Gifts,Clothes & more", 3.7f, "8,424", "1,000,000 - 5,000,000", "26.2MB");
                ThirdAppVo nineApp_Myntra = new ThirdAppVo(Website.MYNTRA, AppAdController.packageMap.get(Website.MYNTRA), "http://www.9apps.com/android-apps/Myntra-Fashion-Shopping-App/", WebsiteHelper.getLogoUrl(Website.MYNTRA), "Online shopping for fashion clothes,footwear,accessories for Men,Women & Kids", 4.1f, "509,053", "10,000,000 - 50,000,000", "17.2MB");

                tempNINEAPP.addAll(Arrays.asList(nineApp_Amazon, nineApp_Flipkart, nineApp_ShopClues, nineApp_eBay, nineApp_Paytm, nineApp_Snapdeal, nineApp_Jabong, nineApp_VOONIK, nineApp_INFIBEAM, nineApp_Myntra));
                NINEAPP.put("NINEAPP", tempNINEAPP);
                apps.add(NINEAPP);
                apps.add(GOOGLEPLAY);
                DownloadConfigVo downloadConfigVo = new DownloadConfigVo(true,
                        Arrays.asList("com.snapdeal.main", "com.flipkart.android",
                                "in.amazon.mShop.android.shopping", "net.one97.paytm",
                                "com.ebay.mobile", "com.shopclues", "com.infibeam.infibeamapp",
                                "com.myntra.android", "com.jabong.android",
                                "com.alibaba.aliexpresshd"),
                        "NINEAPP",
                        apps,
                        Arrays.asList("com.voonik.android",
                                "cn.xender", "com.india.hasoffer",
                                "com.lenovo.anyshare,gps", "com.mobile.indiapp",
                                "com.leo.appmaster", "com.voodoo.android",
                                "com.app.buyhatke", "com.makemytrip",
                                "com.goibibo", "com.cleartrip.android",
                                "com.yatra.base", "com.android.contacts"
                        ));
                modelAndView.addObject("data", downloadConfigVo);
                break;
            case COMADD:
                Map nMap = new HashMap();
                //如果版本是28就放开,否则关闭
                String appVersion = deviceInfoVo.getAppVersion();
                if (StringUtils.isNotBlank(appVersion) && Integer.valueOf(appVersion) >= 28) {
                    nMap.put("op", true);
                } else {
                    nMap.put("op", false);
                }
                modelAndView.addObject("data", nMap);
            default:
                break;
        }
        return modelAndView;
    }

    private ModelAndView getDealInfoMethod(@RequestParam String id, ModelAndView mv) {
        Map map = new HashMap();
        DeviceInfoVo deviceInfoVo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        if (deviceInfoVo != null) {
            String appVersion = deviceInfoVo.getAppVersion();
            if (!StringUtils.isEmpty(appVersion)) {
                //暂时只是去除空格,未来要加上正则匹配,希望根林不要坑我...
                appVersion = appVersion.trim();
                Integer vsion = Integer.valueOf(appVersion);
                if (StringUtils.isEmpty(id)) {
                    //空,完毕
                    mv.addObject("data", null);
                    return mv;
                } else {
                    Long dealId = Long.valueOf(id);
                    AppDeal appDeal = appService.getDealDetail(dealId);
                    if (appDeal != null) {
                        //获取点赞数和评论数以及该用户的点赞和点踩状态
                        getDealThuAndComNums(appDeal.getId(), map);
                        if (vsion < 23) {
                            map.put("image", appDeal.getInfoPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getInfoPageImage()));
                            map.put("title", appDeal.getTitle());
                            map.put("website", appDeal.getWebsite() == Website.UNKNOWN ? WebsiteHelper.getAllWebSiteString(appDeal.getLinkUrl()) : appDeal.getWebsite().name());
                            map.put("exp", new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH).format(appDeal.getExpireTime()));
                            map.put("logoUrl", appDeal.getWebsite() == null ? "" : WebsiteHelper.getLogoUrl(appDeal.getWebsite()));
                            getModel1Deal(map, appDeal);
                        } else {
                            map.put("discount", appDeal.getDiscount());
                            map.put("originPrice", appDeal.getOriginPrice() == null ? 0 : appDeal.getOriginPrice());
                            map.put("priceDescription", appDeal.getPriceDescription() == null ? "" : appDeal.getPriceDescription());
                            map.put("image", appDeal.getInfoPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getInfoPageImage()));
                            map.put("title", appDeal.getTitle());
                            //返回deal的处境时间距离现在时间的时间,多少天,小时,分钟..
                            map.put("createTime", TimeUtils.getDifference2Date(new Date(), appDeal.getCreateTime()));
                            map.put("website", appDeal.getWebsite() == Website.UNKNOWN ? WebsiteHelper.getAllWebSiteString(appDeal.getLinkUrl()) : appDeal.getWebsite().name());
                            //降价生成deal无失效日期
                            if (!appDeal.getAppdealSource().name().equals("PRICE_OFF")) {
                                map.put("exp", new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH).format(appDeal.getExpireTime()));
                            } else {
                                //是降价生成的deal，失效时间设置创建时间七天后
                                Date createTime = appDeal.getCreateTime();
                                createTime.setTime(createTime.getTime() + 1000 * 60 * 60 * 24 * 7);
                                map.put("exp", new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH).format(createTime));
                            }
                            map.put("logoUrl", appDeal.getWebsite() == null ? "" : WebsiteHelper.getLogoUrl(appDeal.getWebsite()));

                            //要判断deal的类型,手动导入和降价生成
                            if (appDeal.getAppdealSource().name().equals("PRICE_OFF")) {
                                //降价生成
                                StringBuilder sb = new StringBuilder();
                                String description = appDeal.getDescription();
                                //网站名 is offering 商品名 for Rs.现价.
                                //当支持货到付款时展示 : Cash On Delivery is available
                                sb.append(appDeal.getWebsite().name()).append(" is offering ").append(appDeal.getTitle()).append(" for ").append(appDeal.getPriceDescription()).append(".");
                                //是否支持COD
                                PtmCmpSku cmpSkuById = cmpSkuCacheManager.getCmpSkuById(appDeal.getPtmcmpskuid());
                                if (cmpSkuById != null) {
                                    //如果存在此sku
                                    String supportPayMethod = cmpSkuById.getSupportPayMethod();
                                    if (!StringUtils.isBlank(supportPayMethod) && supportPayMethod.contains("COD")) {
                                        sb.append("Cash On Delivery is available.");
                                    }
                                }
                                //描述拼接完成
                                map.put("description", sb.toString());
                                //拼接Price Research
                                if (StringUtils.isNotBlank(description)) {
                                    //如果描述不为空,拼接描述然后换行,空行
                                    sb = new StringBuilder();
                                    sb.append(description).append("\n\n");
                                }
                                if (appDeal.getPtmcmpskuid() > 0) {
                                    //如果存在skuId,将skuId返回
                                    map.put("skuId", appDeal.getPtmcmpskuid());
                                    PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, appDeal.getPtmcmpskuid());
                                    if (ptmCmpSkuDescription != null) {
                                        List<FetchedProductReview> fetchedProductReviewList = ptmCmpSkuDescription.getFetchedProductReviewList();
                                        if (fetchedProductReviewList != null && fetchedProductReviewList.size() > 0) {
                                            List<String> commentList = new ArrayList<>();
                                            for (FetchedProductReview fec : fetchedProductReviewList) {
                                                String reviewContent = fec.getReviewContent();
                                                if (!StringUtils.isEmpty(reviewContent)) {
                                                    reviewContent = ClientHelper.delHTMLTag(reviewContent);
                                                    //处理下换行符号
                                                    String replaceResult = reviewContent.replaceAll("\n", "");
                                                    reviewContent = replaceResult;
                                                }
                                                if (!StringUtils.isEmpty(reviewContent)) {
                                                    if (commentList.size() < 4) {
                                                        //拼接评论标题
                                                        String reviewTitle = fec.getReviewTitle();
                                                        if (!StringUtils.isEmpty(reviewTitle)) {
                                                            reviewTitle = ClientHelper.delHTMLTag(reviewTitle);
                                                            //处理下换行符号
                                                            String replaceResult = reviewTitle.replaceAll("\n", "");
                                                            reviewTitle = replaceResult;
                                                            commentList.add(reviewTitle == null ? "" : reviewTitle + "." + reviewContent);
                                                        } else {
                                                            commentList.add(reviewContent);
                                                        }
                                                    }
                                                }
                                            }
                                            map.put("comments", commentList);
                                        }
                                        //查看是否存在offer,如果存在将offer拼接
                                        //“网站名” also provides “SKU当前生效的offer数量“ extra offer（offer数量为1时 展示offer 大于1时 展示offers）: “按服务端排序展示offer列表 以分号间隔”
                                        String offers = ptmCmpSkuDescription.getOffers();
                                        if (!hasoffer.base.utils.StringUtils.isEmpty(offers)) {
                                            String[] temps = offers.split(",");
                                            if (temps.length >= 1) {
                                                sb.append(appDeal.getWebsite().name()).append(" also provides ").append(temps.length).append(" extra offer :");
                                            }
                                            for (String str : temps) {
                                                sb.append(str).append(";");
                                            }
                                            //拼完之后换行,空一行
                                            sb.append("\n\n");
                                            sb.append("Please note: offers and price may vary by location.");
                                        }
                                        //设置Key Features
                                        String jsonParam = ptmCmpSkuDescription.getJsonParam();
                                        if (StringUtils.isNotBlank(jsonParam)) {
                                            Map jsonMap = JsonHelper.getJsonMap(jsonParam);
                                            map.put("KeyFeatures", jsonMap);
                                        }

                                    }
                                    map.put("priceResearch", sb.toString());
                                    Map priceCurveDesc = new HashMap();
                                    //配置点击弹出价格曲线的文字以及文字的颜色
                                    priceCurveDesc.put("clickableContent", "Click here to check price history.");
                                    priceCurveDesc.put("fontColor", "#108ee9");
                                    map.put("clickConfig", priceCurveDesc);
                                }
                            } else if (AppdealSource.DEAL_SITE.equals(appDeal.getAppdealSource())) {
                                getModel1Deal(map, appDeal);
                            } else {
                                //手动导入,描述要用老的方式
                                getModel1Deal(map, appDeal);
                            }
                        }
                        map.put("extra", 0);
                        if (appDeal.getWebsite() == Website.FLIPKART) {
                            map.put("extra", 7.5);
                            map.put("cashbackInfo", "1. Offer valid for a limited time only while stocks last\n" +
                                    "2. To earn Rewards, remember to visit retailer through Hasoffer & then place your order\n" +
                                    "3. Rewards may not paid on purchases made using store credits/gift vouchers\n" +
                                    "4. Rewards is not payable if you return any part of your order. Unfortunately even if you exchange any part of your order, Rewards for the full order will be Cancelled\n" +
                                    "5  Do not visit any other price comparison, coupon or deal site in between clicking-out from Hasoffer & ordering on retailer site.");
                        }
                        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
                        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                        System.out.println("link url is  :" + appDeal.getLinkUrl());
                        String s = appDeal.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(appDeal.getWebsite(), appDeal.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId});
                        logger.info(" dealInfo record deal deepLink :" + s);
                        map.put("deeplink", s);
                        mv.addObject("data", map);
                        return mv;
                    }
                }
            }
        }
        return mv;
    }

    private void getModel1Deal(Map map, AppDeal appDeal) {
        StringBuilder sb = new StringBuilder();
        String description = appDeal.getDescription();
        sb.append(description == null ? "" : description);
        if (description.lastIndexOf('\n') > 0) {
            if (description.lastIndexOf('\n') == description.length() - 1) {
                //最后有换行,再加一个换行
                sb.append("\n");
            } else {
                //最后无换行,加两个
                sb.append("\n");
                sb.append("\n");
            }
        } else {
            //无换行
            sb.append("\n");
            sb.append("\n");
        }
        if (appDeal.getPtmcmpskuid() > 0) {
            PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, appDeal.getPtmcmpskuid());
            if (ptmCmpSkuDescription != null) {
                String jsonParam = ptmCmpSkuDescription.getJsonParam();
                if (StringUtils.isNotBlank(jsonParam)) {
                    Map jsonMap = JsonHelper.getJsonMap(jsonParam);
                    if (jsonMap != null) {
                        //遍历map
                        Set<Map.Entry> set = jsonMap.entrySet();
                        Iterator<Map.Entry> iterator = set.iterator();
                        if (iterator.hasNext()) {
                            sb.append("Key Features: \n");
                        }
                        while (iterator.hasNext()) {
                            Map.Entry next = iterator.next();
                            sb.append(next.getKey()).append(" : ");
                            sb.append(next.getValue()).append("\n");
                        }
                    }

                }

            }
        }
        map.put("description", sb.toString());
    }

    private void setDeal(AppDeal appDeal, DealVo dealVo) {
        if (appDeal.getDealThumbNumber() == null) {
            appDeal.setDealThumbNumber(0);
        }
        dealVo.setId(appDeal.getId());
        dealVo.setType(appDeal.getWeight() >= 1 ? 1 : 0);
        dealVo.setImage(appDeal.getListPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getListPageImage()));
        dealVo.setExtra(0d);
        dealVo.setLogoUrl(appDeal.getWebsite() == null ? "" : WebsiteHelper.getLogoUrl(appDeal.getWebsite()));
        if (appDeal.getWebsite() != null && appDeal.getWebsite().name().equals("FLIPKART")) {
            dealVo.setExtra(7.5);
        }
        dealVo.setLogoUrl(WebsiteHelper.getLogoUrl(appDeal.getWebsite()));
        dealVo.setExp(appDeal.getExpireTime());
        dealVo.setTitle(appDeal.getTitle());
        dealVo.setCreateTime(TimeUtils.getDifference2Date(new Date(), appDeal.getCreateTime()));
        dealVo.setPresentPrice(appDeal.getPresentPrice() == null ? 0 : appDeal.getPresentPrice());
        dealVo.setDiscount(appDeal.getDiscount());
        dealVo.setOriginPrice(appDeal.getOriginPrice() == null ? 0 : appDeal.getOriginPrice());
        dealVo.setPriceDescription(appDeal.getPriceDescription() == null ? "" : appDeal.getPriceDescription());
        dealVo.setWebsite(appDeal.getWebsite() == Website.UNKNOWN ? WebsiteHelper.getAllWebSiteString(appDeal.getLinkUrl()) : appDeal.getWebsite().name());
        //计算总评论数
        //计算总点赞数
        Long totalDealThumb = dealService.getTotalDealThumb(appDeal.getId());
        int randomDealThumb = 0;
        if (appDeal.getDealThumbNumber() != null) {
            randomDealThumb = appDeal.getDealThumbNumber();
        }
        dealVo.setThumbNumber(totalDealThumb == null ? randomDealThumb : totalDealThumb + randomDealThumb);
        PageableResult<AppDealComment> dealComments = dealService.getPageAbleDealComment(appDeal.getId(), 1, 5);
        if (dealComments != null) {
            dealVo.setCommentNumber(dealComments.getNumFund());
        }
    }

    public void getDealThuAndComNums(Long dealId, Map map) {
        map.put("action", 0);
        map.put("commentNumber", 0);
        map.put("thumbNumber", 0);
        Long totalDealThumb = dealService.getTotalDealThumb(dealId);
        AppDeal dealDetail = appService.getDealDetail(dealId);
        int randomThumbNumber = 0;
        if (dealDetail != null) {
            Integer dealThumbNumber = dealDetail.getDealThumbNumber();
            if (dealThumbNumber != null) {
                randomThumbNumber = dealThumbNumber;
            }
        }
        if (totalDealThumb != null) {
            map.put("thumbNumber", totalDealThumb + randomThumbNumber);
        } else {
            map.put("thumbNumber", randomThumbNumber);
        }
        PageableResult<AppDealComment> dealComments = dealService.getPageAbleDealComment(dealId, 1, 5);
        if (dealComments != null) {
            map.put("commentNumber", dealComments.getNumFund());
        }
        //是否已点赞是否已点踩
        UrmUser urmUser;
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        if (StringUtils.isNotBlank(userToken)) {
            String key = "user_" + userToken;
            urmUser = userICacheService.get(UrmUser.class, key, 0);
            if (urmUser == null) {
                urmUser = appService.getUserByUserToken(userToken);
                if (urmUser != null) {
                    userICacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
                }
            }
            if (urmUser != null) {
                AppDealThumb appDealThumb = dealService.getDealThumbByUidDid(urmUser.getId(), dealId);
                if (appDealThumb != null) {
                    int action = appDealThumb.getAction();
                    if (action == -1) {
                        //踩
                        map.put("action", 2);
                    } else if (action == 1) {
                        //赞
                        map.put("action", 1);
                    }
                }
            }
        }
    }

    public void getDealsFromCache(List list, int page, int size) {
        String key = ConstantUtil.API_DEALS_ + page + "_" + +size;
        String dealsString = iCacheService.get(key, 0);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(dealsString)) {
            List<DealVo> dealVos = JSONArray.parseArray(dealsString, DealVo.class);
            if (dealVos != null && dealVos.size() > 0) {
                list.addAll(dealVos);
            }
        }
    }

    public void setDeals2Cache(List list, int page, int size) {
        String key = ConstantUtil.API_DEALS_ + page + "_" + size;
        iCacheService.add(key, JSONArray.toJSONString(list), TimeUtils.MILLISECONDS_OF_1_MINUTE * 5);
    }
}
