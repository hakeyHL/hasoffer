package hasoffer.job.bean;

import com.aliyuncs.ecs.model.v20140526.ReleaseEipAddressResponse;
import com.aliyuncs.ecs.model.v20140526.UnassociateEipAddressResponse;
import hasoffer.aliyun.api.action.AllocateEipAddressAction;
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

public class ReleaseIPJobBean extends QuartzJobBean {

    private Logger logger = LoggerFactory.getLogger(ReleaseIPJobBean.class);

    @Resource
    private AliVPCService aliVPCService;

    @Resource
    private AliVPCLogService aliVPCLogService;

    @Resource
    private IRedisMapService<String, String> mapService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //1. 向所有的服务器发送IP失效,暂停抓取服务；
        List<AliVPC> aliVPCList = aliVPCService.queryAllVPCList();
        for (AliVPC vpc : aliVPCList) {
            mapService.putMap("ALI-VPC-STATUS", vpc.getPrivateIpAddress(), "N");
        }

        // 休息20S，防止频繁请求，API无法处理
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //2. 解绑IP
        for (AliVPC vpc : aliVPCList) {
            UnassociateEipAddressResponse acsResponse = AllocateEipAddressAction.unAssociateEipAddressAction(vpc.getEcsInstance(), vpc.getEipId());
            logger.info("unAssociateEipAddressAction(EcsID:{}, EipID:{}, reqId:{})", vpc.getEcsInstance(), vpc.getEipId(), acsResponse.getRequestId());
        }

        // 休息5S，防止频繁请求，API无法处理
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //3. 释放IP
        for (AliVPC vpc : aliVPCList) {
            ReleaseEipAddressResponse response = AllocateEipAddressAction.releaseEipAddressAction(vpc.getEipId());
            logger.info("releaseEipAddressAction(eipId:{}, reqId:{})", vpc.getEipId(), response.getRequestId());
            aliVPCLogService.updateEndTimeLog(new Date(), vpc.getEipId(), response.getRequestId());
        }

    }

}
