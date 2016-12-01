package hasoffer.job.bean;

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

        //2. 解绑IP，释放IP
        for (AliVPC vpc : aliVPCList) {
            mapService.putMap("alivpc-status", vpc.getPrivateIpAddress(), "N");
            //AllocateEipAddressAction.unAssociateEipAddressAction(vpc.getEcsInstance(), vpc.getEipId());
            logger.info("unAssociateEipAddressAction(EcsID:{}, EipID:{})", vpc.getEcsInstance(), vpc.getEipId());
        }

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (AliVPC vpc : aliVPCList) {
            String reqId = AllocateEipAddressAction.releaseEipAddressAction(vpc.getEipId());
            logger.info("releaseEipAddressAction({})", vpc.getEipId());
            aliVPCLogService.updateEndTimeLog(new Date(), vpc.getEipId(), reqId);
        }

    }

}
