package hasoffer.api.controller;

import hasoffer.base.utils.TimeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created on 2015/12/21.
 */
@Controller
public class SystemController {

    public static void main(String[] args) {
        long dayStart = TimeUtils.getDayStart("2016-10-21", "yyyy-MM-dd");
        System.out.println(dayStart);
        long dayStart1 = TimeUtils.getDayStart("2016-10-22", "yyyy-MM-dd");
        System.out.println(dayStart1);
        long dayStart2 = TimeUtils.getDayStart("2016-10-23", "yyyy-MM-dd");
        System.out.println(dayStart2);
        long dayStart3 = TimeUtils.getDayStart("2016-10-24", "yyyy-MM-dd");
        System.out.println(dayStart3);
        long dayStart4 = TimeUtils.getDayStart("2016-10-25", "yyyy-MM-dd");
        System.out.println(dayStart4);
        long dayStart5 = TimeUtils.getDayStart("2016-10-26", "yyyy-MM-dd");
        System.out.println(dayStart5);
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public ModelAndView config(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("error", "y");

        return mav;
    }

}
