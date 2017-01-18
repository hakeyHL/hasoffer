package hasoffer.core.third;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.persistence.po.urm.DeviceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.List;

public class BigDataApi {

    private static final Logger logger = LoggerFactory.getLogger(BigDataApi.class);

    public static DeviceLog getDeviceInfoFromLog(String androidId, Long orderTime) {
        String url = "http://52.77.246.238:8090/device/id/" + androidId;
        HttpResponseModel responseModel = HttpUtils.get(url, null, null);

        int status = responseModel.getStatusCode();
        String responseStr = null;
        switch (status) {
            case HttpURLConnection.HTTP_OK:
                responseStr = responseModel.getBodyString();
                break;
            default:
        }
        DeviceLog lastLog = null;
        if (responseStr == null) {
            logger.error("Can't found this device, android:{}", androidId);
            return lastLog;
        } else {
            try {
                List<DeviceLog> deviceLogs = JSONUtil.toArray(responseStr, DeviceLog.class);
                if (deviceLogs.size() == 0) {
                    return null;
                } else if (deviceLogs.size() == 1) {
                    return deviceLogs.get(0);
                } else {
                    DeviceLog minLog = deviceLogs.get(0);
                    for (DeviceLog log : deviceLogs) {
                        if (minLog.getReqDate() > log.getReqDate()) {
                            minLog = log;
                        }
                        if (log.getReqDate() > orderTime) {
                            continue;
                        }
                        if (lastLog == null) {
                            lastLog = log;
                            continue;
                        }
                        if (lastLog.getReqDate() < log.getReqDate()) {
                            lastLog = log;
                        }
                    }
                    if (lastLog == null) {
                        lastLog = minLog;
                    }
                    for (DeviceLog log : deviceLogs) {
                        if (lastLog.getFirstTimeReq() > log.getFirstTimeReq()) {
                            lastLog.setFirstTimeReq(log.getFirstTimeReq());
                        }
                        if (lastLog.getLastTimeReq() < log.getLastTimeReq()) {
                            lastLog.setLastTimeReq(log.getLastTimeReq());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Can't parse the json, android:{}", androidId);
                return null;
            }
        }
        return lastLog;
    }

    public static void main(String[] args) {
        System.out.println(getDeviceInfoFromLog("e10a826589cd2acd", 1489813666000L));
    }
}
