package hasoffer.timer.dynamicip;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 2016/6/15.
 */
@Component
public class DynamicIp {

    private Logger logger = LoggerFactory.getLogger(DynamicIp.class);

    @Scheduled(cron = "0 0/2 * * * ?")
    public void statSkuUpdate() {

        //该实例id为web7-task2的实例id
        String instanceId = "i-2a01078e";

        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials("AKIAI2KXGSAA6ML4ZSJQ", "vDUeGxdjPeH1ulHark/VhKlAkD4d9L/wVpBINxep");
        AmazonEC2Client client = new AmazonEC2Client(basicAWSCredentials).withRegion(Regions.AP_SOUTHEAST_1);

        DescribeAddressesResult describeAddressesResult = client.describeAddresses();
        List<Address> addresses = describeAddressesResult.getAddresses();
        for (Address address : addresses) {

            if (instanceId.equals(address.getInstanceId())) {
                //二次验证
                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                describeInstancesRequest.setInstanceIds(Arrays.asList(instanceId));
                DescribeInstancesResult describeInstancesResult = client.describeInstances(describeInstancesRequest);
                if (describeInstancesResult.getReservations().size() != 1) {
                    throw new RuntimeException();
                }

                if (describeInstancesResult.getReservations().get(0).getInstances().size() != 1) {
                    throw new RuntimeException();
                }

                Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
                if (!instance.getPublicIpAddress().equals(address.getPublicIp())) {
                    throw new RuntimeException();
                }
                logger.debug("origin ip:" + instance.getPublicIpAddress());

                //解绑
                DisassociateAddressRequest disassociateAddressRequest = new DisassociateAddressRequest();
                disassociateAddressRequest.setAssociationId(address.getAssociationId());
                client.disassociateAddress(disassociateAddressRequest);

                //释放
                ReleaseAddressRequest releaseAddressRequest = new ReleaseAddressRequest();
                releaseAddressRequest.setAllocationId(address.getAllocationId());
                client.releaseAddress(releaseAddressRequest);
            }
        }

        //分配
        AllocateAddressResult allocateAddressResult = client.allocateAddress();

        //绑定ip
        AssociateAddressRequest associateAddressRequest = new AssociateAddressRequest();
        associateAddressRequest.setInstanceId(instanceId);
        associateAddressRequest.setAllocationId(allocateAddressResult.getAllocationId());
        AssociateAddressResult associateAddressResult = client.associateAddress(associateAddressRequest);
        logger.debug("new ip:" + associateAddressRequest.getPublicIp());
    }

}
