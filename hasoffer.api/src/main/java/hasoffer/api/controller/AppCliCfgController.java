package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import hasoffer.api.helper.Httphelper;
import hasoffer.core.app.AppClientCfgService;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.redis.impl.CacheServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年10月17日.
 * Time 12:23
 * 客户端配置参数controller
 */
@Controller
@RequestMapping(value = "/cfg")
public class AppCliCfgController {
    final String HOME_REDEEM_TIP_COPY = "app_home_copy";
    @Autowired
    private CacheServiceImpl cacheService;
    private Logger logger = LoggerFactory.getLogger(AppCliCfgController.class);
    @Autowired
    private AppClientCfgService appClientCfgService;

    public static void main(String[] args) {
    }

    @RequestMapping(value = "/app/homeCfg")
    public String homePageRewardsConfig(@RequestParam(defaultValue = "10000") int action,
                                        HttpServletResponse response,
                                        String stringFirst,
                                        String stringSecond) {
        ResultVo resultVo = new ResultVo();
        switch (action) {
            case 1:
                //get home page redeem tip
                String homeRedeemTip = cacheService.get(HOME_REDEEM_TIP_COPY, 0);
                if (!StringUtils.isEmpty(homeRedeemTip)) {
                    List<String> strings = JSONArray.parseArray(homeRedeemTip, String.class);
                    resultVo.getData().put("redeem", strings);
                } else {
                    List<String> strings = Arrays.asList("Get Rs100-1000 Gifts !", "on check-in everyday");
                    resultVo.getData().put("redeem", strings);
                    cacheService.add(HOME_REDEEM_TIP_COPY, JSON.toJSONString(strings), -1);
                }
                break;
            case 2:
                //change home page redeem tip
                if (StringUtils.isNotEmpty(stringFirst) && StringUtils.isNotEmpty(stringSecond)) {
                    List<String> redeemStrings = Arrays.asList(stringFirst, stringSecond);
                    //get home page redeem tip
                    String homeRedeemTip2 = cacheService.get(HOME_REDEEM_TIP_COPY, 0);
                    if (!StringUtils.isEmpty(homeRedeemTip2)) {
                        //delete
                        cacheService.del(HOME_REDEEM_TIP_COPY);
                        cacheService.add(HOME_REDEEM_TIP_COPY, JSON.toJSONString(redeemStrings), -1);
                    } else {
                        //add
                        cacheService.add(HOME_REDEEM_TIP_COPY, JSON.toJSONString(redeemStrings), -1);
                    }
                }
                break;
            case 3:
                //get wishList config
                Map<String, Boolean> map = new HashMap<>();
                //wish
                map.put("001", true);
                //search
                map.put("002", false);

                map.put("003", false);

                resultVo.setData(map);
                break;
            default:
                break;
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(resultVo), response);
        return null;
    }

}
