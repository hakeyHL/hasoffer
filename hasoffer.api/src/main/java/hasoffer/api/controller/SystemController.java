package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.bo.push.AppMsgClick;
import hasoffer.core.bo.push.AppMsgClickType;
import hasoffer.core.bo.push.AppMsgDisplay;
import hasoffer.core.bo.push.AppPushMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2015/12/21.
 */
@Controller
public class SystemController {


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
        resultVo.getData().put("have", true);
        List<String> msgList = new ArrayList<>();
        //如果查询到才设置有
        AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("Nova NHT 1047 Cordless Trimmer For Men (Orange) 335.0 , click to view details.  ", "Nova NHT 1047 Cordless Trimmer For Men (Orange) 335.0", "Nova NHT 1047 Cordless Trimmer For Men (Orange) 335.0", "http://img1.hasofferimage.com/2016/1202/2010/00/2909.jpg"),
                new AppMsgClick(AppMsgClickType.DEAL, "99070016", "com.flipkart.android")
        );
       /* AppPushMessage message2 = new AppPushMessage(
                new AppMsgDisplay("price off alert ", "alert you ", "let us alert "),
                new AppMsgClick(AppMsgClickType.DEEPLINK, "http://affiliateshopclues.com/?a=2892&c=69&p=r&s1=&ckmrdr=http://www.shopclues.com/apple-iphone-6s-16gb-26.html", "com.flipkart.android")
        );
*/
        msgList.add(JSON.toJSONString(message));
//        msgList.add(JSON.toJSONString(message2));
        resultVo.getData().put("pushList", msgList);
        jsonObject.put("data", resultVo.getData());
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }
}
