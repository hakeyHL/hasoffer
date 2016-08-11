package hasoffer.admin.controller;

import hasoffer.core.push.IPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by lihongde on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value = "/push")
public class PushController {

    @Resource
    IPushService pushService;
    private Logger logger = LoggerFactory.getLogger(PushController.class);


    @RequestMapping(value = "/pushMessage", method = RequestMethod.POST)
    public ModelAndView listDealData(HttpServletRequest request, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size) {
        ModelAndView mav = new ModelAndView("deal/list");
        pushService.sendPush(page, size);
        return mav;
    }
}
