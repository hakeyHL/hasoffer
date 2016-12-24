package hasoffer.admin.controller;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/layout")
public class ShowHomeController {

    @RequestMapping("/showIndex")
    public ModelAndView showMain() {
        return new ModelAndView("layout/index");
    }

    @RequestMapping("/showUpdateGroupByDay")
    public ModelAndView showUpdateGroupByDay() {
        ModelAndView modelAndView = new ModelAndView("stats/updateStatus");
        modelAndView.addObject("queryDate", DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        return modelAndView;
    }


    @RequestMapping("/showUpdateStats")
    public ModelAndView showUpdateStats() {
        ModelAndView modelAndView = new ModelAndView("stats/updateList");
        modelAndView.addObject("queryDate", DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        return modelAndView;
    }


    @RequestMapping("/showProxyIP")
    public ModelAndView showProxyIP() {
        ModelAndView modelAndView = new ModelAndView("proxyIp/proxyIp");
        return modelAndView;
    }



}
