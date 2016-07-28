package hasoffer.api.controller;

import hasoffer.core.app.AdvertiseService;
import hasoffer.core.persistence.po.admin.Advertisement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by hs on 2016/7/25.
 * 专用于广告相关的Controller
 */
@Controller
@RequestMapping("ad")
public class AppAdController {
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
    public ModelAndView getAdsByProductId() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        List<Advertisement> advertisements = advertiseService.getAdByCategory();
        if (advertisements != null && advertisements.size() > 0) {
            modelAndView.addObject("data", advertisements.get(0));
        } else {
            modelAndView.addObject("data", "{\n" +
                    "        \"aderlogoUrl\": \"http://h.hiphotos.baidu.com/baike/w%3D268%3Bg%3D0/sign=d66357243fdbb6fd255be220311fcc25/c75c10385343fbf235a845fcb67eca8064388f6d.jpg\",\n" +
                    "        \"aderName\": \"京东\",\n" +
                    "        \"adImage\": \"http://img14.360buyimg.com/n1/jfs/t2191/111/699154754/198998/32d7bfe0/5624b582Nbc01af5b.jpg\",\n" +
                    "        \"adSlogan \": \"Java编程思想\",\n" +
                    "        \"adLink\": \"http://xihuan.jd.com/11143993.html\",\n" +
                    "        \"adBtnContent\": \"buy it\",\n" +
                    "        \"aderSiteUrl\": \"http://www.jd.com\",\n" +
                    "        \"adLocation\": 3\n" +
                    "    }");
        }
        return modelAndView;
    }
}