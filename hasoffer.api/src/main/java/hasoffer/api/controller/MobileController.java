package hasoffer.api.controller;

import hasoffer.core.app.vo.mobile.SiteMapKeyVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * Created by hs on 2016年12月20日.
 * H5 Controller
 * Time 16:38
 */
@Controller
@RequestMapping("m")
public class MobileController {
    @RequestMapping("siteMap")
    public ModelAndView siteMapHasoffer() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "success");

        List<SiteMapKeyVo> siteMapKeyVos = new ArrayList<>();

        Map keyMap = new HashMap();
        //key 1
        Map proMap = new HashMap();
        keyMap.put("Mobile Finder On Hasoffer", Arrays.asList(new SiteMapKeyVo("Latest Mobiles", 0)));

        //key 2
        proMap.clear();

        keyMap.put("All Mobile Models In India", Arrays.asList(
                new SiteMapKeyVo("Top Mobile Phones", 0),
                new SiteMapKeyVo("Gionee Elife S6", 3),
                new SiteMapKeyVo("Oppo R9 Plus", 3),
                new SiteMapKeyVo("Samsung Z1", 3),
                new SiteMapKeyVo("Honor 7", 3),
                new SiteMapKeyVo("Oppo A30", 3)));

        //key 3
        keyMap.put("Top 10 Mobiles", Arrays.asList(
                new SiteMapKeyVo("Top 10  Mobiles  Below 5000", 2).builderProMap("price", "5000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 10000", 2).builderProMap("price", "10000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 15000", 2).builderProMap("price", "15000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 20000", 2).builderProMap("price", "20000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 25000", 2).builderProMap("price", "25000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 30000", 2).builderProMap("price", "30000"),

                new SiteMapKeyVo("Top 10 Htc Desire Series Mobiles", 2),
                new SiteMapKeyVo("Top 10 Sony Xperia Series Mobiles", 2),
                new SiteMapKeyVo("Top 10 Samsung Galaxy Series Mobiles", 2),
                new SiteMapKeyVo("Top 10 Lg Optimus Series Mobiles", 2),
                new SiteMapKeyVo("Top 10 Nokia Lumia Series Mobiles", 2),
                new SiteMapKeyVo("Top 10 Nokia Asha Series Mobiles", 2)));

        modelAndView.addObject("data", keyMap);
        return modelAndView;
    }
}
