package hasoffer.core.admin;

import hasoffer.base.model.PageableResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/4/7.
 */
public interface IAdminCountService {

    /**
     * 查看日期存活设备总数
     * @param baseDate
     * @return
     */
    int totalAliveDevice(String baseDate);

    /**
     * 查看日期比价设备总数
     * @param baseDate
     * @return
     */
    int totalRatioDevice(String baseDate);

    PageableResult<Map<String ,Object>> findStsAlive(Map<String, String> params, int page, int size);

    void cntFromAliveToDayAlive(Date logsMaxCreateTime);

    void cntFromAliveUpdateDayAlive(Date logsMaxCreateTime);

    List<String> findTables();

    List<Map<String, Object>> getWakeupTime(String alive);

    List<Map<String, Object>> findStsDayAlive();

    List<String> getAliveTables();

    List<Map<String, Object>> findDataMapsBySQL(String sql);

    void deviceRequestLogsToAlive(Date logsMaxCreateTime);

    void deviceRequestLogsAliveUpdate(Date logsMaxCreateTime);
}
