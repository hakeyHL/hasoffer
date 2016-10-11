package hasoffer.admin.controller;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping(value = "/orderStats")
public class OrderController {
    private static Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    @RequestMapping(value = "/updateOrderReport", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView listOrderStats(HttpServletRequest request, @RequestParam(defaultValue = "") String webSite) {


        try {

            Date todayTime = new Date();

            //头一天
            //Date day1AgoTime = TimeUtils.addDay(todayTime, -1);
            //当天
            if (webSite == null || "".equals(webSite) || "ALL".equals(webSite) || Website.SNAPDEAL.name().equals(webSite.toUpperCase())) {
                //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), todayTime, todayTime);
                for (int i = 0; i < 15; i++) {
                    orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), TimeUtils.addDay(todayTime, -i), TimeUtils.addDay(todayTime, -i));
                }
            }
            if (webSite == null || "".equals(webSite) || "ALL".equals(webSite) || Website.FLIPKART.name().equals(webSite.toUpperCase())) {
                for (int i = 0; i < 15; i++) {
                    orderStatsAnalysisService.updateOrder(Website.FLIPKART.name(), TimeUtils.addDay(todayTime, -i), TimeUtils.addDay(todayTime, -i));
                }
            }
        } catch (Exception e) {
            logger.debug("reportOrderStatistic:任务失败,   DATE:" + new Date() + ":具体如下");
            logger.debug(e.toString());
        }
        return new ModelAndView("showstat/listOrderReport");
    }

}
