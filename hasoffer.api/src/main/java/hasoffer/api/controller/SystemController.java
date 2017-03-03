package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.ApiHttpHelper;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.PtmMStdProductService;
import hasoffer.core.product.PtmMStdSkuService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created on 2015/12/21.
 */
@Controller
public class SystemController {
    private static final String PRICEOFFNOTICE_PUSH_PREFIX = "PRICEOFFNOTICE_PUSH_";
    private static final String DEAL_PUSH_PREFIX = "DEAL_PUSH_";
    @Resource
    IDataBaseManager dbm;
    @Resource
    MongoDbManager mongoDbManager;
    @Resource
    PtmMStdSkuService ptmMStdSkuService;
    @Resource
    PtmMStdProductService ptmMStdProductService;
    @Resource
    IRedisListService redisListService;
    @Resource
    ICacheService redisStringService;
    @Resource
    ICacheService<UrmUser> userICacheService;
    @Resource
    AppServiceImpl appService;


    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public ModelAndView config(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("error", "y");

        return mav;
    }

    @RequestMapping(value = "app/push/check", method = RequestMethod.GET)
    public ResultVo checkGetPushMsg(@RequestParam(defaultValue = "100") int type, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        ResultVo resultVo = new ResultVo();
        resultVo.getData().put("have", false);
        String currentDate = TimeUtils.parse(new Date(), "yyyyMMdd");
        List<String> pushList = new ArrayList<>();
        //检查是否有deal推送
        Set dealPushSet = redisStringService.keys(DEAL_PUSH_PREFIX + currentDate + "_*");
        if (dealPushSet != null && dealPushSet.size() > 0) {
            //有deal推送
            Iterator iterator = dealPushSet.iterator();
            while (iterator.hasNext()) {
                String dealKey = (String) iterator.next();
                pushList.add(redisStringService.get(dealKey, 0));
            }
        }
        //检查是否有属于该用户的降价提醒
        UrmUser urmUser = null;
        long size = 0;
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        if (StringUtils.isNotEmpty(userToken)) {
            String key = "user_" + userToken;
            urmUser = userICacheService.get(UrmUser.class, key, 0);
            if (urmUser == null) {
                urmUser = appService.getUserByUserToken(userToken);
                if (urmUser != null) {
                    userICacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
                }
            }
            if (urmUser != null) {
                size = redisListService.size(PRICEOFFNOTICE_PUSH_PREFIX + currentDate + "_" + urmUser.getId());
                if (size > 0) {
                    List<String> range = redisListService.range(PRICEOFFNOTICE_PUSH_PREFIX + currentDate + "_" + urmUser.getId(), 0, size);
                    for (String str : range) {
                        pushList.add(str);
                    }
                }
            }
        }
        if (pushList.size() > 0) {
            //没有,结束
            resultVo.getData().put("have", true);
        }
        switch (type) {
            case 0:
                jsonObject.put(ConstantUtil.API_NAME_DATA, resultVo.getData());
                ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            case 1:
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        redisListService.pop(PRICEOFFNOTICE_PUSH_PREFIX + currentDate + "_" + urmUser.getId());
                    }
                }
                resultVo.getData().put("pushList", pushList);
                jsonObject.put(ConstantUtil.API_NAME_DATA, resultVo.getData());
                ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            default:
                jsonObject.put(ConstantUtil.API_NAME_DATA, resultVo.getData());
                ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
        }

    }

    /**
     * 测试获取web.xml中的entry
     *
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping("jndiTest")
    public String JNDITest(@RequestParam String name) {
        try {
            javax.naming.Context initCtx = new InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            String ParamValue = (String) envCtx.lookup(name);
            System.out.println(name + " :" + ParamValue);
        } catch (NamingException e) {
            System.out.println(e.getMessage());
        }

        return "ok";
    }


   /* @ResponseBody
    @RequestMapping("mspTest")
    public String JNDITest() {
        Spider.create(new IndiaMySmartPricePageProcessor(ptmMStdProductService, mongoDbManager, ptmMStdSkuService))
                .addUrl("http://www.mysmartprice.com/mobile/apple-iphone-6-64gb-msp4882")
                .thread(1)
                .run();
        return "ok";
    }*/
}
