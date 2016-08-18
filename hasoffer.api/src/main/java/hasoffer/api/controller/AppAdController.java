package hasoffer.api.controller;

import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.app.AdvertiseService;
import hasoffer.core.persistence.po.admin.Adt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016/7/25.
 * 专用于广告相关的Controller
 */
@Controller
@RequestMapping("ad")
public class AppAdController {
    static Map<Website, String> packageMap = new HashMap<Website, String>();

    static {
        packageMap.put(Website.SNAPDEAL, "com.snapdeal.main");
        packageMap.put(Website.FLIPKART, "com.flipkart.android");
        packageMap.put(Website.AMAZON, "in.amazon.mShop.android.shopping");
        packageMap.put(Website.PAYTM, "net.one97.paytm");
        packageMap.put(Website.EBAY, "com.ebay.mobile");
        packageMap.put(Website.SHOPCLUES, "com.shopclues");
        packageMap.put(Website.INFIBEAM, "com.infibeam.infibeamapp");
        packageMap.put(Website.MYNTRA, "com.myntra.android");
        packageMap.put(Website.JABONG, "com.jabong.android");
        packageMap.put(Website.VOONIK, "com.voonik.android");
    }

    Logger logger = LoggerFactory.getLogger(AppAdController.class);
    @Resource
    AdvertiseService advertiseService;

    /**
     * 根据商品id获取category
     * 根据category匹配广告
     *
     * @return
     */
    @RequestMapping("product")
    public ModelAndView getAdsByProductId(@RequestParam(defaultValue = "0") Long productId, @RequestParam(defaultValue = "") String website) {
        ModelAndView modelAndView = new ModelAndView();
        Map map = new HashMap<>();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        List<Adt> adt = advertiseService.getAdByCategory();
        if (adt != null && adt.size() > 0) {
            Adt adt1 = adt.get(0);
            if (!StringUtils.isEmpty(website)) {
                adt1.setPackageName(packageMap.get(Website.valueOf(website)));
            }
            map.put("ads", Arrays.asList(adt1));
            modelAndView.addObject("data", map);
        }
        return modelAndView;
    }
}
