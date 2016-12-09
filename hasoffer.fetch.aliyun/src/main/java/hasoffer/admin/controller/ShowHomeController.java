package hasoffer.admin.controller;

import hasoffer.admin.service.SchedulerServiceImpl;
import hasoffer.spring.quartz.model.QuartzJobInfo;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/layout")
public class ShowHomeController {

    private static final String GROUP = "DEFAULT";

    @Autowired
    private SchedulerServiceImpl schedulerService;

    @RequestMapping("/showJob")
    public ModelAndView showJob() {
        ModelAndView mv = new ModelAndView("layout/mainFrame");
        List<QuartzJobInfo> infos = schedulerService.getQrtzTriggers();
        mv.addObject("infos", infos);
        return mv;
    }

    @RequestMapping("/showIndex")
    public ModelAndView showMain() {
        return new ModelAndView("layout/index");
    }

    @RequestMapping("/showUpdateStats")
    public ModelAndView showUpdateStats() {
        ModelAndView modelAndView = new ModelAndView("stats/updateStatus");
        modelAndView.addObject("queryDate", DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        return modelAndView;
    }


    @RequestMapping("/pause")
    public String pauseTrigger(@RequestParam(defaultValue = "") String triggerName) {
        schedulerService.pauseTrigger(triggerName, GROUP);
        return "redirect:/layout/showJob";
    }

    @RequestMapping("/resumeTrigger")
    public String resumeTrigger(@RequestParam(defaultValue = "") String triggerName) {
        schedulerService.resumeTrigger(triggerName, GROUP);
        return "redirect:/layout/showJob";
    }

    //@RequestMapping("/shutDownNow")
    //public String breakTrigger(@RequestParam(defaultValue = "") String triggerName) {
    //    if(triggerName.contains("fetchTrigger")){
    //        webSiteFetchService.shutDown();
    //    }
    //    return "redirect:/layout/showHome";
    //}
    //
    //
    @RequestMapping("/runNow")
    public String runNow(@RequestParam(defaultValue = "") String triggerName, @RequestParam(defaultValue = "") String targetBizDate, @RequestParam(defaultValue = "") String officeId) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (targetBizDate != null && targetBizDate.trim().length() > 0) {
            data.put("targetBizDate", targetBizDate);
        }
        if (officeId != null && officeId.trim().length() > 0) {
            data.put("officeId", officeId);
        }

        try {
            schedulerService.runNow(triggerName, GROUP, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/layout/showJob";
    }


}
