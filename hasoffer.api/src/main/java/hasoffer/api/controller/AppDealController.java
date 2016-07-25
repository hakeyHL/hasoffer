package hasoffer.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by hs on 2016/7/25.
 * 专用于Deal的Controller
 */
@Controller
@RequestMapping("deal")
public class AppDealController {
    Logger logger = LoggerFactory.getLogger(AppDealController.class);

    /**
     * 获取商品相关deal列表
     *
     * @return modelAndView
     */
    @RequestMapping("product")
    public ModelAndView getDealsByProductTitle() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        modelAndView.addObject("data", "{\n" +
                "        \"deals\": [\n" +
                "            {\n" +
                "                \"website\": \"FLIPKART\",\n" +
                "                \"logoUrl\": \"http://img2.imgtn.bdimg.com/it/u=878754940,809562928&fm=21&gp=0.jpg\",\n" +
                "                \"title\": \"宏碁（acer）TMP236 13.3英寸轻薄笔记本电脑（i5-5200U 8G 8G SSHD+500G 核芯显卡 全高清屏Win7）\",\n" +
                "                \"discount\": \"200\",\n" +
                "                \"deepLink\": \"http://item.jd.com/1362743.html?cpdad=1DLSUE\",\n" +
                "                \"id\": \"1362743\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"website\": \"FLIPKART\",\n" +
                "                \"logoUrl\": \"http://img2.imgtn.bdimg.com/it/u=878754940,809562928&fm=21&gp=0.jpg\",\n" +
                "                \"title\": \"伯希和PELLIOT户外防晒皮肤衣 男女轻薄透气速衣1731 男孔雀蓝色 L\",\n" +
                "                \"discount\": \"\",\n" +
                "                \"deepLink\": \"http://item.jd.com/2908042.html?cpdad=1DLSUE\",\n" +
                "                \"id\": \"2908042\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }");
        return modelAndView;
    }

    @RequestMapping("info")
    public ModelAndView getDealById() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        modelAndView.addObject("data", "{\n" +
                "        \"description\": \"DTS Studio Sound\\nLED Backlit display\\nDOS Operating System\\nIceland Style Kboard\",\n" +
                "        \"provisions \": \"• Taxs are applicable.\\n• This offer cannot be clubbed with any other ongoing offer.\\n• Offer cannot be redeemed for cash.\\n• No coupon code required.\\n• Company has the right to end this offer without prior notice.\"\n" +
                "    }");
        return modelAndView;
    }
}
