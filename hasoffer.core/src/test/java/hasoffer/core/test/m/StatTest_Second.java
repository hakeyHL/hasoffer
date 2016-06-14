package hasoffer.core.test.m;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.StatDayAlive;
import hasoffer.core.persistence.mongo.StatDevice;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import hasoffer.core.worker.ListNeedUpdateBindAssistYmdWorker;
import hasoffer.core.worker.UpdateBindAssistYmdWorker;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/3/30
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class StatTest_Second {


    @Resource
    IDeviceService deviceService;

    @Resource
    IMongoDbManager mdm;

    private Logger logger = LoggerFactory.logger(StatTest_Second.class);

    @Test
    public void showByYMD() {
        String ymd = "20160502";
        Map<String, StatDayAlive> dayAliveMap = new HashMap<String, StatDayAlive>();//getInitMap(ymd, brandSet, osSet, mcSet);

        int page = 1, PAGE_SIZE = 2000;

        PageableResult<StatDevice> pagedStatDevices = deviceService.listPagedStatDevice(ymd, page, PAGE_SIZE);
        List<StatDevice> statDevices = pagedStatDevices.getData();

        long totalPage = pagedStatDevices.getTotalPage();

        int total = 0;
        AtomicInteger passCount = new AtomicInteger(0);

        while (page <= totalPage) {

            logger.debug("page = " + page + "/" + totalPage);

            if (page > 1) {
                statDevices = deviceService.listStatDevice(ymd, page, PAGE_SIZE);
            }

            for (StatDevice sd : statDevices) {
                total++;

                if (sd.getDeviceYmd().equals(sd.getYmd())) {
                    if (sd.getWakeUp() > 0 && sd.getBindAssist() <= 0) {
                        logger.debug(sd.toString());
                    }
                }

                if (total % 500 == 0) {
                    logger.debug(String.format("stat %d devices. %d passed.", total, passCount.get()));
                }
            }

            page++;
        }

    }

    @Test
    public void testStat() {
        List<String> ymds = new ArrayList<String>();
        TimeUtils.fillDays(ymds, "20160405", "20160405", TimeUtils.PATTERN_YMD);

        ListAndProcessWorkerStatus<StatDevice> ws = new ListAndProcessWorkerStatus<StatDevice>();
        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new ListNeedUpdateBindAssistYmdWorker(mdm, ws, ymds));
        for (int i = 0; i < 10; i++) {
            es.execute(new UpdateBindAssistYmdWorker(deviceService, ws));
        }

        while (true) {
            if (ws.isListWorkFinished() && ws.getSdQueue().size() == 0) {
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
            }
            System.out.println("queue size : " + ws.getSdQueue().size());
        }

        System.out.println("work finished.");
    }
}

