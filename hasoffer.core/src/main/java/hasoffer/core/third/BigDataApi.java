package hasoffer.core.third;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.persistence.po.urm.DeviceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BigDataApi {

    private static final Logger logger = LoggerFactory.getLogger("hasoffer.affiliate.order");

    public static DeviceLog getDeviceInfoFromLog(String androidId, Long orderTime) {
        String url = "http://172.31.0.155:8090/device/id/" + androidId;
        logger.info("Get device, android:{} Info begin.", androidId);
        HttpResponseModel responseModel = HttpUtils.get(url, null, null);
        logger.info("Get device, android:{} Info End.", androidId);
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
                List<DeviceLog> deviceTempLogs = JSONUtil.toArray(responseStr, DeviceLog.class);
                List<DeviceLog> deviceLogs = new ArrayList<>();
                Long minDateLong = 1451577600000L;
                Long maxDateLong = 1514736000000L;
                for (DeviceLog log : deviceTempLogs) {
                    if (log.getReqDate() < minDateLong || log.getFirstTimeReq() < minDateLong || log.getLastTimeReq() < minDateLong) {
                        continue;
                    }
                    if (log.getReqDate() > maxDateLong || log.getFirstTimeReq() > maxDateLong || log.getLastTimeReq() > maxDateLong) {
                        continue;
                    }
                    deviceLogs.add(log);
                }
                if (deviceLogs.size() == 0) {
                    return null;
                } else if (deviceLogs.size() == 1) {
                    return deviceLogs.get(0);
                } else {
                    for (DeviceLog log : deviceLogs) {
                        if (log.getReqDate() < 0) {
                            continue;
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
                    if (lastLog != null) {
                        for (DeviceLog log : deviceLogs) {
                            if (lastLog.getFirstTimeReq() > log.getFirstTimeReq()) {
                                lastLog.setFirstTimeReq(log.getFirstTimeReq());
                            }
                            if (lastLog.getLastTimeReq() < log.getLastTimeReq()) {
                                lastLog.setLastTimeReq(log.getLastTimeReq());
                            }
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
        //for (int i = 0; i < 1000; i++) {
        //    System.out.println(getDeviceInfoFromLog("7168377460d76302", 1489813666000L));
        //    System.out.println(getDeviceInfoFromLog("a2dccd0e66aab3dd", 1489813666000L));
        //    System.out.println(getDeviceInfoFromLog("4e286dfbac3a0824", 1489813666000L));
        //    System.out.println(getDeviceInfoFromLog("3ed971e5a439285f", 1489813666000L));
        //    System.out.println(getDeviceInfoFromLog("c09b61d9d7787d9", 1489813666000L));
        //}
        System.out.println(new Date(-947712482000L));
        //System.out.println(new Date(159268439894000L));
        //System.out.println(new Date(1483210218000L));
        //System.out.println(new Date(1484593685000L));
        //System.out.println(new Date(Long.MAX_VALUE));
        //try {
        //    System.out.println(DateUtils.parseDate("2016-01-01", "yyyy-MM-dd").getTime());
        //    System.out.println(DateUtils.parseDate("2018-01-01", "yyyy-MM-dd").getTime());
        //} catch (ParseException e) {
        //    e.printStackTrace();
        //}
    }
}
