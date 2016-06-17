package hasoffer.api.controller;

import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.ParseConfigHelper;
import hasoffer.api.worker.SearchLogQueue;
import hasoffer.base.enums.AppType;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.system.IAppService;
import hasoffer.core.user.IDeviceService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private Logger logger = LoggerFactory.logger(AppController.class);

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
     * @param userToken
     * @return
     */
    @RequestMapping(value = "/backDetail", method = RequestMethod.GET)
    public ModelAndView backDetail(@RequestParam String userToken) {
        ModelAndView mv=new ModelAndView();
        BackDetailVo data =new BackDetailVo();
        data.setPendingCoins(Long.valueOf(300));
        data.setVericiedCoins(Long.valueOf(200));

        List <OrderVo>transcations=new ArrayList<OrderVo>();
        OrderVo orderVo=new OrderVo();
        orderVo.setAccount(Long.valueOf(200));
        orderVo.setChannel("SEAPDEAL");
        orderVo.setOrderId("ererer");
        orderVo.setOrderTime(new Date());
        orderVo.setRate(Long.valueOf(22));
        orderVo.setTotal(Long.valueOf(1000));
        orderVo.setType(0);
        transcations.add(orderVo);

        OrderVo orderVo1=new OrderVo();
        orderVo1.setAccount(Long.valueOf(100));
        orderVo1.setChannel("EBAY");
        orderVo1.setOrderId("87656");
        orderVo1.setOrderTime(new Date());
        orderVo1.setRate(Long.valueOf(50));
        orderVo1.setTotal(Long.valueOf(200));
        orderVo1.setType(1);
        transcations.add(orderVo1);

        data.setTranscations(transcations);
        mv.addObject("data",data);
        return mv;
    }

    /**
     * 订单详情
     * @param orderId
     * @param userToken
     * @return
     */
    @RequestMapping(value = "/orderDetail", method = RequestMethod.GET)
    public ModelAndView orderDetail(@RequestParam String orderId,@RequestParam String userToken) {
        ModelAndView mv=new ModelAndView();
        OrderVo orderVo=new OrderVo();
        orderVo.setTotal(Long.valueOf(300));
        orderVo.setType(0);
        orderVo.setRate(Long.valueOf(22));
        orderVo.setOrderTime(new Date());
        orderVo.setOrderId("45454");
        orderVo.setChannel("SNEAPDEAL");
        orderVo.setAccount(Long.valueOf(33));

        mv.addObject("data",orderVo);
        return mv;
    }

    /**
     * banners列表
     * @return
     */
    @RequestMapping(value = "/banners", method = RequestMethod.GET)
    public ModelAndView banners() {
        ModelAndView mv=new ModelAndView();
        String data="{\n" +
                "    \"banners\": [\n" +
                "        {\n" +
                "            \"sourceurl\": \"http://192.168.1.126/xx.png\",\n" +
                "            \"link\": \"192.168.1.126:8080/getProduct?id=eerer\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"sourceurl\": \"http://192.168.1.126/xx.png\",\n" +
                "            \"link\": \"192.168.1.126:8080/getProduct?id=eerer\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        mv.addObject("data",data);
        return mv;
    }

    /**
     * deal列表
     * @return
     */
    @RequestMapping(value = "/deals", method = RequestMethod.GET)
    public ModelAndView deals() {
        ModelAndView mv =new ModelAndView();
        String data="{\n" +
                "  \"banners\":[\n" +
                "  {\n" +
                "     \"sourceurl\":\"http://192.168.1.126/xx.png\",        \n" +
                "     \"link\":\"192.168.1.126:8080/getProduct?id=eerer\"  \n" +
                "},  \n" +
                "{\n" +
                "\"sourceurl\":\"http://192.168.1.126/xx.png\",\n" +
                "     \"link\":\"192.168.1.126:8080/getProduct?id=eerer\"\n" +
                "}\n" +
                "]\n" +
                "}";
        mv.addObject("data",data);
        return mv;
    }

    /**
     * deal详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/dealInfo", method = RequestMethod.GET)
    public ModelAndView dealInfo(@RequestParam String id) {
        ModelAndView mv =new ModelAndView();
        String data="{\n" +
                "    \"banners\": [\n" +
                "        {\n" +
                "            \"sourceurl\": \"http://192.168.1.126/xx.png\",\n" +
                "            \"link\": \"192.168.1.126:8080/getProduct?id=eerer\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"sourceurl\": \"http://192.168.1.126/xx.png\",\n" +
                "            \"link\": \"192.168.1.126:8080/getProduct?id=eerer\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        mv.addObject("data",data);
        return mv ;
    }

    /**
     * 用户信息绑定
     * @return
     */
    @RequestMapping(value = "/bindUserInfo", method = RequestMethod.POST)
    public ModelAndView bindUserInfo() {
        ModelAndView mv =new ModelAndView();
        String  data="{\n" +
                "    \"userToken\": \"erere\"\n" +
                "}";
        mv.addObject("data",data);
        return  mv;
    }

    /**
     * 用户信息获取
     * @return
     */
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public ModelAndView userInfo() {
        ModelAndView mv =new ModelAndView();
        String data="{\n" +
                "    \"name\": \"小李\",\n" +
                "\"coins\": 200,\n" +
                "    \"userIcon\": \"http://192.168.1.201:8080/test.jpg\"\n" +
                "}";
        mv.addObject("data",data);
        return  mv;
    }

    /**
     * 商品类目
     * @return
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public ModelAndView category() {
        ModelAndView mv =new ModelAndView();
        String data="{\n" +
                "    \"refreshTime\":\"05/06/2016 12:12:12\"\n" +
                "    \"categories\": [\n" +
                "        {\n" +
                "            \"name\": \"电器\",\n" +
                "            \"image\": \"192.168.1.126:8080/getProduct.jpg\",\n" +
                "            \"id\": \"erer\",\n" +
                "            \"hasChildren\": 1,\n" +
                "            \"parentId\": \"0\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"家用\",\n" +
                "            \"image\": \"192.168.1.126:8080/getProduct.jpg\",\n" +
                "            \"id\": \"3434t\",\n" +
                "            \"hasChildren\": 0,\n" +
                "            \"parentId\": \"erer\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        mv.addObject("data",data);
        return  mv;
    }

    /**
     * 商品列表
     * @return
     */
    @RequestMapping(value = "/productsList", method = RequestMethod.GET)
    public ModelAndView productsList() {
        ModelAndView mv =new ModelAndView();
        String data="{\n" +
                "    \"product\": [\n" +
                "        {\n" +
                "            \"id\": \"5556465\",\n" +
                "            \"name\": \"桃花朵朵开\",\n" +
                "            \"price\": 1000,\n" +
                "            \"storesNum\": 50,\n" +
                "            \"commentNum\": 1\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"456\",\n" +
                "            \"name\": \"水牛\",\n" +
                "            \"price\": 600,\n" +
                "            \"storesNum\": 10,\n" +
                "            \"commentNum\": 8\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        mv.addObject("data",data);
        return  mv;
    }

    /**
     * 商品详情
     * @return
     */
    @RequestMapping(value = "/produceInfo", method = RequestMethod.GET)
    public ModelAndView produceInfo() {
        ModelAndView mv =new ModelAndView();
        String data="{\n" +
                "    \"ratingsNum\": 4,\n" +
                "    \"images\": [\n" +
                "        \"192.168.1.126:8080/getProduct.jpg\",\n" +
                "        \"192.168.1.126:8080/getProduct.jpg\"\n" +
                "    ],\n" +
                "    \"name\": \"小牛\",\n" +
                "    \"bestPrice\": 200,\n" +
                "    \"totalRatingsNum\": 4,\n" +
                "    \"plats\": [\n" +
                "        {\n" +
                "            \"price\": 200,\n" +
                "            \"deepLink\": \"\",\n" +
                "            \"freight\": 20,\n" +
                "            \"suppor\": [\n" +
                "                \"COD\",\n" +
                "                \"EMI\"\n" +
                "            ],\n" +
                "            \"distributionTime\": \"05/06/2016 20:20:52\",\n" +
                "            \"postage\": \"\",\n" +
                "            \"returnGuarantee\": \"\",\n" +
                "            \"rebateInfor\": [\n" +
                "                {\n" +
                "                    \"coins\": 10,\n" +
                "                    \"note\": \"545\",\n" +
                "                    \"title\": \"dfdf\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"coins\": 2,\n" +
                "                    \"note\": \"yty\",\n" +
                "                    \"title\": \"iuiui\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"price\": 200,\n" +
                "            \"deepLink\": \"\",\n" +
                "            \"\": 20,\n" +
                "            \"suppor\": [\n" +
                "                \"COD\",\n" +
                "                \"EMI\"\n" +
                "            ],\n" +
                "            \"distributionTime\": \"\",\n" +
                "            \"postage\": \"\",\n" +
                "            \"returnGuarantee\": \"\",\n" +
                "            \"rebateInfor\": [\n" +
                "                {\n" +
                "                    \"coins\": 10,\n" +
                "                    \"note\": \"787\",\n" +
                "                    \"title\": \"090\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"coins\": 2,\n" +
                "                    \"note\": \"098\",\n" +
                "                    \"title\": \"fgtrt\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        mv.addObject("data",data);
        return  mv;
    }
}
