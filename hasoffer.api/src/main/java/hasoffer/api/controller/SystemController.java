package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
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
    IRedisListService redisListService;
    @Resource
    ICacheService redisStringService;
    @Resource
    ICacheService<UrmUser> userICacheService;
    @Resource
    AppServiceImpl appService;

    public static void main(String[] args) {
        long dayStart6 = TimeUtils.getDayStart("2016-10-29", "yyyy-MM-dd");
        System.out.println(dayStart6);
        long dayStart7 = TimeUtils.time(2016, 10, 30, 22, 0, 0);
        System.out.println(dayStart7);
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public ModelAndView config(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("error", "y");

        return mav;
    }

    @RequestMapping(value = "app/push/check", method = RequestMethod.GET)
    public ResultVo checkGetPushMsg(@RequestParam(defaultValue = "100") int type, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
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
                jsonObject.put("data", resultVo.getData());
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            case 1:
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        redisListService.pop(PRICEOFFNOTICE_PUSH_PREFIX + currentDate + "_" + urmUser.getId());
                    }
                }
                resultVo.getData().put("pushList", pushList);
                jsonObject.put("data", resultVo.getData());
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            default:
                jsonObject.put("data", resultVo.getData());
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
        }

    }

    @ResponseBody
    @RequestMapping(value = "app/testArray", method = RequestMethod.GET)
    public String checkGetPushMsg(String[] a) {
        System.out.println("1");
        return "ok";
    }
}
