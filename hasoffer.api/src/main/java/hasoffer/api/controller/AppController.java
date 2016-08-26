package hasoffer.api.controller;

import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.ParseConfigHelper;
import hasoffer.api.worker.SearchLogQueue;
import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.bo.product.Banners;
import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.bo.push.*;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.cache.AppCacheManager;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.impl.ProductServiceImpl;
import hasoffer.core.product.solr.*;
import hasoffer.core.push.IPushService;
import hasoffer.core.system.IAppService;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.utils.AffliIdHelper;
import hasoffer.core.utils.ImageUtil;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import jodd.util.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    IAppService appService;
    @Resource
    IDeviceService deviceService;
    @Resource
    CmpSkuCacheManager cmpSkuCacheManager;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    ContentNegotiatingViewResolver jsonViewResolver;
    @Resource
    CategoryIndexServiceImpl categoryIndexService;
    @Resource
    ProductIndexServiceImpl productIndexServiceImpl;
    @Resource
    ProductIndex2ServiceImpl ProductIndex2ServiceImpl;
    @Resource
    ProductServiceImpl productService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    AppCacheManager appCacheManager;
    @Resource
    IPushService pushService;
    private Logger logger = LoggerFactory.logger(AppController.class);

    public static void main(String[] args) {
//        String ss = WebsiteHelper.getDealUrlWithAff(Website.SNAPDEAL, "http://www.snapdeal.com/product/micromax-canvas-a1-aq4502-8/630310793485", new String[]{"SHANCHUAN", "123"});
//        System.out.print(ss);

        //Random random = new Random();
        //for (int i = 0; i < 100; i++) {
        //   int nextInt = random.nextInt(8);
        //    System.out.println(nextInt);
        // }
        String dealUrlWithAff = WebsiteHelper.getDealUrlWithAff(Website.SHOPCLUES, "http://www.shopclues.com/reach-allure-speed.html", new String[]{MarketChannel.GOOGLEPLAY.name(), "asd123gfd654"});
        System.out.println(dealUrlWithAff);
        // String ss = WebsiteHelper.getDealUrlWithAff(Website.FLIPKART, "http://www.flipkart.com/philips-mix-4-gb-sa5mxx04wf-97-16-mp3-player/p/itmdmfndygbz3wfd?pid=AUDDMFMAC4WSSGGH&al=TQCV0eQ7m7uScf%2FCbjC3PcldugMWZuE7sHPMhtl4IOoHmf27YkMOEISwRAaogpJNxY67buiFvno%3D&offer=nb%3Amp%3A06e1fc0e26&ref=L%3A5882205368552411071&srno=b_1&findingMethod=Deals%20of%20the%20Day&otracker=hp_omu_Deals%20of%20the%20Day_1_39fdd0fe-e2e3-4176-9cf4-15ca32404fe5_0", new String[]{"GOOGLEPLAY", "aaaadfdfdfdf"});
        //System.out.println(ss);
        //System.out.println(WebsiteHelper.getUrlWithAff("http://dl.flipkart.com/dl/all/~intex-speakers/pr?sid=all&p%5B%5D=facets.filter_standard%255B%255D%3D1"));
        String ss = "<div id='mini_nav_qq'><li><a target='_top' " +
                "href='http:// lady.qq.com/emo/emotio.shtml'>情感</a></li><li>" +
                "<a target='_top' href='http://lady.qq.com/beauty/beauty.shtml'>美容</a></li></div>";
        String s = ClientHelper.delHTMLTag(ss);
        System.out.println(s);
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

//        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);

        AppConfigVo configVo = new AppConfigVo();
//        if (!StringUtils.isEmpty(deviceId)) {
//            UrmDeviceConfig deviceConfig = deviceService.getDeviceConfig(deviceId);
//            if (deviceConfig != null) {
//                configVo.setShowToast(deviceConfig.isShowToast());
////                configVo.setShowPrice(deviceConfig.isShowPrice());
//                configVo.setShowPrice(true);
//            }
//        }
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

    /**
     * 客户端回调
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    public ModelAndView callback(HttpServletRequest request,
                                 @RequestParam CallbackAction action) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        switch (action) {
            case FLOWCTRLSUCCESS:
                // 流量拦截成功
                try {
                    String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
                    DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                    cmpSkuCacheManager.recordFlowControll(deviceId, deviceInfo.getCurShopApp());
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
                break;
            case HOMEPAGE:
                Map map = new HashMap();
                map.put("info", AffliIdHelper.getAffiIds());
                modelAndView.addObject("data", map);
                break;
            case CLICKDEAL:
                AppDeal appDeal = appService.getDealDetail(request.getParameter("id"));
                if (appDeal != null) {
                    appService.countDealClickCount(appDeal);
                }
                break;
            case DOWNLOADBOOTCONFIG:
                //app下载引导
                List<Map<String, List<ThirdAppVo>>> apps = new ArrayList<Map<String, List<ThirdAppVo>>>();
                Map<String, List<ThirdAppVo>> nine = new HashMap<>();
                Map<String, List<ThirdAppVo>> google = new HashMap<>();

                //添加GooglePlay渠道的app下载属性
                List<ThirdAppVo> googlePlayApps = new ArrayList<ThirdAppVo>();
                ThirdAppVo googlePlayApps_Amazon = new ThirdAppVo(Website.AMAZON, "https://play.google.com/store/apps/details?id=com.amazon.mShop.android.shopping", WebsiteHelper.getBiggerLogoUrl(Website.AMAZON), "SBrowse,search & buy millions of products right from your Android device", 4.3f, "491,637", "50,000,000 - 100,000,000");
                ThirdAppVo googlePlayApps_Flipkart = new ThirdAppVo(Website.FLIPKART, "https://play.google.com/store/apps/details?id=com.flipkart.android", WebsiteHelper.getBiggerLogoUrl(Website.FLIPKART), "Shop for electronics,apparels & more using our Flipart app Free shipping & COD", 4.2f, "2,044,978", "50,000,000 - 100,000,000");
                ThirdAppVo googlePlayApps_ShopClues = new ThirdAppVo(Website.SHOPCLUES, "https://play.google.com/store/apps/details?id=com.shopclues", WebsiteHelper.getBiggerLogoUrl(Website.SHOPCLUES), "India's largest Online Marketplace is now in your Pocket - Install,Shop,Enjoy!", 3.9f, "235,468", "10,000,000 - 50,000,000");
                ThirdAppVo googlePlayApps_eBay = new ThirdAppVo(Website.EBAY, "https://play.google.com/store/apps/details?id=com.ebay.mobile", WebsiteHelper.getBiggerLogoUrl(Website.EBAY), "Buy,bid & sell! Deals & Discounts to Save Money on Home,Collectables & Cars", 4.2f, "1,759,547", "100,000,000 - 500,000,000");
                ThirdAppVo googlePlayApps_Paytm = new ThirdAppVo(Website.PAYTM, "https://play.google.com/store/apps/details?id=net.one97.paytm", WebsiteHelper.getBiggerLogoUrl(Website.PAYTM), "Best Mobile Recharge and DTH Recharge, Bill Payment and Shipping Experience", 4.3f, "1,401,209", "10,000,000 - 50,000,000");
                ThirdAppVo googlePlayApps_Snapdeal = new ThirdAppVo(Website.SNAPDEAL, "https://play.google.com/store/apps/details?id=com.snapdeal.main", WebsiteHelper.getBiggerLogoUrl(Website.SNAPDEAL), "Best deals on women & men's fashion,home essentials,electronics & gadgets!", 4.1f, "1,035,900", "10,000,000 - 50,000,000");
                ThirdAppVo googlePlayApps_Jabong = new ThirdAppVo(Website.JABONG, "https://play.google.com/store/apps/details?id=com.jabong.android", WebsiteHelper.getBiggerLogoUrl(Website.JABONG), "India's Best Online Shopping App To Buy Latest Fashion for Men,Women,Kids", 3.9f, "171,487", "10,000,000 - 50,000,000");
                ThirdAppVo googlePlayApps_VOONIK = new ThirdAppVo(Website.VOONIK, "https://play.google.com/store/apps/details?id=com.voonik.android", WebsiteHelper.getBiggerLogoUrl(Website.VOONIK), "Online Shopping for women clothing,ethnic wear,sarees,kurtis,lingere in India", 4.2f, "129,079", "5,000,000 - 10,000,000");
                ThirdAppVo googlePlayApps_INFIBEAM = new ThirdAppVo(Website.INFIBEAM, "https://play.google.com/store/apps/details?id=com.infibeam.infibeamapp", WebsiteHelper.getBiggerLogoUrl(Website.INFIBEAM), "Infibeam.com-Buy Mobiles,Electronics,Books,Gifts,Clothes & more", 3.7f, "8,424", "1,000,000 - 5,000,000");
                ThirdAppVo googlePlayApps_Myntra = new ThirdAppVo(Website.MYNTRA, "https://play.google.com/store/apps/details?id=com.myntra.android&hl=en", WebsiteHelper.getBiggerLogoUrl(Website.MYNTRA), "Online shopping for fashion clothes,footwear,accessories for Men,Women & Kids", 4.1f, "509,053", "10,000,000 - 50,000,000");

                googlePlayApps.addAll(Arrays.asList(googlePlayApps_Amazon, googlePlayApps_Flipkart, googlePlayApps_ShopClues, googlePlayApps_eBay, googlePlayApps_Paytm, googlePlayApps_Snapdeal, googlePlayApps_Jabong, googlePlayApps_VOONIK, googlePlayApps_INFIBEAM, googlePlayApps_Myntra));
                google.put("GOOGLEPLAY", googlePlayApps);

                //添加9APP渠道的app下载属性
                List<ThirdAppVo> nineApp = new ArrayList<ThirdAppVo>();
                ThirdAppVo nineApp_Amazon = new ThirdAppVo(Website.AMAZON, "http://www.9apps.com/android-apps/Amazon-India-Shopping/", WebsiteHelper.getBiggerLogoUrl(Website.AMAZON), "SBrowse,search & buy millions of products right from your Android device", 4.3f, "491,637", "50,000,000 - 100,000,000");
                ThirdAppVo nineApp_Flipkart = new ThirdAppVo(Website.FLIPKART, "http://www.9apps.com/android-apps/Flipkart-Amazing-Discounts-Everyday/", WebsiteHelper.getBiggerLogoUrl(Website.FLIPKART), "Shop for electronics,apparels & more using our Flipart app Free shipping & COD", 4.2f, "2,044,978", "50,000,000 - 100,000,000");
                ThirdAppVo nineApp_ShopClues = new ThirdAppVo(Website.SHOPCLUES, "http://www.9apps.com/android-apps/ShopClues/", WebsiteHelper.getBiggerLogoUrl(Website.SHOPCLUES), "India's largest Online Marketplace is now in your Pocket - Install,Shop,Enjoy!", 3.9f, "235,468", "10,000,000 - 50,000,000");
                ThirdAppVo nineApp_eBay = new ThirdAppVo(Website.EBAY, "http://www.9apps.com/android-apps/eBay/", WebsiteHelper.getBiggerLogoUrl(Website.EBAY), "Buy,bid & sell! Deals & Discounts to Save Money on Home,Collectables & Cars", 4.2f, "1,759,547", "100,000,000 - 500,000,000");
                ThirdAppVo nineApp_Paytm = new ThirdAppVo(Website.PAYTM, "http://www.9apps.com/android-apps/Recharge-Shop-and-Wallet-Paytm/", WebsiteHelper.getBiggerLogoUrl(Website.PAYTM), "Best Mobile Recharge and DTH Recharge, Bill Payment and Shipping Experience", 4.3f, "1,401,209", "10,000,000 - 50,000,000");
                ThirdAppVo nineApp_Snapdeal = new ThirdAppVo(Website.SNAPDEAL, "http://www.9apps.com/android-apps/Snapdeal-Online-Shopping-India/", WebsiteHelper.getBiggerLogoUrl(Website.SNAPDEAL), "Best deals on women & men's fashion,home essentials,electronics & gadgets!", 4.1f, "1,035,900", "10,000,000 - 50,000,000");
                ThirdAppVo nineApp_Jabong = new ThirdAppVo(Website.JABONG, "http://www.9apps.com/android-apps/Jabong-Online-Fashion-Shopping/", WebsiteHelper.getBiggerLogoUrl(Website.JABONG), "India's Best Online Shopping App To Buy Latest Fashion for Men,Women,Kids", 3.9f, "171,487", "10,000,000 - 50,000,000");
                ThirdAppVo nineApp_VOONIK = new ThirdAppVo(Website.VOONIK, "http://www.9apps.com/android-apps/Voonik-Shopping-App-For-Women/", WebsiteHelper.getBiggerLogoUrl(Website.VOONIK), "Online Shopping for women clothing,ethnic wear,sarees,kurtis,lingere in India", 4.2f, "129,079", "5,000,000 - 10,000,000");
                ThirdAppVo nineApp_INFIBEAM = new ThirdAppVo(Website.INFIBEAM, "http://www.9apps.com/android-apps/Infibeam-Online-Shopping-App/", WebsiteHelper.getBiggerLogoUrl(Website.INFIBEAM), "Infibeam.com-Buy Mobiles,Electronics,Books,Gifts,Clothes & more", 3.7f, "8,424", "1,000,000 - 5,000,000");
                ThirdAppVo nineApp_Myntra = new ThirdAppVo(Website.MYNTRA, "http://www.9apps.com/android-apps/Myntra-Fashion-Shopping-App/", WebsiteHelper.getBiggerLogoUrl(Website.MYNTRA), "Online shopping for fashion clothes,footwear,accessories for Men,Women & Kids", 4.1f, "509,053", "10,000,000 - 50,000,000");

                nineApp.addAll(Arrays.asList(nineApp_Amazon, nineApp_Flipkart, nineApp_ShopClues, nineApp_eBay, nineApp_Paytm, nineApp_Snapdeal, nineApp_Jabong, nineApp_VOONIK, nineApp_INFIBEAM, nineApp_Myntra));
                nine.put("9APP", nineApp);
                apps.add(nine);
                apps.add(google);
                DownloadConfigVo downloadConfigVo = new DownloadConfigVo(true, Arrays.asList("com.snapdeal.main", "com.flipkart.android", "in.amazon.mShop.android.shopping", "net.one97.paytm", "com.ebay.mobile", "com.shopclues", "com.infibeam.infibeamapp", "com.myntra.android", "com.jabong.android", "com.voonik.android", "cn.xender", "com.india.hasoffer", "com.lenovo.anyshare,gps", "com.mobile.indiapp", "com.leo.appmaster", "com.voodoo.android", "com.app.buyhatke"), "9APP", Arrays.asList(new DownLoadConfigChannle("GOOGLEPLAY", "https://www.googleplay.com"), new DownLoadConfigChannle("9APP", "https://www.9APP.com")), apps);
                modelAndView.addObject("data", downloadConfigVo);
                break;
            default:
                break;
        }
        return modelAndView;
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

    /**
     * 查看返利
     *
     * @return
     */
    @RequestMapping(value = "/backDetail", method = RequestMethod.GET)
    public ModelAndView backDetail() {
        ModelAndView mv = new ModelAndView();
        BackDetailVo data = new BackDetailVo();
        List<OrderVo> transcations = new ArrayList<OrderVo>();
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        UrmUser user = appService.getUserByUserToken(userToken);
        BigDecimal PendingCoins = BigDecimal.ZERO;
        BigDecimal VericiedCoins = BigDecimal.ZERO;
        if (user != null) {
            List<OrderStatsAnalysisPO> orders = appService.getBackDetails(user.getId().toString());
            for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
                if (orderStatsAnalysisPO.getWebSite().equals(Website.FLIPKART.name())) {
                    OrderVo orderVo = new OrderVo();
                    BigDecimal tempPrice = orderStatsAnalysisPO.getSaleAmount().multiply(BigDecimal.valueOf(0.015)).min(orderStatsAnalysisPO.getTentativeAmount());
                    orderVo.setAccount(tempPrice.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
                    orderVo.setChannel(orderStatsAnalysisPO.getChannel());
                    orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
                    orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
                    orderVo.setWebsite(orderStatsAnalysisPO.getWebSite());
                    //返利比率=tentativeAmount*rate/SaleAmount
                    orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
                    transcations.add(orderVo);
                    if (orderStatsAnalysisPO.getOrderStatus() != "cancelled" && orderStatsAnalysisPO.getOrderStatus() != "disapproved") {
                        PendingCoins = PendingCoins.add(tempPrice);
                    }
                    if (orderStatsAnalysisPO.getOrderStatus().equals("approved")) {
                        VericiedCoins = VericiedCoins.add(tempPrice);
                    }
                }
            }
        }
        //待定的
        data.setPendingCoins(PendingCoins.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        //可以使用的
        data.setVericiedCoins(VericiedCoins.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        data.setTranscations(transcations);
        mv.addObject("data", data);
        return mv;
    }

    /**
     * 订单详情
     *
     * @param orderId
     * @return
     */
    /*@RequestMapping(value = "/orderDetail", method = RequestMethod.GET)
    public ModelAndView orderDetail(@RequestParam String orderId) {
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        ModelAndView mv = new ModelAndView();
        UrmUser user = appService.getUserByUserToken(userToken);
        OrderStatsAnalysisPO orderStatsAnalysisPO = appService.getOrderDetail(orderId, user.getId().toString());
        if (orderStatsAnalysisPO != null) {
            OrderVo orderVo = new OrderVo();
            orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
            orderVo.setRate(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.015)).divide(orderStatsAnalysisPO.getSaleAmount(), 2, BigDecimal.ROUND_HALF_UP));
            orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
            orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
            orderVo.setChannel(orderStatsAnalysisPO.getChannel());
            orderVo.setTotal(orderStatsAnalysisPO.getSaleAmount());
            orderVo.setAccount(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.015)));
            mv.addObject("data", orderVo);
        }
        return mv;
    }*/

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
            banner.setLink(appBanner.getLinkUrl() == null ? "" : appBanner.getLinkUrl());
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
    @RequestMapping(value = "/deals", method = RequestMethod.GET)
    public ModelAndView deals(@RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue = "20") String pageSize) {
        ModelAndView mv = new ModelAndView();
        PageableResult Result = appService.getDeals(Long.valueOf(page), Long.valueOf(pageSize));
        Map map = new HashMap();
        List li = new ArrayList();
        List<AppDeal> deals = Result.getData();
        for (AppDeal appDeal : deals) {
            DealVo dealVo = new DealVo();
            dealVo.setId(appDeal.getId());
            dealVo.setImage(appDeal.getListPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getListPageImage()));
            String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
            DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
            dealVo.setLink(appDeal.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(appDeal.getWebsite(), appDeal.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId}));
            dealVo.setExtra(0d);
            dealVo.setLogoUrl(appDeal.getWebsite() == null ? "" : WebsiteHelper.getLogoUrl(appDeal.getWebsite()));
            if (appDeal.getWebsite().name().equals("FLIPKART")) {
                dealVo.setExtra(1.5);
            }
            dealVo.setLogoUrl(WebsiteHelper.getLogoUrl(appDeal.getWebsite()));
            dealVo.setExp(appDeal.getExpireTime());
            dealVo.setTitle(appDeal.getTitle());
            dealVo.setPriceDescription(appDeal.getPriceDescription() == null ? "" : appDeal.getPriceDescription());
            dealVo.setWebsite(appDeal.getWebsite());
            li.add(dealVo);
        }
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
    @RequestMapping(value = "/dealInfo", method = RequestMethod.GET)
    public ModelAndView dealInfo(@RequestParam String id) {
        AppDeal appDeal = appService.getDealDetail(id);
        ModelAndView mv = new ModelAndView();
        if (appDeal != null) {
            Map map = new HashMap();
            map.put("image", appDeal.getInfoPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getInfoPageImage()));
            map.put("title", appDeal.getTitle());
            map.put("website", appDeal.getWebsite());
            map.put("exp", new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH).format(appDeal.getExpireTime()));
            map.put("logoUrl", appDeal.getWebsite() == null ? "" : WebsiteHelper.getLogoUrl(appDeal.getWebsite()));
            map.put("description", appDeal.getDescription() == null ? "" : appDeal.getDescription());
            map.put("extra", 0);
            if (appDeal.getWebsite() == Website.FLIPKART) {
                map.put("extra", 1.5);
                map.put("cashbackInfo", "1. Offer valid for a limited time only while stocks last\n" +
                        "2. To earn Rewards, remember to visit retailer through Hasoffer & then place your order\n" +
                        "3. Rewards may not paid on purchases made using store credits/gift vouchers\n" +
                        "4. Rewards is not payable if you return any part of your order. Unfortunately even if you exchange any part of your order, Rewards for the full order will be Cancelled\n" +
                        "5  Do not visit any other price comparison, coupon or deal site in between clicking-out from Hasoffer & ordering on retailer site.");
            }
            String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
            DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
            map.put("deeplink", appDeal.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(appDeal.getWebsite(), appDeal.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId}));
            mv.addObject("data", map);
        }
        return mv;
    }

    /**
     * 用户信息绑定
     *
     * @return
     */
    @RequestMapping(value = "/bindUserInfo", method = RequestMethod.POST)
    public ModelAndView bindUserInfo(UserVo userVO, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        Map map = new HashMap();
        String userToken = UUID.randomUUID().toString();
        UrmUser uUser = appService.getUserById(userVO.getThirdId() == null ? "-" : userVO.getThirdId());
        if (uUser == null) {
            logger.debug("user is not exist before");
            UrmUser urmUser = new UrmUser();
            urmUser.setUserToken(userToken);
            urmUser.setAvatarPath(userVO.getUserIcon());
            urmUser.setCreateTime(new Date());
            urmUser.setTelephone(userVO.getTelephone() == null ? "" : userVO.getTelephone());
            urmUser.setThirdPlatform(userVO.getPlatform());
            urmUser.setThirdToken(userVO.getToken());
            urmUser.setUserName(userVO.getUserName());
            urmUser.setThirdId(userVO.getThirdId());
            int result = appService.addUser(urmUser);
            logger.debug("add user result is :" + result);
        } else {
            logger.debug("user exist ,update userInfo");
            uUser.setUserName(userVO.getUserName());
            uUser.setThirdPlatform(userVO.getPlatform());
            uUser.setTelephone(uUser.getTelephone());
            uUser.setAvatarPath(uUser.getAvatarPath());
            uUser.setThirdToken(uUser.getThirdToken());
            uUser.setUserToken(userToken);
            appService.updateUserInfo(uUser);
            logger.debug("update userInfo over ");
        }
        map.put("userToken", userToken);
        mv.addObject("data", map);
        return mv;
    }

    /**
     * 用户信息获取
     *
     * @return
     */
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public ModelAndView userInfo() {
        ModelAndView mv = new ModelAndView();
        BigDecimal PendingCoins = BigDecimal.ZERO;
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        UrmUser user = appService.getUserByUserToken(userToken);
        if (user != null) {
            UserVo userVo = new UserVo();
            userVo.setName(user.getUserName());
            List<OrderStatsAnalysisPO> orders = appService.getBackDetails(user.getId().toString());
            for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
                if (orderStatsAnalysisPO.getOrderStatus() != "cancelled") {
                    PendingCoins = PendingCoins.add(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.03)));
                }
            }
            PendingCoins = PendingCoins.setScale(2, BigDecimal.ROUND_HALF_UP);
            userVo.setConis(PendingCoins);
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
    @RequestMapping(value = "/productsList")
    public ModelAndView productsList(SearchCriteria criteria, @RequestParam(defaultValue = "4") int type) {
        ModelAndView mv = new ModelAndView();
        List li = new ArrayList();
        Map map = new HashMap();
        PageableResult<ProductModel2> products;
        String data = "";
        //查询热卖商品
        List<PtmProduct> products2s = productCacheManager.getTopSellins(criteria.getPage(), criteria.getPageSize());
        switch (type) {
            case 0:
                addProductVo2List(li, products2s);
                if (products2s != null && products2s.size() > 4) {
                    li = li.subList(0, 5);
                }
                map.put("product", li);
                break;
            case 1:
                addProductVo2List(li, products2s);
                map.put("product", li);
                break;
            case 2:
                //search by title
                System.out.println("  sort " + criteria.getSort().name());
//                criteria.setPivotFields(Arrays.asList("cate2"));
                PageableResult p = ProductIndex2ServiceImpl.searchProducts(criteria);
                if (p != null && p.getData().size() > 0) {
                    System.out.println("getPivotFieldVals  " + p.getPivotFieldVals().size());
                    if (p.getPivotFieldVals() != null && p.getPivotFieldVals().size() > 0) {
                        // List<CategoryVo>
                        List<CategoryVo> categorys = new ArrayList();
                        Map pivotFieldVals = p.getPivotFieldVals();
                        Set<Map.Entry> set = pivotFieldVals.entrySet();
                        Iterator<Map.Entry> iterator = set.iterator();
                        while (iterator.hasNext()) {
                            Map.Entry next = iterator.next();
                            System.out.println("key " + next.getKey());
                            List<NameValue> nameValues = (List<NameValue>) next.getValue();
                            for (NameValue nameValue : nameValues) {
                                Long cateId = (Long) nameValue.getName();
                                PtmCategory ptmCategory = appCacheManager.getCategoryById(cateId);
                                if (ptmCategory != null) {
                                    CategoryVo categoryVo = new CategoryVo();
                                    categoryVo.setId(ptmCategory.getId());
                                    categoryVo.setLevel(ptmCategory.getLevel());
                                    categoryVo.setName(ptmCategory.getName());
                                    categoryVo.setCategorys(appCacheManager.getCategorys(cateId + ""));
                                    categorys.add(categoryVo);
                                }
                            }
                        }
                        map.put("categorys", categorys);
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
                    products = ProductIndex2ServiceImpl.searchPro(criteria);
                    if (products != null && products.getData().size() > 0) {
                        addProductVo2List(li, products.getData());
                    }
                }
                break;
            case 4:
                //如果是默认值,则判断类目id和level是否传递了,传了就是类目搜索,适配老接口
                if (StringUtils.isNotBlank(criteria.getCategoryId())) {
                    //search by category
                    products = ProductIndex2ServiceImpl.searchPro(criteria);
                    if (products != null && products.getData().size() > 0) {
                        addProductVo2List(li, products.getData());
                    }
                } else if (StringUtils.isNotBlank(criteria.getKeyword())) {
                    PageableResult pKeywordResult = ProductIndex2ServiceImpl.searchProducts(criteria);
                    if (pKeywordResult != null && pKeywordResult.getData().size() > 0) {
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
        return mv;
    }
//    public ModelAndView productsList(SearchCriteria criteria, @RequestParam(defaultValue = "3") int type) {
//        ModelAndView mv = new ModelAndView();
//        List li = new ArrayList();
//        Map map = new HashMap();
//        PageableResult<ProductModel> products;
//        //category level page size
//        // PageableResult <ProductModel> products=productIndexServiceImpl.searchPro(Long.valueOf(criteria.getCategoryId()),criteria.getLevel(),criteria.getPage(),criteria.getPageSize());
//        if (StringUtils.isNotBlank(criteria.getCategoryId())) {
//            //search by category
//            products = productIndexServiceImpl.searchPro(Long.valueOf(criteria.getCategoryId()), criteria.getLevel(), criteria.getPage(), criteria.getPageSize());
//            //products = productIndexServiceImpl.searchPro(Long.valueOf(2), 2, 1, 10);
//            if (products != null && products.getData().size() > 0) {
//                addProductVo2List(li, products.getData());
//            }
//        } else if (StringUtils.isNotEmpty(criteria.getKeyword())) {
//            //search by title
//            //productIndexServiceImpl.simpleSearch(criteria.getKeyword(),1,10);
//            PageableResult p = productIndexServiceImpl.searchProductsByKey(criteria.getKeyword(), criteria.getPage(), criteria.getPageSize());
//            if (p != null && p.getData().size() > 0) {
//                addProductVo2List(li, p.getData());
//            }
//        }
//        String data = "";
//        //查询热卖商品
//        List<PtmProduct> products2s = productCacheManager.getTopSellins(criteria.getPage(), criteria.getPageSize());
//        switch (type) {
//            case 0:
//                addProductVo2List(li, products2s);
//                if (products2s != null && products2s.size() > 4) {
//                    li = li.subList(0, 5);
//                }
//                map.put("product", li);
//                break;
//            case 1:
//                addProductVo2List(li, products2s);
//                map.put("product", li);
//                break;
//            case 2:
//                PageableResult p = productIndexServiceImpl.searchProductsByKey(criteria.getKeyword(), criteria.getPage(), criteria.getPageSize());
//                if (p != null && p.getData().size() > 0) {
//                    addProductVo2List(li, p.getData());
//                }
//                map.put("product", li);
//                break;
//            default:
//                map.put("product", null);
//        }
//        if (li != null && li.size() > 0) {
//            map.put("product", li);
//        }
//        mv.addObject("data", map);
//        return mv;
//    }


    public void addProductVo2List(List desList, List sourceList) {

        if (sourceList != null && sourceList.size() > 0) {
            if (ProductModel.class.isInstance(sourceList.get(0))) {
                Iterator<ProductModel> modelList = sourceList.iterator();
                while (modelList.hasNext()) {
                    ProductModel productModel = modelList.next();
                    int count = cmpSkuService.getSkuSoldStoreNum(productModel.getId());
                    if (count > 0) {
                        ProductListVo productListVo = new ProductListVo();
                        productListVo.setStoresNum(count);
                        productListVo.setId(productModel.getId());
                        setCommentNumAndRatins(productListVo);
                        productListVo.setImageUrl(productCacheManager.getProductMasterImageUrl(productModel.getId()));
                        productListVo.setName(productModel.getTitle());
                        productListVo.setPrice(Math.round(productModel.getPrice()));
                        desList.add(productListVo);
                    }
                }
            } else if (PtmProduct.class.isInstance(sourceList.get(0))) {
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
                    int count = cmpSkuService.getSkuSoldStoreNum(ptmProduct.getId());
                    if (count > 0) {
                        ProductListVo productListVo = new ProductListVo();
                        productListVo.setId(ptmProduct.getId());
                        productListVo.setImageUrl(productCacheManager.getProductMasterImageUrl(ptmProduct.getId()));
                        productListVo.setName(ptmProduct.getTitle());
                        productListVo.setPrice(Math.round(ptmProduct.getMinPrice()));
                        productListVo.setStoresNum(count);
                        setCommentNumAndRatins(productListVo);
                        desList.add(productListVo);
                    }
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
                    if (!ptmCmpSku2.getWebsite().equals(Website.EBAY)) {
                        System.out.println("not ebay ");
                        //评论数*星级 累加 除以评论数和
                        sum += ptmCmpSku2.getRatings() * ptmCmpSku2.getCommentsNumber();
                        //去除列表中除此之外的其他此site的数据
                        totalCommentNum += ptmCmpSku2.getCommentsNumber();
                    }
                }
            }
            System.out.println("totalCommentNum   " + totalCommentNum);
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

}
