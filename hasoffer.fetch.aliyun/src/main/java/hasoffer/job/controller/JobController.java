package hasoffer.job.controller;

import hasoffer.admin.service.SchedulerServiceImpl;
import hasoffer.spring.quartz.model.QuartzJobInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/job")
public class JobController {

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

    @RequestMapping("/pause")
    public String pauseTrigger(@RequestParam(defaultValue = "") String triggerName) {
        schedulerService.pauseTrigger(triggerName, GROUP);
        return "redirect:/job/showJob";
    }

    @RequestMapping("/resumeTrigger")
    public String resumeTrigger(@RequestParam(defaultValue = "") String triggerName) {
        schedulerService.resumeTrigger(triggerName, GROUP);
        return "redirect:/job/showJob";
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
        return "redirect:/job/showJob";
    }

}
