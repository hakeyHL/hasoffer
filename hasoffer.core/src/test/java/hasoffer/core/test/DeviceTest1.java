package hasoffer.core.test;

import hasoffer.base.utils.DeviceUtils;
import org.junit.Test;

/**
 * Date : 2016/1/21
 * Function :
 */
public class DeviceTest1 {

    @Test
    public void testDeviceId() {
        String deviceid = "e7d2ad71e239a1ca";
        String imei = "";
        String serialNo = "3300da551736a28d";

        String urmDeviceId = DeviceUtils.getDeviceId(deviceid, imei, serialNo);

        System.out.println(urmDeviceId);
    }

}
