package hasoffer.api.controller;

import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.app.vo.mobile.SiteMapKeyVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

                new SiteMapKeyVo("Top 10 Htc Desire Series Mobiles", 2).buildeShortName("Htc Desire Series"),
                new SiteMapKeyVo("Top 10 Sony Xperia Series Mobiles", 2).buildeShortName("Sony Xperia Series"),
                new SiteMapKeyVo("Top 10 Samsung Galaxy Series Mobiles", 2).buildeShortName("Samsung Galaxy Series"),
                new SiteMapKeyVo("Top 10 Lg Optimus Series Mobiles", 2).buildeShortName("Lg Optimus Series"),
                new SiteMapKeyVo("Top 10 Nokia Lumia Series Mobiles", 2).buildeShortName("Nokia Lumia Series"),
                new SiteMapKeyVo("Top 10 Nokia Asha Series Mobiles", 2).buildeShortName("Nokia Asha Series"),
                //Top 10 + “品牌名称” + Mobiles + Below +“价格参数”
                new SiteMapKeyVo("Top 10 HTC Mobiles Below 50000", 2).builderProMap("price", "5000").builderProMap("brand", "HTC")
        ));
        modelAndView.addObject("data", keyMap);
        return modelAndView;
    }

    /**
     * 处理前端的关键字"搜索"
     *
     * @param shortName
     * @param name
     * @param type
     * @param pros
     * @return
     */
    public ResultVo resolveKeyWordsSearch(String shortName, String name, @RequestParam(defaultValue = "0") int type, Map pros, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        switch (type) {
            case 0:
                //0 是把name 发回来
                if (StringUtils.isNotEmpty(name)) {
                    if (name.equals("Latest Mobiles")) {
                        //获取发布日期为最近的10部手机 -- 创建时间降序
                    }
                    if (name.equals("Top Mobile Phone")) {
                        //评分数前十的手机
                    }
                }
                break;
            case 1:
                //1 是把shortName 发回来
                if (StringUtils.isNotEmpty(shortName)) {
                    //从数据库中like查询 series 系列商品

                }
                break;
            case 2:
                // 2 是把pros中的数据发回来
                Set<Map.Entry<String, String>> set = pros.entrySet();
                Iterator<Map.Entry<String, String>> iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    //key 有 price  brand  --特征1  特征2
                    String key = next.getKey();

                    String value = next.getValue();


                }
                break;
            case 3:
                // 3 是按照name去调用搜索接口
                break;

            default:
        }
        return null;
    }

}
