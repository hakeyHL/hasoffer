package hasoffer.job.service;

import hasoffer.aliyun.api.model.EipAddressModel;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.job.dao.AliVPCDAO;
import hasoffer.job.dmo.AliVPC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AliVPCService {

    @Resource
    private AliVPCDAO aliVPCDAO;

    @Resource
    private IRedisMapService<String, String> mapService;

    public List<AliVPC> queryAllVPCList() {
        return aliVPCDAO.queryAllVPCList();
    }

    public void updateEipInfo(String ecsInstance, EipAddressModel eipAddressModel) {
        aliVPCDAO.updateEipInfo(ecsInstance, eipAddressModel);
    }

    public void updateVpcStatus(AliVPC vpc, boolean status) {
        mapService.putMap("ALI-VPC-STATUS", vpc.getPrivateIpAddress(), status ? "Y" : "N");
    }
}
