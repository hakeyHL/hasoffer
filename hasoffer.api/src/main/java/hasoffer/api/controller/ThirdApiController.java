package hasoffer.api.controller;

import hasoffer.api.helper.ApiHttpHelper;
import hasoffer.api.helper.ClientHelper;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.third.ThirdService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by hs on 2017年02月13日.
 * Time 16:25
 */
@Controller
@RequestMapping("third/api")
public class ThirdApiController {
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


}
