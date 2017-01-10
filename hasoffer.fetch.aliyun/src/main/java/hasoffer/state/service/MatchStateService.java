package hasoffer.state.service;

import hasoffer.aliyun.enums.WebSite;
import hasoffer.state.dao.MatchStateDAO;
import hasoffer.state.dmo.MatchStateDMO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
public class MatchStateService {

    @Resource
    private MatchStateDAO matchStateDAO;


    public void insert(MatchStateDMO dmo) {
        matchStateDAO.insert(dmo);
    }


    public void update(MatchStateDMO dmo) {
        matchStateDAO.update(dmo);
    }

    public List<MatchStateDMO> selectStats(String queryDay, String webSite) {
        return matchStateDAO.selectStats(queryDay, webSite);
    }


    public List<WebSite> selectWebSite() {
        return Arrays.asList(new WebSite[]{WebSite.AMAZON, WebSite.FLIPKART, WebSite.SHOPCLUES, WebSite.SNAPDEAL});
    }

}
