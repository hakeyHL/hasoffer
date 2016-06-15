package hasoffer.timer.dynamicip;

import hasoffer.base.utils.TimeUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created on 2016/6/15.
 */
@Component
public class DynamicIp {

    @Scheduled(cron = "0 0/5 * * * ?")
    public void statSkuUpdate() {
        System.out.println(TimeUtils.nowDate());

//        //该实例id为web7-task2的实例id
//        String instanceId = "i-2a01078e";
//
//        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials("AKIAI2KXGSAA6ML4ZSJQ", "vDUeGxdjPeH1ulHark/VhKlAkD4d9L/wVpBINxep");
//        AmazonEC2Client client = new AmazonEC2Client(basicAWSCredentials).withRegion(Regions.AP_SOUTHEAST_1);
//
//        DescribeAddressesResult describeAddressesResult = client.describeAddresses();
//        List<Address> addresses = describeAddressesResult.getAddresses();
//        for (Address address : addresses) {
//
//            if (instanceId.equals(address.getInstanceId())) {
//                //二次验证
//                DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
//                describeInstancesRequest.setInstanceIds(Arrays.asList(instanceId));
//                DescribeInstancesResult describeInstancesResult = client.describeInstances(describeInstancesRequest);
//                if (describeInstancesResult.getReservations().size() != 1) {
//                    throw new RuntimeException();
//                }
//
//                if (describeInstancesResult.getReservations().get(0).getInstances().size() != 1) {
//                    throw new RuntimeException();
//                }
//
//                Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
//                if (!instance.getPublicIpAddress().equals(address.getPublicIp())) {
//                    throw new RuntimeException();
//                }
//
//                //解绑
//                DisassociateAddressRequest disassociateAddressRequest = new DisassociateAddressRequest();
//                disassociateAddressRequest.setAssociationId(address.getAssociationId());
//                client.disassociateAddress(disassociateAddressRequest);
//
//                //释放
//                ReleaseAddressRequest releaseAddressRequest = new ReleaseAddressRequest();
//                releaseAddressRequest.setAllocationId(address.getAllocationId());
//                client.releaseAddress(releaseAddressRequest);
//            }
//        }
//
//        //分配
//        AllocateAddressResult allocateAddressResult = client.allocateAddress();
//
//        //绑定ip
//        AssociateAddressRequest associateAddressRequest = new AssociateAddressRequest();
//        associateAddressRequest.setInstanceId(instanceId);
//        associateAddressRequest.setAllocationId(allocateAddressResult.getAllocationId());
//        AssociateAddressResult associateAddressResult = client.associateAddress(associateAddressRequest);
    }

}
