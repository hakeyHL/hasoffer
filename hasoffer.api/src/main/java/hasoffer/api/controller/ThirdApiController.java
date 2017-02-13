package hasoffer.api.controller;

import hasoffer.api.helper.Httphelper;
import hasoffer.core.third.ThirdService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String getTopSkusForNineApp(String page, String pageSize, HttpServletResponse response) {
        String topSkus = thirdService.getTopSkusForNineApps(page, pageSize, null, 0);
        Httphelper.sendJsonMessage(topSkus, response);
        return null;
    }

    //将点击跳转类型为deal且当前时间在生效与失效日期之间的Banner按照创建时间降序返回


}
