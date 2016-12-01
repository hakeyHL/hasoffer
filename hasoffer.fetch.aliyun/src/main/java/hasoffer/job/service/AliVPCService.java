package hasoffer.job.service;

import hasoffer.aliyun.api.model.EipAddressModel;
import hasoffer.job.dao.AliVPCDAO;
import hasoffer.job.dmo.AliVPC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AliVPCService {

    @Resource
    private AliVPCDAO aliVPCDAO;

    public List<AliVPC> queryAllVPCList() {
        return aliVPCDAO.queryAllVPCList();
    }

    public void updateEipInfo(String ecsInstance, EipAddressModel eipAddressModel) {
        aliVPCDAO.updateEipInfo(ecsInstance, eipAddressModel);
    }
}
