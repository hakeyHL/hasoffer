package hasoffer.timer.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.UrmDeviceBuyLog;
import hasoffer.core.user.IDeviceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created on 2016/3/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class RequestLogToBuyLogTest {

    private static Logger logger = LoggerFactory.getLogger(RequestLogToBuyLogTest.class);

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDeviceService deviceService;

    @Test
    public void testRequestLogToBuyLog() {

        final String DATE_PATTERN_FROM_WEB = "MM/dd/yyyy";

        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "createTime"));

        PageableResult<UrmDeviceBuyLog> result = mdm.queryPage(UrmDeviceBuyLog.class, query, 1, Integer.MAX_VALUE);

        Date buyLogsMaxCreateTime = TimeUtils.stringToDate("03/25/2016", DATE_PATTERN_FROM_WEB);

//        if (result != null && result.getData().size() > 0) {
//            buyLogsMaxCreateTime = result.getData().get(0).getCreateTime();
//        }

        deviceService.deviceRequestLogsAnalysis(buyLogsMaxCreateTime);

    }

}
