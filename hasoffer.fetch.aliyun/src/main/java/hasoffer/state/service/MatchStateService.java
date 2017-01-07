package hasoffer.state.service;

import hasoffer.state.dao.MatchStateDAO;
import hasoffer.state.dmo.MatchStateDMO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    public List<MatchStateDMO> selectByDate(String queryDay) {
        return matchStateDAO.selectByDate(queryDay);
    }


}
