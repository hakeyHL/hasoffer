package hasoffer.api.controller;

import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.ParseConfigHelper;
import hasoffer.api.worker.SearchLogQueue;
import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.bo.product.Banners;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.cache.AppCacheManager;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.iml.ProductServiceImpl;
import hasoffer.core.product.solr.CategoryIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel;
import hasoffer.core.system.IAppService;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
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
    ProductServiceImpl productService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    AppCacheManager appCacheManager;
    private Logger logger = LoggerFactory.logger(AppController.class);

    public static void main(String[] args) {
//        String ss = WebsiteHelper.getDealUrlWithAff(Website.SNAPDEAL, "http://www.snapdeal.com/product/micromax-canvas-a1-aq4502-8/630310793485", new String[]{"SHANCHUAN", "123"});
//        System.out.print(ss);

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int nextInt = random.nextInt(8);
            System.out.println(nextInt);
        }

        String ss = WebsiteHelper.getDealUrlWithAff(Website.FLIPKART, "http://www.flipkart.com/philips-mix-4-gb-sa5mxx04wf-97-16-mp3-player/p/itmdmfndygbz3wfd?pid=AUDDMFMAC4WSSGGH&al=TQCV0eQ7m7uScf%2FCbjC3PcldugMWZuE7sHPMhtl4IOoHmf27YkMOEISwRAaogpJNxY67buiFvno%3D&offer=nb%3Amp%3A06e1fc0e26&ref=L%3A5882205368552411071&srno=b_1&findingMethod=Deals%20of%20the%20Day&otracker=hp_omu_Deals%20of%20the%20Day_1_39fdd0fe-e2e3-4176-9cf4-15ca32404fe5_0", new String[]{"GOOGLEPLAY", "aaaadfdfdfdf"});
        System.out.println(ss);
        //System.out.println(WebsiteHelper.getUrlWithAff("http://dl.flipkart.com/dl/all/~intex-speakers/pr?sid=all&p%5B%5D=facets.filter_standard%255B%255D%3D1"));
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
                String[] FLIDS = new String[]{"xyangryrg", "zhouxixi0", "harveyouo", "allenooou", "747306881", "hlhakeygm", "oliviersl", "wuningSFg"};
                String[] SNIDS = new String[]{"89037", "104658", "104664", "104663", "104705", "104659", "104717", "104726"};
                String[] SHIDS = new String[]{"none", "none", "none", "none", "none", "none", "none", "none", "none", "none", "none", "none"};
                Map map = new HashMap();
                Random random = new Random();
                map.put("info", new StringBuilder().append(FLIDS[random.nextInt(FLIDS.length)] + ",").append(SNIDS[random.nextInt(SNIDS.length)] + ",").append(SHIDS[random.nextInt(SHIDS.length)]));
                modelAndView.addObject("errorCode", "00000");
                modelAndView.addObject("msg", "ok");
                modelAndView.addObject("data", map);
                break;
            case CLICKDEAL:
                AppDeal appDeal = appService.getDealDetail(request.getParameter("id"));
                if (appDeal != null) {

                    appService.countDealClickCount(appDeal);
                }
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
                    orderVo.setAccount(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.015)).divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
                    orderVo.setChannel(orderStatsAnalysisPO.getChannel());
                    orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
                    orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
                    orderVo.setWebsite(orderStatsAnalysisPO.getWebSite());
                    //返利比率=tentativeAmount*rate/SaleAmount
                    orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
                    transcations.add(orderVo);
                    BigDecimal tempPrice = orderStatsAnalysisPO.getSaleAmount().multiply(BigDecimal.valueOf(0.015)).min(orderStatsAnalysisPO.getTentativeAmount());
                    if (orderStatsAnalysisPO.getOrderStatus() != "cancelled") {
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
    @RequestMapping(value = "/productsList", method = RequestMethod.GET)
    public ModelAndView productsList(SearchCriteria criteria, @RequestParam(defaultValue = "3") int type) {
        ModelAndView mv = new ModelAndView();
        List li = new ArrayList();
        Map map = new HashMap();
        PageableResult<ProductModel> products;
        //category level page size
        // PageableResult <ProductModel> products=productIndexServiceImpl.searchPro(Long.valueOf(criteria.getCategoryId()),criteria.getLevel(),criteria.getPage(),criteria.getPageSize());
        if (StringUtils.isNotBlank(criteria.getCategoryId())) {
            //search by category
            products = productIndexServiceImpl.searchPro(Long.valueOf(criteria.getCategoryId()), criteria.getLevel(), criteria.getPage(), criteria.getPageSize());
            //products = productIndexServiceImpl.searchPro(Long.valueOf(2), 2, 1, 10);
            if (products != null && products.getData().size() > 0) {
                addProductVo2List(li, products.getData());
            }
        } else if (StringUtils.isNotEmpty(criteria.getKeyword())) {
            //search by title
            //productIndexServiceImpl.simpleSearch(criteria.getKeyword(),1,10);
            PageableResult p = productIndexServiceImpl.searchProductsByKey(criteria.getKeyword(), criteria.getPage(), criteria.getPageSize());
            if (p != null && p.getData().size() > 0) {
                addProductVo2List(li, p.getData());
            }
        }
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
                PageableResult p = productIndexServiceImpl.searchProductsByKey(criteria.getKeyword(), criteria.getPage(), criteria.getPageSize());
                if (p != null && p.getData().size() > 0) {
                    addProductVo2List(li, p.getData());
                }
                map.put("product", li);
                break;
            default:
                map.put("product", null);
        }
        if (li != null && li.size() > 0) {
            map.put("product", li);
        }
        mv.addObject("data", map);
        return mv;
    }

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
            }
        }
    }

    public void setCommentNumAndRatins(ProductListVo productListVo) {
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listPagedCmpSkus(productListVo.getId(), 1, 10);
        if (pagedCmpskus != null) {
            Long totalCommentNum = Long.valueOf(0);
            int totalRating = 0;
            for (PtmCmpSku ptmCmpSku : pagedCmpskus.getData()) {
                totalCommentNum += ptmCmpSku.getCommentsNumber();
                totalRating += ptmCmpSku.getRatings();
            }
            if (totalCommentNum == 0 || pagedCmpskus.getData().size() == 0 || totalRating == 0) {
                productListVo.setCommentNum(Long.valueOf(0));
                productListVo.setRatingNum(0);
            } else {
                productListVo.setCommentNum(totalCommentNum / Long.valueOf(pagedCmpskus.getData().size()));
                productListVo.setRatingNum(totalRating / pagedCmpskus.getData().size());
            }
        }
    }
}
