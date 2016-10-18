package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import hasoffer.api.controller.vo.ResultVo;
import hasoffer.api.helper.Httphelper;
import hasoffer.core.app.AppClientCfgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Created by hs on 2016年10月17日.
 * Time 12:23
 * 客户端配置参数controller
 */
@Controller
@RequestMapping(value = "/cfg")
public class AppCliCfgController {
    private Logger logger = LoggerFactory.getLogger(AppCliCfgController.class);
    @Autowired
    private AppClientCfgService appClientCfgService;

    public static void main(String[] args) {
    }

    @RequestMapping(value = "/app/homeCfg")
    public String homePageRewardsConfig(@RequestParam(defaultValue = "10000") int action, HttpServletResponse response) {
        ResultVo resultVo = new ResultVo();
        switch (action) {
            case 1:
                //get home page redeem tip
                resultVo.getData().put("redeem", Arrays.asList("Earn Hasoffer Coin Everyday!", "Redeem Rs100-1000 Amazon Gift Card"));
                break;
            default:
                break;
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(resultVo), response);
        return null;
    }

}
