package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.model.Website;
import hasoffer.core.app.AppClientCfgService;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.app.vo.ThirdAppVo;
import hasoffer.core.redis.impl.CacheServiceImpl;
import hasoffer.fetch.helper.WebsiteHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by hs on 2016年10月17日.
 * Time 12:23
 * 客户端配置参数controller
 */
@Controller
@RequestMapping(value = "/cfg")
public class AppCliCfgController {
    static final String HOME_REDEEM_TIP_COPY = "app_home_copy";
    static final String HOME_INDEX_COPY = "app_home_index_copy";
    @Autowired
    private CacheServiceImpl cacheService;
    private Logger logger = LoggerFactory.getLogger(AppCliCfgController.class);
    @Autowired
    private AppClientCfgService appClientCfgService;

    public static void main(String[] args) {
    }

    @RequestMapping(value = "/app/homeCfg")
    public String homePageRewardsConfig(@RequestParam(defaultValue = "10000") int action,
                                        HttpServletResponse response,
                                        String stringFirst,
                                        String stringSecond) {
        ResultVo resultVo = new ResultVo();
        Map<String, Boolean> map = new HashMap<>();
        switch (action) {
            case 1:
                //get home page redeem tip
                String homeRedeemTip = cacheService.get(HOME_REDEEM_TIP_COPY, 0);
                if (!StringUtils.isEmpty(homeRedeemTip)) {
                    List<String> strings = JSONArray.parseArray(homeRedeemTip, String.class);
                    resultVo.getData().put("redeem", strings);
                } else {
                    List<String> strings = Arrays.asList("Get Rs100-1000 Gifts !", "on check-in everyday");
                    resultVo.getData().put("redeem", strings);
                    cacheService.add(HOME_REDEEM_TIP_COPY, JSON.toJSONString(strings), -1);
                }
                break;
            case 2:
                //change home page redeem tip
                logger.info("enter home copy swap ");
                if (StringUtils.isNotEmpty(stringFirst) && StringUtils.isNotEmpty(stringSecond)) {
                    List<String> redeemStrings = Arrays.asList(stringFirst, stringSecond);
                    //get home page redeem tip
                    String homeRedeemTip2 = cacheService.get(HOME_REDEEM_TIP_COPY, 0);
                    if (!StringUtils.isEmpty(homeRedeemTip2)) {
                        //delete
                        cacheService.del(HOME_REDEEM_TIP_COPY);
                        cacheService.add(HOME_REDEEM_TIP_COPY, JSON.toJSONString(redeemStrings), -1);
                    } else {
                        //add
                        cacheService.add(HOME_REDEEM_TIP_COPY, JSON.toJSONString(redeemStrings), -1);
                    }
                }
                break;
            case 3:
                logger.info("client scan config ");
                // config
                //search
                map.put("001", false);

                //wishlist
                map.put("002", true);

                //购物车
                map.put("003", true);

                //email and phone get
                map.put("004", true);

                resultVo.setData(map);

                break;
            case 4:
                int flag = new Random().nextInt(100);
                if (flag < 5) {
                    map.put("isBoot", false);
                } else {
                    map.put("isBoot", true);
                }
                resultVo.setData(map);
                break;
            case 5:
                List<ThirdAppVo> tempGOOGLEPLAY = new ArrayList<>();
                ThirdAppVo apps_Amazon = new ThirdAppVo(Website.AMAZON, AppAdController.packageMap.get(Website.AMAZON), "https://play.google.com/store/apps/details?id=com.amazon.mShop.android.shopping", WebsiteHelper.getSiteUrl(Website.AMAZON), "Browse,search & buy millions of products right from your Android device", 4.3f, "491,637", "50,000,000 - 100,000,000", "9.6MB");
                ThirdAppVo apps_Flipkart = new ThirdAppVo(Website.FLIPKART, AppAdController.packageMap.get(Website.FLIPKART), "http://dl.flipkart.com/dl/install-app?affid=zhangchen", WebsiteHelper.getSiteUrl(Website.FLIPKART), "Shop for electronics,apparels & more using our Flipart app Free shipping & COD", 4.2f, "2,044,978", "50,000,000 - 100,000,000", "10.0MB");
                ThirdAppVo apps_ShopClues = new ThirdAppVo(Website.SHOPCLUES, AppAdController.packageMap.get(Website.SHOPCLUES), "https://play.google.com/store/apps/details?id=com.shopclues", WebsiteHelper.getSiteUrl(Website.SHOPCLUES), "India's largest Online Marketplace is now in your Pocket - Install,Shop,Enjoy!", 3.9f, "235,468", "10,000,000 - 50,000,000", "7.1MB");
                ThirdAppVo apps_eBay = new ThirdAppVo(Website.EBAY, AppAdController.packageMap.get(Website.EBAY), "https://play.google.com/store/apps/details?id=com.ebay.mobile", WebsiteHelper.getSiteUrl(Website.EBAY), "Buy,bid & sell! Deals & Discounts to Save Money on Home,Collectables & Cars", 4.2f, "1,759,547", "100,000,000 - 500,000,000", "20.6MB");
                ThirdAppVo apps_Paytm = new ThirdAppVo(Website.PAYTM, AppAdController.packageMap.get(Website.PAYTM), "https://play.google.com/store/apps/details?id=net.one97.paytm", WebsiteHelper.getSiteUrl(Website.PAYTM), "Best Mobile Recharge and DTH Recharge, Bill Payment and Shipping Experience", 4.3f, "1,401,209", "10,000,000 - 50,000,000", "13.0MB");
                ThirdAppVo apps_Snapdeal = new ThirdAppVo(Website.SNAPDEAL, AppAdController.packageMap.get(Website.SNAPDEAL), "https://play.google.com/store/apps/details?id=com.snapdeal.main", WebsiteHelper.getSiteUrl(Website.SNAPDEAL), "Best deals on women & men's fashion,home essentials,electronics & gadgets!", 4.1f, "1,035,900", "10,000,000 - 50,000,000", "12.0MB");
                ThirdAppVo apps_Jabong = new ThirdAppVo(Website.JABONG, AppAdController.packageMap.get(Website.JABONG), "https://play.google.com/store/apps/details?id=com.jabong.android", WebsiteHelper.getSiteUrl(Website.JABONG), "India's Best Online Shopping App To Buy Latest Fashion for Men,Women,Kids", 3.9f, "171,487", "10,000,000 - 50,000,000", "6.1MB");
                ThirdAppVo apps_VOONIK = new ThirdAppVo(Website.VOONIK, AppAdController.packageMap.get(Website.VOONIK), "https://play.google.com/store/apps/details?id=com.voonik.android", WebsiteHelper.getSiteUrl(Website.VOONIK), "Online Shopping for women clothing,ethnic wear,sarees,kurtis,lingere in India", 4.2f, "129,079", "5,000,000 - 10,000,000", "5.8MB");
                ThirdAppVo apps_INFIBEAM = new ThirdAppVo(Website.INFIBEAM, AppAdController.packageMap.get(Website.INFIBEAM), "https://play.google.com/store/apps/details?id=com.infibeam.infibeamapp", WebsiteHelper.getSiteUrl(Website.INFIBEAM), "Infibeam.com-Buy Mobiles,Electronics,Books,Gifts,Clothes & more", 3.7f, "8,424", "1,000,000 - 5,000,000", "26.2MB");
                ThirdAppVo apps_Myntra = new ThirdAppVo(Website.MYNTRA, AppAdController.packageMap.get(Website.MYNTRA), "https://play.google.com/store/apps/details?id=com.myntra.android&hl=en", WebsiteHelper.getSiteUrl(Website.MYNTRA), "Online shopping for fashion clothes,footwear,accessories for Men,Women & Kids", 4.1f, "509,053", "10,000,000 - 50,000,000", "17.2MB");
                tempGOOGLEPLAY.addAll(Arrays.asList(apps_Amazon, apps_Flipkart, apps_ShopClues, apps_eBay, apps_Paytm, apps_Snapdeal, apps_Jabong, apps_VOONIK, apps_INFIBEAM, apps_Myntra));
                resultVo.getData().put("shopApps", tempGOOGLEPLAY);
                break;
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(resultVo), response);
        return null;
    }

