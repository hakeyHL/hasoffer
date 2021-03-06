package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.ApiHttpHelper;
import hasoffer.api.helper.ClientHelper;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.third.ThirdService;
import hasoffer.core.utils.ConstantUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by hs on 2017年02月13日.
 * Time 16:25
 */
@Controller
@RequestMapping("third/api")
public class ThirdApiController extends BaseController {
    @Resource
    ThirdService thirdService;

    /**
     * 获取热卖商品列表
     *
     * @param page
     * @param pageSize
     * @param response
     * @return
     */
    @RequestMapping("topselling")
    public String getTopSkusForNineApp(@RequestParam(defaultValue = "1") String page,
                                       @RequestParam(defaultValue = "10") String pageSize,
                                       HttpServletResponse response) {
        DeviceInfoVo deviceInfo = ClientHelper.getDeviceInfo();
        String topSkus = thirdService.listTopSkusForNineApps(page, pageSize, null, 0, new String[]{deviceInfo.getMarketChannel().name(), ClientHelper.getAndroidId()});
        ApiHttpHelper.sendJsonMessage(topSkus, response);
        return null;
    }

    //将点击跳转类型为deal且当前时间在生效与失效日期之间的Banner按照创建时间降序返回
    @RequestMapping("banners")
    public String listBannersForNineApp(HttpServletResponse response) {
        //仅返回5个,最多返回5个
        //规则为:跳转类型为Deal且未失效,按照创建时间降序返回
        List dataList = thirdService.listBannerForNineApp();
        resultJsonObj.put(ConstantUtil.API_NAME_DATA, dataList);
        ApiHttpHelper.sendJsonMessage(JSON.toJSONString(resultJsonObj), response);
        return null;
    }

    /**
     * @param response
     * @param id       sku的id
     * @return
     */
    @RequestMapping("skuInfo/{id}")
    public String getPtmStdPriceInfoForNineApp(HttpServletResponse response
            , @PathVariable("id") String id) {
        if (StringUtils.isEmpty(id) || !StringUtils.isNumericSpace(id)) {
            initErrorCodeAndMsgFail();
            resultJsonObj.put(ConstantUtil.API_NAME_MSG, "id required.");
            ApiHttpHelper.sendJsonMessage(resultJsonObj.toJSONString(), response);
            return null;
        }
        JSONObject dataJsonObj = thirdService.getPtmStdPriceInfo(Long.parseLong(id));
        resultJsonObj.put(ConstantUtil.API_NAME_DATA, dataJsonObj);
        ApiHttpHelper.sendJsonMessage(JSON.toJSONString(resultJsonObj), response);
        return null;
    }

    //整合接口
    //获取banner列表,topselling列表,deals列表,deals列表 ,只有deals列表支持 分页
    @RequestMapping("nineapp/index")
    public String listDealsTopsellingsBanners(@RequestParam(defaultValue = "1") String page,
                                              @RequestParam(defaultValue = "10") String pageSize,
                                              HttpServletResponse response) {
        DeviceInfoVo deviceInfo = ClientHelper.getDeviceInfo();
        List bannerForNineApp = thirdService.listBannerForNineApp();
        getJsonDataObj().put("banners", bannerForNineApp);

        String topSkus = thirdService.listTopSkusForNineApps("1", "10", null, 0, new String[]{deviceInfo.getMarketChannel().name(), ClientHelper.getAndroidId()});
//        proList
        JSONObject topSellingJsonObj = (JSONObject) JSONObject.parse(topSkus);
        JSONObject dataJsonObj = (JSONObject) topSellingJsonObj.get(ConstantUtil.API_NAME_DATA);
        JSONArray proList = dataJsonObj.getJSONArray("proList");
        getJsonDataObj().put("toplist", proList);

        String dealsForIndia = thirdService.listDealsForIndia(Integer.parseInt(page), Integer.parseInt(pageSize), "originPrice", "presentPrice");
        //去掉errorCode和msg
        JSONObject dealsJsonObj = (JSONObject) JSONObject.parse(dealsForIndia);
        dealsJsonObj.remove(ConstantUtil.API_NAME_ERRORCODE);
        dealsJsonObj.remove(ConstantUtil.API_NAME_MSG);

        getJsonDataObj().put("offer", dealsJsonObj.get(ConstantUtil.API_NAME_DATA));
        ApiHttpHelper.sendJsonMessage(JSON.toJSONString(resultJsonObj), response);
        return null;
    }

}
