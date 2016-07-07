package hasoffer.core.test;

import org.junit.Test;

/**
 * Date : 2016/1/21
 * Function :
 */
public class DeviceTest1 {

    @Test
    public void testDeviceId() {
        String deviceid = "\"{\"screenSize\":\"4.6647615649423795\",\"otherApp\":[\"VOODOO\"],\"ramSize\":\"2118651904\",\"screen\":\"1184x768\",\"osVersion\":\"4.4.4\",\"curNetState\":\"wifi\",\"mac\":\"08:00:27:7d:5c:7e\",\"appCount\":\"14\",\"deviceName\":\"Genymotion Google Nexus 4 - 4.4.4 - API 19 - 768x1280_1 vbox86p\",\"appVersion\":\"8\",\"shopApp\":[\"SHOPCLUES\",\"ASKMEBAZAAR\",\"FLIPKART\",\"SNAPDEAL\",\"AMAZON\"],\"brand\":\"generic\",\"imeiId\":\"000000000000008\",\"marketChannel\":\"GOOGLEPLAY\",\"appType\":\"APP\",\"serial\":\"unknown\",\"deviceId\":\"b595aca2c906f99\"}\"";

        System.out.println(deviceid);
    }

}
