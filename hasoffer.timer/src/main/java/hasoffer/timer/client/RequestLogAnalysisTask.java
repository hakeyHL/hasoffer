package hasoffer.timer.client;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.UrmDeviceBuyLog;
import hasoffer.core.user.IDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created on 2016/3/30.
 */
@Component
public class RequestLogAnalysisTask {

    private static Logger logger = LoggerFactory.getLogger(RequestLogAnalysisTask.class);

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDeviceService deviceService;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void requestLogAnalysis() {

        logger.debug("-------------------analysis requestlog task start--------------------------");

        final String DATE_PATTERN_FROM_WEB = "MM/dd/yyyy";

        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "createTime"));

        PageableResult<UrmDeviceBuyLog> result = mdm.queryPage(UrmDeviceBuyLog.class, query, 1, 5);

        Date buyLogsMaxCreateTime = TimeUtils.stringToDate("03/01/2016", DATE_PATTERN_FROM_WEB);

        if (result != null && result.getData().size() > 0) {
            buyLogsMaxCreateTime = result.getData().get(0).getCreateTime();

        }

        logger.debug("analysis requestlog get startTime" + buyLogsMaxCreateTime.toString());
        deviceService.deviceRequestLogsAnalysis(buyLogsMaxCreateTime);
    }

}
