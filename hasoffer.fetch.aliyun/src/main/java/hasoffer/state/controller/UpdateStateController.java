package hasoffer.state.controller;

import hasoffer.state.dmo.UpdateStateDMO;
import hasoffer.state.service.UpdateStateService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/updateState")
public class UpdateStateController {

    @Resource
    private UpdateStateService updateStateService;

    @RequestMapping("/selectUpdateByDay/{queryDay}")
    public List<UpdateStateDMO> selectUpdateByDay(@PathVariable("queryDay") String queryDay) {
        if (queryDay == null) {
            queryDay = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        }
        return updateStateService.selectByDate(queryDay);
    }


}
