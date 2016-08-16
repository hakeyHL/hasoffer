package hasoffer.api.helper;

import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.base.utils.HexDigestUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * Date : 2016/3/29
 * Function :
 */
public class ClientHelper {

    private final static String CLIENT_KEY = "13213213213132132132";

    public static String getRequestKey(DeviceInfoVo deviceInfoVo, Map<String, String> requestParams, String uri) {
        StringBuilder sBuilder = new StringBuilder();

        sBuilder.append(uri);
        sBuilder.append(sort4requestKey(requestParams));
        sBuilder.append(deviceInfoVo.getDeviceId());
        sBuilder.append(CLIENT_KEY);
        sBuilder.append(sBuilder.length());

        String keyBeforeMD5 = sBuilder.toString();
//        System.out.println(keyBeforeMD5);

        return HexDigestUtil.md5(keyBeforeMD5).toUpperCase();
    }

    private static String sort4requestKey(Map<String, String> param) {
        String[] keyArray = param.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keyArray) {
            String value = param.get(key);
            if (value != null && !value.equals("")) {
                stringBuilder.append(key).append(param.get(key));
            }
        }
        return stringBuilder.toString();
    }

    public static int returnNumberBetween0And5(Long number) {
        //取得其余数
        Long tempNumber = number % 10;
        if (tempNumber > 0 && tempNumber <= 5) {
            number = (number / 10) * 10 + 5;
        } else if (tempNumber > 5) {
            number = (number / 10) * 10 + 10;
        }
        return number.intValue();
    }

    public static void main(String[] args) {
        int i = ClientHelper.returnNumberBetween0And5(Long.valueOf(0));
        System.out.println(i);
    }
}