    @RequestMapping(value = "/app/homeIndex")
    public String homeIndexConfig(HttpServletResponse response,
                                  String stringFirst,
                                  String stringSecond,
                                  String stringThird) {
        ResultVo resultVo = new ResultVo();
        //change home page redeem tip
        if (StringUtils.isNotEmpty(stringFirst) && StringUtils.isNotEmpty(stringSecond) && StringUtils.isNotEmpty(stringThird)) {
            //set
            List<String> redeemStrings = Arrays.asList(stringFirst, stringSecond, stringThird);
            //get home page redeem tip
            String homeRedeemTip2 = cacheService.get(HOME_INDEX_COPY, 0);
            resultVo.getData().put("bootIndex", redeemStrings);
            if (!StringUtils.isEmpty(homeRedeemTip2)) {
                //delete
                cacheService.del(HOME_INDEX_COPY);
                cacheService.add(HOME_INDEX_COPY, JSON.toJSONString(resultVo.getData()), -1);
            } else {
                //add
                cacheService.add(HOME_INDEX_COPY, JSON.toJSONString(resultVo.getData()), -1);
            }
        } else {
            //get
            String bootIndex = cacheService.get(HOME_INDEX_COPY, 0);
            if (!StringUtils.isEmpty(bootIndex)) {
                try {
                    JSONObject jsonObject = JSON.parseObject(bootIndex);
                    List<String> bootIndex1 = JSONArray.parseArray(jsonObject.getString("bootIndex"), String.class);
                    resultVo.getData().put("bootIndex", bootIndex1);
                } catch (Exception e) {
                    //出现异常时返回默认
                    resultVo.getData().put("bootIndex", Arrays.asList("GET YOUR DAILY COINS!",
                            "100 Coins=1 Rupee!The more often you check in,the more you will earn",
                            "REEDEM COINS FOR SUPER GIFT!"));
                }
            } else {
                resultVo.getData().put("bootIndex", Arrays.asList("GET YOUR DAILY COINS!",
                        "100 Coins=1 Rupee!The more often you check in,the more you will earn",
                        "REEDEM COINS FOR SUPER GIFT!"));
                //add to cache
                cacheService.add(HOME_INDEX_COPY, JSON.toJSONString(resultVo.getData()), -1);
            }
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(resultVo), response);
        return null;
    }

    @RequestMapping(value = "/app/pushCfg")
    public ModelAndView appPushConfig() {
        ModelAndView modelAndView = new ModelAndView();
        ResultVo resultVo = new ResultVo();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        resultVo.getData().put("open", true);
        resultVo.getData().put("unit", "m");//d 天 h 小时 m 分钟
        resultVo.getData().put("scanInterval", 10);
        modelAndView.addObject("data", resultVo.getData());
        return modelAndView;
    }

    /**
     * 配置返回shop list的图标返回
     * 后续可能还会配置其他图片的返回
     *
     * @return
     */
    @RequestMapping(value = "/app/picCfg")
    public ModelAndView picCfg(@RequestParam(defaultValue = "") String picType) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "success");
        Map dataMap = new HashMap();
        switch (picType) {
            case "index_shopApp_icon":
                dataMap.put("index_shopApp_icon", "http://img1.hasofferimage.com/cate/shopAppIcon.png");
                modelAndView.addObject("data", dataMap);
                return modelAndView;
            default:
                modelAndView.addObject("errorCode", "10000");
                modelAndView.addObject("msg", "no this type " + picType);
                return modelAndView;
        }
    }
}
