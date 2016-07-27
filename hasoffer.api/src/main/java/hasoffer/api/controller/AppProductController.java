package hasoffer.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by hs on 2016年07月25日.
 * Time 17:14
 */
@Controller
@RequestMapping("product")
public class AppProductController {
    Logger logger = LoggerFactory.getLogger(AppProductController.class);

    /**
     * 根据商品获取比价的sku列表
     *
     * @return
     */
    @RequestMapping("cmpskus")
    public ModelAndView getSkusByProductId() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        modelAndView.addObject("data", "{\n" +
                "        \"copywriting\": \"\",\n" +
                "        \"show\": \"waterfall\",\n" +
                "        \"skus\": [\n" +
                "            {\n" +
                "                \"status\": \"onsale\",\n" +
                "                \"title\": \"小王子（法国“圣埃克苏佩里基金会”官方认可简体中文译本）\",\n" +
                "                \"imageUrl\": \"http://img13.360buyimg.com/n1/jfs/t2200/173/590579185/269686/4c299e77/56174e3eN362982a4.jpg\",\n" +
                "                \"cashBack\": \"10\",\n" +
                "                \"deepLink\": \"http://item.jd.com/11143993.html\",\n" +
                "                \"saved\": 100,\n" +
                "                \"id\": \"11143993\",\n" +
                "                \"price\": \"1,000\",\n" +
                "                \"website\": \"FLIPKART\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"status\": \"sold out\",\n" +
                "                \"title\": \"摩斯维 手机套/金属边框/防摔保护壳外壳 适用于华为荣耀畅玩4X/全网通/电信/移动版 拉丝尊享款-香槟金-送钢化膜\",\n" +
                "                \"imageUrl\": \"http://img11.360buyimg.com/n1/jfs/t2698/221/1187894551/168647/33c6c8e1/5736a5f7Nfa29f761.jpg\",\n" +
                "                \"cashBack\": \"20\",\n" +
                "                \"deepLink\": \"http://item.jd.com/1381873091.html\",\n" +
                "                \"saved\": 100,\n" +
                "                \"id\": \"1381873091\",\n" +
                "                \"price\": \"1,000\",\n" +
                "                \"website\": \"FLIPKART\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }");
        return modelAndView;
    }
}
