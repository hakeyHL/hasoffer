package hasoffer.api.controller;

import hasoffer.api.helper.Httphelper;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.third.impl.ThirdServiceImple;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by hs on 2016/7/4.
 */
@Controller
@RequestMapping(value = "/third")
public class ThirdPartyController {
    @Resource
    ThirdServiceImple thridPartyService;

    /**
     * provide API to get deals for Gmobi
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/gmobi")
    public String config(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int pageSize,
                         HttpServletResponse response) {
        String result = thridPartyService.getDealsForGmobi(page, pageSize, new String[]{"discount"});
        Httphelper.sendJsonMessage(result, response);
        return null;
    }

    //offer for india

    /**
     * get方式获取deal列表数据
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/india", method = RequestMethod.GET)
    public String getDealsForIndia(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   HttpServletResponse response) {
        String dealsForIndia = thridPartyService.getDealsForIndia(page, pageSize, "originPrice", "presentPrice");
        Httphelper.sendJsonMessage(dealsForIndia, response);
        return null;
    }

    @RequestMapping(value = "/offer/dealInfo/{id}", method = RequestMethod.GET)
    public String getDealsForIndia(@PathVariable("id") String id,
                                   HttpServletResponse response) {
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        String dealInfoForIndia = thridPartyService.getDealInfo(id, deviceInfo.getMarketChannel().name(), deviceId);
        Httphelper.sendJsonMessage(dealInfoForIndia, response);
        return null;
    }

    /**
     * mexico offer list
     *
     * @param page
     * @param pageSize
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/inveno", method = RequestMethod.GET)
    public String getDealsForMexico(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    HttpServletResponse response) {
        String dealsForMexico = thridPartyService.getDealsForInveno(page, pageSize, new String[]{"discount", "originPrice", "presentPrice"});
        Httphelper.sendJsonMessage(dealsForMexico, response);
        return null;
    }
}
