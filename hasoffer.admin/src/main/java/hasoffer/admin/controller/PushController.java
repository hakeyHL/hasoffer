package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.PushVo;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.core.push.IPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value = "/push")
public class PushController {

    @Resource
    IPushService pushService;
    private Logger logger = LoggerFactory.getLogger(PushController.class);
    private Object allAppType;


    @RequestMapping(value = "/pushIndex")
    public ModelAndView PushIndex() {
        ModelAndView mav = new ModelAndView("push/push");
        List<MarketChannel> channles = pushService.getAllMarketChannels();
        List<Website> websites = new ArrayList<>();
        Class classzz = Website.class;
        for (Object o : classzz.getEnumConstants()) {
            websites.add((Website) o);
        }
        mav.addObject("channels", channles);
        mav.addObject("websites", websites);
        return mav;
    }

    @RequestMapping(value = "/y")
    public ModelAndView PushMessage(PushVo pushVol) {
        //app类型
        //渠道
        //
        ModelAndView mav = new ModelAndView("push/push");

        return mav;
    }
}
