package hasoffer.state.controller;

import hasoffer.state.dmo.MatchStateDMO;
import hasoffer.state.service.MatchStateService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/matchState")
public class MatchStateController {


    @Resource
    private MatchStateService matchStateService;

    @RequestMapping("/selectStateMatchByDay/{queryDay}")
    @ResponseBody
    public List<MatchStateDMO> stateMatch(@PathVariable("queryDay") String queryDay) {
        if (queryDay == null) {
            queryDay = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        }
        return matchStateService.selectStateMatchByDay(queryDay);
    }


}
