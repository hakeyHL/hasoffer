package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import hasoffer.api.helper.ApiHttpHelper;
import hasoffer.api.helper.ClientHelper;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.third.ThirdService;
import hasoffer.core.utils.ConstantUtil;
import org.springframework.stereotype.Controller;
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
        String topSkus = thirdService.getTopSkusForNineApps(page, pageSize, null, 0, new String[]{deviceInfo.getMarketChannel().name(), ClientHelper.getAndroidId()});
        ApiHttpHelper.sendJsonMessage(topSkus, response);
        return null;
    }

    //将点击跳转类型为deal且当前时间在生效与失效日期之间的Banner按照创建时间降序返回
    @RequestMapping("banners")
    public String getBannersForNineApp(HttpServletResponse response) {
        //仅返回5个,最多返回5个
        //规则为:跳转类型为Deal且未失效,按照创建时间降序返回
        List dataList = thirdService.getBannerForNineApp();
        resultJsonObj.put(ConstantUtil.API_NAME_DATA, dataList);
        ApiHttpHelper.sendJsonMessage(JSON.toJSONString(resultJsonObj), response);
        return null;
    }

}