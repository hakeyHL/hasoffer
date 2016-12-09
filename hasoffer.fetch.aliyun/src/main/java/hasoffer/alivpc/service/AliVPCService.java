package hasoffer.alivpc.service;

import hasoffer.alivpc.dao.AliVPCDAO;
import hasoffer.alivpc.dmo.AliVPCDMO;
import hasoffer.aliyun.api.model.EipAddressModel;
import hasoffer.data.redis.IRedisMapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AliVPCService {

    @Resource
    private AliVPCDAO aliVPCDAO;

    @Resource
    private IRedisMapService<String, String> mapService;

    public List<AliVPCDMO> queryAllVPCList() {
        return aliVPCDAO.queryAllVPCList();
    }

    public void updateEipInfo(String ecsInstance, EipAddressModel eipAddressModel) {
        aliVPCDAO.updateEipInfo(ecsInstance, eipAddressModel);
    }

    public void updateVpcStatus(AliVPCDMO vpc, boolean status) {
        mapService.putMap("ALI-VPC-STATUS", vpc.getPrivateIpAddress(), status ? "Y" : "N");
    }
}
