package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.controller.vo.DealVo;
import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.api.helper.Httphelper;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.product.solr.DealIndexServiceImpl;
import hasoffer.core.product.solr.DealModel;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by hs on 2016/7/25.
 * 专用于Deal的Controller
 */
@Controller
@RequestMapping("deal")
public class AppDealController {
    Logger logger = LoggerFactory.getLogger(AppDealController.class);
    @Resource
    IDealService dealService;
    @Resource
    DealIndexServiceImpl indexService;

    /**
     * 获取商品相关deal列表
     *
     * @return modelAndView
     */
    @RequestMapping("product")
    public ModelAndView getDealsByProductTitle(@RequestParam(defaultValue = "") String title,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        //TODO 从Solr搜索Deal列表
        ModelAndView modelAndView = new ModelAndView();
        List<DealVo> deals = new ArrayList<DealVo>();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        List<DealModel> dealModels = indexService.simpleSearch(title, page, pageSize);
        if (dealModels != null && dealModels.size() > 0) {
            for (DealModel dealModel : dealModels) {
                DealVo dealVo = new DealVo();
                dealVo.setLogoUrl(dealModel.getWebsite() == null ? "" : WebsiteHelper.getLogoUrl(dealModel.getWebsite()));
                dealVo.setTitle(dealModel.getTitle());
                dealVo.setWebsite(dealModel.getWebsite());
                dealVo.setId(dealModel.getId());
                dealVo.setDiscount(new Random().nextInt(50) + 50);
                String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
                DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                dealVo.setDeepLink(dealModel.getLinkUrl() == null ? "" : WebsiteHelper.getDealUrlWithAff(dealModel.getWebsite(), dealModel.getLinkUrl(), new String[]{deviceInfo.getMarketChannel().name(), deviceId}));
                deals.add(dealVo);
            }
            Map map = new HashMap();
            map.put("deals", deals);
            modelAndView.addObject("data", dealModels);
        } else {
            modelAndView.addObject("data", "{\n" +
                    "        \"deals\": [\n" +
                    "            {\n" +
                    "                \"website\": \"FLIPKART\",\n" +
                    "                \"logoUrl\": \"http://img2.imgtn.bdimg.com/it/u=878754940,809562928&fm=21&gp=0.jpg\",\n" +
                    "                \"title\": \"宏碁（acer）TMP236 13.3英寸轻薄笔记本电脑（i5-5200U 8G 8G SSHD+500G 核芯显卡 全高清屏Win7）\",\n" +
                    "                \"discount\": \"60\",\n" +
                    "                \"deepLink\": \"http://item.jd.com/1362743.html?cpdad=1DLSUE\",\n" +
                    "                \"id\": \"1362743\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"website\": \"FLIPKART\",\n" +
                    "                \"logoUrl\": \"http://img2.imgtn.bdimg.com/it/u=878754940,809562928&fm=21&gp=0.jpg\",\n" +
                    "                \"title\": \"伯希和PELLIOT户外防晒皮肤衣 男女轻薄透气速衣1731 男孔雀蓝色 L\",\n" +
                    "                \"discount\": \"70\",\n" +
                    "                \"deepLink\": \"http://item.jd.com/2908042.html?cpdad=1DLSUE\",\n" +
                    "                \"id\": \"2908042\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }");
        }
        return modelAndView;
    }

    @RequestMapping("info")
    public String getDealById(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        AppDeal appDeal = dealService.getDealById(id);
        Map hashMap = new HashMap<>();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        hashMap.put("provisions", "• Taxs are applicable.\\n• This offer cannot be clubbed with any other ongoing offer.\\n• Offer cannot be redeemed for cash.\\n• No coupon code required.\\n• Company has the right to end this offer without prior notice.\"\n");
        if (appDeal != null) {
            logger.info("has this deal " + id);
            hashMap.put("description", appDeal.getDescription());
        }
        jsonObject.put("data", hashMap);
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }
}
