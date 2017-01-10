package hasoffer.state.controller;

import hasoffer.aliyun.enums.WebSite;
import hasoffer.state.dmo.MatchStateDMO;
import hasoffer.state.service.MatchStateService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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

    @RequestMapping("/selectMatchState")
    @ResponseBody
    public List<MatchStateDMO> selectMatchState(@RequestBody MatchStateDMO matchStateDMO) {
        if (matchStateDMO.getUpdateDate() == null) {
            matchStateDMO.setUpdateDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        }
        return matchStateService.selectStats(matchStateDMO.getUpdateDate(), matchStateDMO.getWebSite());
        //return null;
    }


    @RequestMapping("/selectWebSite")
    @ResponseBody
    public List<WebSite> selectWebSite() {
        return matchStateService.selectWebSite();
    }

}
