package hasoffer.api.controller;

import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.api.controller.vo.SearchIO;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2016/7/8.
 */
@Controller
@RequestMapping("/app/common/")
public class AppUserController {
    @Resource
    AppServiceImpl appService;

    @RequestMapping("/addUserId2DeepLink")
    public ModelAndView get(@RequestParam String deepLink, String website) {
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        SearchIO sio = new SearchIO("", "", "", website, "", deviceInfo.getMarketChannel(), deviceId, 0, 0);
        UrmUser urmUser = appService.getUserByUserToken((String) Context.currentContext().get(StaticContext.USER_TOKEN));
        String affs[] = null;
        if (urmUser != null) {
            affs = new String[]{sio.getMarketChannel().name(), sio.getDeviceId(), urmUser.getId() + ""};
        } else {
            affs = new String[]{sio.getMarketChannel().name(), sio.getDeviceId()};
        }
        String affsUrl = WebsiteHelper.getUrlWithAff(Website.valueOf(website), deepLink, affs);
        ModelAndView modelAndView = new ModelAndView();
        Map map = new HashMap();
        map.put("deeplink", affsUrl);
        modelAndView.addObject("data", map);
        return modelAndView;
    }
}
