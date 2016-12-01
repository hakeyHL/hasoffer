package hasoffer.job.bean;

import hasoffer.aliyun.api.action.AllocateEipAddressAction;
import hasoffer.aliyun.api.model.EipAddressModel;
import hasoffer.aliyun.api.util.ActionConstant;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.job.dmo.AliVPC;
import hasoffer.job.service.AliVPCLogService;
import hasoffer.job.service.AliVPCService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AssociateEipJobBean extends QuartzJobBean {

    private Logger logger = LoggerFactory.getLogger(AssociateEipJobBean.class);

    @Resource
    private AliVPCService aliVPCService;

    @Resource
    private AliVPCLogService aliVPCLogService;

    @Resource
    private IRedisMapService<String, String> mapService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        List<AliVPC> aliVPCList = aliVPCService.queryAllVPCList();

        for (AliVPC vpc : aliVPCList) {
            EipAddressModel eipAddressModel = AllocateEipAddressAction.allocateEipAddressAction("20", ActionConstant.RequestInternetChargeType.PayByTraffic);
            vpc.setEipId(eipAddressModel.getAllocationId());
            vpc.setEipIpAddress(eipAddressModel.getEipAddress());
            logger.info("allocateEipAddressAction(AllocationId:{}, EipAddress:{})", eipAddressModel.getAllocationId(), eipAddressModel.getEipAddress());
            aliVPCService.updateEipInfo(vpc.getEcsInstance(), eipAddressModel);
            aliVPCLogService.insertLog(new Date(), vpc.getEipId(), vpc.getEipIpAddress(), eipAddressModel.getRequestId());
        }

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (AliVPC vpc : aliVPCList) {
            mapService.putMap("alivpc-status", vpc.getPrivateIpAddress(), "Y");
            //AllocateEipAddressAction.associateEipAddressAction(vpc.getEcsInstance(), vpc.getEipId());
            logger.info("associateEipAddressAction(EcsInstance:{},EipId)", vpc.getEcsInstance(), vpc.getEipId());

        }

    }
}
