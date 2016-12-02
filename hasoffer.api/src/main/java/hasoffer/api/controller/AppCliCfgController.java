package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.*;

/**
 * Created by hs on 2016年10月17日.
 * Time 12:23
 * 客户端配置参数controller
 */
@Controller
@RequestMapping(value = "/cfg")
public class AppCliCfgController {
    static final String HOME_REDEEM_TIP_COPY = "app_home_copy";
    static final String HOME_INDEX_COPY = "app_home_index_copy";
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
        Map<String, Boolean> map = new HashMap<>();
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
                logger.info("enter home copy swap ");
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
                logger.info("client scan config ");
                // config
                //search
                map.put("001", false);

                //wishlist
                map.put("002", false);

                //购物车
                map.put("003", false);

                //email and phone get
                map.put("004", false);

                resultVo.setData(map);

                break;
            case 4:
                int flag = new Random().nextInt(100);
                if (flag < 5) {
                    map.put("isBoot", false);
                } else {
                    map.put("isBoot", true);
                }
                resultVo.setData(map);
                break;
            default:
                break;
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(resultVo), response);
        return null;
    }

    @RequestMapping(value = "/app/homeIndex")
    public String homeIndexConfig(HttpServletResponse response,
                                  String stringFirst,
                                  String stringSecond,
                                  String stringThird) {
        ResultVo resultVo = new ResultVo();
        //change home page redeem tip
        logger.info("enter home copy swap ");
        if (StringUtils.isNotEmpty(stringFirst) && StringUtils.isNotEmpty(stringSecond) && StringUtils.isNotEmpty(stringThird)) {
            //set
            List<String> redeemStrings = Arrays.asList(stringFirst, stringSecond, stringThird);
            //get home page redeem tip
            String homeRedeemTip2 = cacheService.get(HOME_INDEX_COPY, 0);
            resultVo.getData().put("bootIndex", redeemStrings);
            if (!StringUtils.isEmpty(homeRedeemTip2)) {
                //delete
                cacheService.del(HOME_INDEX_COPY);
                cacheService.add(HOME_INDEX_COPY, JSON.toJSONString(resultVo.getData()), -1);
            } else {
                //add
                cacheService.add(HOME_INDEX_COPY, JSON.toJSONString(resultVo.getData()), -1);
            }
        } else {
            //get
            String bootIndex = cacheService.get(HOME_INDEX_COPY, 0);
            if (!StringUtils.isEmpty(bootIndex)) {
                try {
                    JSONObject jsonObject = JSON.parseObject(bootIndex);
                    List<String> bootIndex1 = JSONArray.parseArray(jsonObject.getString("bootIndex"), String.class);
                    resultVo.getData().put("bootIndex", bootIndex1);
                } catch (Exception e) {
                    //出现异常时返回默认
                    resultVo.getData().put("bootIndex", Arrays.asList("GET YOUR DAILY COINS!",
                            "100 Coins=1 Rupee!The more often you check in,the more you will earn",
                            "REEDEM COINS FOR SUPER GIFT!"));
                }
            } else {
                resultVo.getData().put("bootIndex", Arrays.asList("GET YOUR DAILY COINS!",
                        "100 Coins=1 Rupee!The more often you check in,the more you will earn",
                        "REEDEM COINS FOR SUPER GIFT!"));
                //add to cache
                cacheService.add(HOME_INDEX_COPY, JSON.toJSONString(resultVo.getData()), -1);
            }
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(resultVo), response);
        return null;
    }

    @RequestMapping(value = "/app/pushCfg")
    public ResultVo appPushConfig() {
        ResultVo resultVo = new ResultVo();
        resultVo.getData().put("open", true);
        resultVo.getData().put("unit", "m");//d 天 h 小时 m 分钟
        resultVo.getData().put("scanInterval", 10);
        return resultVo;
    }
}
