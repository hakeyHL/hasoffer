package hasoffer.job.worker;

import com.aliyuncs.ecs.model.v20140526.ReleaseEipAddressResponse;
import com.aliyuncs.ecs.model.v20140526.UnassociateEipAddressResponse;
import com.aliyuncs.exceptions.ClientException;
import hasoffer.aliyun.api.action.AllocateEipAddressAction;
import hasoffer.aliyun.api.model.EipAddressModel;
import hasoffer.aliyun.api.util.ActionConstant;
import hasoffer.data.redis.IRedisMapService;
import hasoffer.job.dmo.AliVPC;
import hasoffer.job.service.AliVPCLogService;
import hasoffer.job.service.AliVPCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EipTaskWorker implements Runnable {

    private final AliVPC vpc;
    private final AliVPCService aliVPCService;
    private final AliVPCLogService aliVPCLogService;
    private final IRedisMapService<String, String> mapService;
    private Logger logger = LoggerFactory.getLogger(EipTaskWorker.class);

    public EipTaskWorker(AliVPC vpc, AliVPCService aliVPCService, AliVPCLogService aliVPCLogService, IRedisMapService<String, String> mapService) {
        this.vpc = vpc;
        this.aliVPCService = aliVPCService;
        this.aliVPCLogService = aliVPCLogService;
        this.mapService = mapService;
    }

    @Override
    public void run() {
        updateAliServerIp();
    }

    private void updateAliServerIp() {

        //1. 通知服务器，停止抓取,并等待30S，使其完成手头的任务。
        aliVPCService.updateVpcStatus(vpc, false);
        logger.info("Update IP Start. VPC {}, Local IP:{}, OLD Public IP:{}.", vpc.getEcsInstance(), vpc.getPrivateIpAddress(), vpc.getEipIpAddress());
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //2. 解绑IP
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
                UnassociateEipAddressResponse acsResponse = AllocateEipAddressAction.unAssociateEipAddressAction(vpc.getEcsInstance(), vpc.getEipId());
                logger.info("解绑IP 成功：unAssociateEipAddressAction(EcsID:{}, EipID:{}, reqId:{})", vpc.getEcsInstance(), vpc.getEipId(), acsResponse.getRequestId());
                break;
            } catch (ClientException e) {
                logger.error("unAssociateEipAddressAction(EcsID:{}, EipID:{}) fail.", vpc.getEcsInstance(), vpc.getEipId(), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        //3. 释放IP
        while (true) {
            // 休息1S，防止频繁请求，API无法处理
            try {
                TimeUnit.SECONDS.sleep(1);
                ReleaseEipAddressResponse response = AllocateEipAddressAction.releaseEipAddressAction(vpc.getEipId());
                logger.info("释放IP 成功：releaseEipAddressAction(EipID:{}, reqId:{})", vpc.getEipId(), response.getRequestId());
                aliVPCLogService.updateEndTimeLog(new Date(), vpc.getEipId(), response.getRequestId());
                break;
            } catch (ClientException e) {
                logger.error("releaseEipAddressAction(EipID:{}) fail.", vpc.getEipId(), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        //休息90S，在第二个小时启动。
        try {
            TimeUnit.SECONDS.sleep(90);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //4. 申请公网IP
        //公网IP默认20M，费用默认按流量计费
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
                EipAddressModel eipAddressModel = AllocateEipAddressAction.allocateEipAddressAction("20", ActionConstant.RequestInternetChargeType.PayByTraffic);
                vpc.setEipId(eipAddressModel.getAllocationId());
                vpc.setEipIpAddress(eipAddressModel.getEipAddress());
                logger.info("申请公网IP 成功：allocateEipAddressAction(AllocationId:{}, EipAddress:{})", eipAddressModel.getAllocationId(), eipAddressModel.getEipAddress());
                aliVPCService.updateEipInfo(vpc.getEcsInstance(), eipAddressModel);
                aliVPCLogService.insertLog(new Date(), vpc.getEipId(), vpc.getEipIpAddress(), eipAddressModel.getRequestId());
                break;
            } catch (ClientException e) {
                logger.error("allocateEipAddressAction()", e);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        //5. 绑定公网IP
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
                AllocateEipAddressAction.associateEipAddressAction(vpc.getEcsInstance(), vpc.getEipId());
                logger.info("绑定公网IP 成功: associateEipAddressAction(EcsInstance:{}, EipId:{})", vpc.getEcsInstance(), vpc.getEipId());
                break;
            } catch (ClientException e) {
                logger.error("associateEipAddressAction(EcsInstance:{}, EipId:{})", vpc.getEcsInstance(), vpc.getEipId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //6. 通知服务器,IP 更新完毕，可以抓取了。
        mapService.putMap("ALI-VPC-STATUS", vpc.getPrivateIpAddress(), "Y");
        logger.info("Update IP Finish. VPC {}, Local IP:{}, New Public IP:{}.", vpc.getEcsInstance(), vpc.getPrivateIpAddress(), vpc.getEipIpAddress());
    }
}
