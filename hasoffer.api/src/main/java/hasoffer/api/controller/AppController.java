package hasoffer.api.controller;

import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.ParseConfigHelper;
import hasoffer.api.worker.SearchLogQueue;
import hasoffer.base.enums.AppType;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.bo.product.Banners;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.iml.ProductServiceImpl;
import hasoffer.core.product.solr.CategoryIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel;
import hasoffer.core.system.IAppService;
import hasoffer.core.user.IDeviceService;
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
    private Logger logger = LoggerFactory.logger(AppController.class);

    public static void main(String[] args) {
        BigDecimal ss = BigDecimal.valueOf(20);
        ss.divide(BigDecimal.valueOf(3));
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
            default:
                break;
        }

        return new ModelAndView();
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
        if (deviceInfoVo == null || deviceInfoVo.getAppType() == null) {
            appType = AppType.APP;
        } else {
            appType = deviceInfoVo.getAppType();
        }

        AppVersion appVersion = appService.getLatestVersion(appType);

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
     * @param userToken
     * @return
     */
    @RequestMapping(value = "/backDetail", method = RequestMethod.GET)
    public ModelAndView backDetail(@RequestParam String userToken) {
        ModelAndView mv = new ModelAndView();
        BackDetailVo data = new BackDetailVo();
        List<OrderVo> transcations = new ArrayList<OrderVo>();
        UrmUser user = appService.getUserByUserToken(userToken);
        BigDecimal PendingCoins = BigDecimal.ZERO;
        BigDecimal VericiedCoins = BigDecimal.ZERO;
        if (user != null) {
            List<OrderStatsAnalysisPO> orders = appService.getBackDetails(user.getId().toString());
            for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
                if (orderStatsAnalysisPO.getWebSite() == Website.SHOPCLUES.name() || orderStatsAnalysisPO.getWebSite() == Website.FLIPKART.name()) {
                    OrderVo orderVo = new OrderVo();
                    orderVo.setAccount(orderStatsAnalysisPO.getSaleAmount());
                    orderVo.setChannel(orderStatsAnalysisPO.getChannel());
                    orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
                    orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
                    //返利比率=tentativeAmount*rate/SaleAmount
                    orderVo.setRate(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.03)).divide(orderStatsAnalysisPO.getSaleAmount(), 2, BigDecimal.ROUND_HALF_UP));
                    orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
                    transcations.add(orderVo);
                    if (orderStatsAnalysisPO.getOrderStatus() != "cancelled") {
                        PendingCoins = PendingCoins.add(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.03)));
                    }
                    if (orderStatsAnalysisPO.getOrderStatus().equals("approved")) {
                        VericiedCoins = VericiedCoins.add(orderStatsAnalysisPO.getTentativeAmount());
                    }
                }
            }
        }

        //待定的
        data.setPendingCoins(PendingCoins);
        //可以使用的
        data.setVericiedCoins(VericiedCoins);

        data.setTranscations(transcations);
        mv.addObject("data", data);
        return mv;
    }

    /**
     * 订单详情
     *
     * @param orderId
     * @param userToken
     * @return
     */
    @RequestMapping(value = "/orderDetail", method = RequestMethod.GET)
    public ModelAndView orderDetail(@RequestParam String orderId, @RequestParam String userToken) {
        ModelAndView mv = new ModelAndView();
        UrmUser user = appService.getUserByUserToken(userToken);
        OrderStatsAnalysisPO orderStatsAnalysisPO = appService.getOrderDetail(orderId, user.getId().toString());
        if (orderStatsAnalysisPO != null) {
            OrderVo orderVo = new OrderVo();
            orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
            orderVo.setRate(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.03)).divide(orderStatsAnalysisPO.getSaleAmount(), 2, BigDecimal.ROUND_HALF_UP));
            orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
            orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
            orderVo.setChannel(orderStatsAnalysisPO.getChannel());
            orderVo.setTotal(orderStatsAnalysisPO.getSaleAmount());
            orderVo.setAccount(orderStatsAnalysisPO.getTentativeAmount().multiply(BigDecimal.valueOf(0.03)));
            mv.addObject("data", orderVo);
        }
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
            banner.setLink(appBanner.getLinkUrl());
            banner.setRank(appBanner.getRank());
            banner.setSource(1);
            banner.setSourceUrl(appBanner.getImageUrl());
            banner.setExpireDate(appBanner.getDeadline());
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
    public ModelAndView deals(String page, String pageSize) {
        ModelAndView mv = new ModelAndView();
        if (StringUtils.isEmpty(page)) {
            page = "0";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "20";
        }
        PageableResult Result = appService.getDeals(Long.valueOf(page), Long.valueOf(pageSize));
        Map map = new HashMap();
        List li = new ArrayList();
        List<AppDeal> deals = Result.getData();
        for (AppDeal appDeal : deals) {
            DealVo dealVo = new DealVo();
            dealVo.setId(appDeal.getId());
            dealVo.setExp(appDeal.getExpireTime());
            dealVo.setExtra(3.0);
            dealVo.setImage(appDeal.getImageUrl());
            dealVo.setLink(appDeal.getLinkUrl());
            dealVo.setTitle(appDeal.getTitle());
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
            map.put("image", appDeal.getImageUrl());
            map.put("title", appDeal.getTitle());
            map.put("exp", appDeal.getExpireTime());
            map.put("extra", 3.0);
            map.put("description", appDeal.getDescription());
            map.put("cashbackInfo", " hello ");
            map.put("deeplink", appDeal.getLinkUrl());
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
        UrmUser uUser = appService.getUserById(userVO.getThirdId() == null ? "0" : "1");
        if (uUser == null) {

            logger.debug("user is not exist before");
            UrmUser urmUser = new UrmUser();
            urmUser.setUserToken(userToken);
            urmUser.setAvatarPath(userVO.getUserIcon());
            urmUser.setCreateTime(new Date());
            urmUser.setNumber(userVO.getNumber());
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
            uUser.setNumber(uUser.getNumber());
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
    public ModelAndView userInfo(@RequestParam String userToken) {
        ModelAndView mv = new ModelAndView();
        BigDecimal PendingCoins = BigDecimal.ZERO;
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
        List<PtmCategory> ptmCategorys = null;
        List categorys = new ArrayList();
        if (StringUtils.isBlank(categoryId)) {
            ptmCategorys = appService.getCategory();
            for (PtmCategory ptmCategory : ptmCategorys) {
                CategoryVo categoryVo = new CategoryVo();
                categoryVo.setId(ptmCategory.getId());
                categoryVo.setHasChildren(ptmCategory.getParentId() == 0 ? 0 : 1);
                categoryVo.setImage(ptmCategory.getImageUrl());
                categoryVo.setLevel(ptmCategory.getLevel());
                categoryVo.setName(ptmCategory.getName());
                categoryVo.setParentId(ptmCategory.getParentId());
                categoryVo.setRank(ptmCategory.getRank());
                categorys.add(categoryVo);
            }
        } else {
            //get childs
            ptmCategorys = appService.getChildCategorys(categoryId);
            List childCategory = null;
            for (PtmCategory ptmCategory : ptmCategorys) {
                CategoryVo categoryVo = new CategoryVo();
                categoryVo.setId(ptmCategory.getId());
                categoryVo.setHasChildren(ptmCategory.getParentId() == 0 ? 0 : 1);
                categoryVo.setImage(ptmCategory.getImageUrl());
                categoryVo.setLevel(ptmCategory.getLevel());
                categoryVo.setName(ptmCategory.getName());
                categoryVo.setParentId(ptmCategory.getParentId());
                categoryVo.setRank(ptmCategory.getRank());
                List<PtmCategory> ptmCategorysTemp = appService.getChildCategorys(categoryVo.getId().toString());
                if (ptmCategorysTemp != null) {
                    categoryVo.setHasChildren(1);
                    childCategory = new ArrayList();
                    for (PtmCategory cates : ptmCategorysTemp) {
                        CategoryVo cate = new CategoryVo();
                        cate.setId(cates.getId());
                        cate.setHasChildren(0);
                        cate.setImage(cates.getImageUrl());
                        cate.setLevel(cates.getLevel());
                        cate.setName(cates.getName());
                        cate.setParentId(cates.getParentId());
                        cate.setRank(cates.getRank());
                        childCategory.add(cate);
                    }
                }
                categoryVo.setCategorys(childCategory);
                categorys.add(categoryVo);
            }
        }
        mv.addObject("data", categorys);
        return mv;
    }

    /**
     * 商品列表
     *
     * @return
     */
    @RequestMapping(value = "/productsList", method = RequestMethod.GET)
    public ModelAndView productsList(SearchCriteria criteria, String type) {
        ModelAndView mv = new ModelAndView();
        int requestType =2 ;
        if (StringUtils.isNotBlank(type)) {
            requestType = Integer.valueOf(type);
        }
        List li = new ArrayList();
        Map map = new HashMap();
        PageableResult<ProductModel> products;
        List<ProductModel> products1;
        //category level page size
        // PageableResult <ProductModel> products=productIndexServiceImpl.searchPro(Long.valueOf(criteria.getCategoryId()),criteria.getLevel(),criteria.getPage(),criteria.getPageSize());
        if (!StringUtils.isBlank(criteria.getCategoryId())) {
            //search by category
            products = productIndexServiceImpl.searchPro(Long.valueOf(2), 1, 1, 10);
            if (products != null && products.getData().size() > 0) {
                List<ProductModel> productModes = products.getData();
                for (ProductModel productModel : productModes) {
                    ProductListVo productListVo = new ProductListVo();
                    productListVo.setCommentNum(productModel.getRating());
                    productListVo.setId(productModel.getId());
                    productListVo.setImageUrl("http://pic95.nipic.com/file/20160419/7874840_024541265000_2.jpg");
                    productListVo.setName(productModel.getTitle());
                    productListVo.setPrice(productModel.getPrice());
                    productListVo.setStoresNum(5);
                    productListVo.setRatingNum(3.5f);
                    li.add(productListVo);
                }
            }
        } else {
            //search by title
            //productIndexServiceImpl.simpleSearch(criteria.getKeyword(),1,10);
            PageableResult p = productIndexServiceImpl.SearchProductsByKey(criteria.getKeyword(), 1, 10);
            if (p != null && p.getData().size() > 0) {
                List<ProductModel> productModes = p.getData();
                for (ProductModel productModel : productModes) {
                    ProductListVo productListVo = new ProductListVo();
                    productListVo.setCommentNum(productModel.getRating());
                    productListVo.setId(productModel.getId());
                    productListVo.setImageUrl("http://pic95.nipic.com/file/20160419/7874840_024541265000_2.jpg");
                    productListVo.setName(productModel.getTitle());
                    productListVo.setPrice(productModel.getPrice());
                    productListVo.setStoresNum(5);
                    productListVo.setRatingNum(3.5f);
                    li.add(productListVo);
                }
            }
        }
        String data = "";
        //查询热卖商品
        List<PtmProduct> products2s=productCacheManager.getTopSellingProductsByDate("20160627", 1, 20);
        switch (requestType) {
            case 0:
                if (products2s != null && products2s.size() > 0) {
                    int i = 0;
                    for (PtmProduct ptmProduct : products2s) {
                        if (i < 5) {
                            ProductListVo productListVo = new ProductListVo();
                            productListVo.setCommentNum(ptmProduct.getRating());
                            productListVo.setId(ptmProduct.getId());
                            productListVo.setImageUrl("http://pic101.nipic.com/file/20160606/23332452_170901893000_2.jpg");
                            productListVo.setName(ptmProduct.getTitle());
                            productListVo.setPrice(ptmProduct.getPrice());
                            productListVo.setStoresNum(5);
                            productListVo.setRatingNum(3.5f);
                            li.add(productListVo);
                            i++;
                        }
                    }
                }
                map.put("product", li);
                break;
            case 1:
                if (products2s != null && products2s.size() > 0) {
                    for (PtmProduct ptmProduct : products2s) {
                        ProductListVo productListVo = new ProductListVo();
                        productListVo.setCommentNum(ptmProduct.getRating());
                        productListVo.setId(ptmProduct.getId());
                        productListVo.setImageUrl("http://pic101.nipic.com/file/20160606/23332452_170901893000_2.jpg");
                        productListVo.setName(ptmProduct.getTitle());
                        productListVo.setPrice(ptmProduct.getPrice());
                        productListVo.setStoresNum(5);
                        productListVo.setRatingNum(3.5f);
                        li.add(productListVo);
                    }
                }
                map.put("product", li);
                break;
            case 2:
                PageableResult p = productIndexServiceImpl.SearchProductsByKey(criteria.getKeyword(), 1, 10);
                if (p != null && p.getData().size() > 0) {
                    List<ProductModel> productModes = p.getData();
                    for (ProductModel productModel : productModes) {
                        ProductListVo productListVo = new ProductListVo();
                        productListVo.setCommentNum(productModel.getRating());
                        productListVo.setId(productModel.getId());
                        productListVo.setImageUrl("http://pic101.nipic.com/file/20160606/23332452_170901893000_2.jpg");
                        productListVo.setName(productModel.getTitle());
                        productListVo.setPrice(productModel.getPrice());
                        productListVo.setStoresNum(5);
                        productListVo.setRatingNum(3.5f);
                        li.add(productListVo);
                    }
                }
                map.put("product", li);
                break;
            default:
                map.put("product", li);
        }
        if (li != null && li.size() > 0) {
            map.put("product", li);
        }
        mv.addObject("data", map);
        return mv;
    }
}
