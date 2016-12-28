package hasoffer.state.controller;

import hasoffer.aliyun.enums.WebSite;
import hasoffer.state.dmo.UpdateStateDMO;
import hasoffer.state.service.UpdateStateService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/updateState")
public class UpdateStateController {

    @Resource
    private UpdateStateService updateStateService;

    @RequestMapping("/selectUpdateByDay/{queryDay}")
    @ResponseBody
    public List<UpdateStateDMO> selectUpdateByDay(@PathVariable("queryDay") String queryDay) {
        if (queryDay == null) {
            queryDay = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        }
        return updateStateService.selectByDate(queryDay);
    }

    @RequestMapping(value = "/selectUpdateStats", method = RequestMethod.POST)
    @ResponseBody
    public List<UpdateStateDMO> selectUpdateStats(@RequestBody UpdateStateDMO updateStateDMO) {
        if (updateStateDMO.getUpdateDate() == null) {
            updateStateDMO.setUpdateDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        }
        return updateStateService.selectStats(updateStateDMO.getUpdateDate(), updateStateDMO.getTaskTarget(), updateStateDMO.getWebSite());
    }

    @RequestMapping("/selectTaskTarget")
    @ResponseBody
    public List<UpdateStateService.TaskTarget> selectTaskTarget() {
        return updateStateService.selectTaskTarget();
    }

    @RequestMapping("/selectWebSite")
    @ResponseBody
    public List<WebSite> selectWebSite() {
        return updateStateService.selectWebSite();
    }

}
