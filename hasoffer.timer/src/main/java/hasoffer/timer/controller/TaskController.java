package hasoffer.timer.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IAdminCountService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductHelper;
import hasoffer.fetch.model.ListProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created on 2015/12/23.
 */
@Controller
@RequestMapping(value = "/task")
public class TaskController {

    @Resource
    TaskScheduler scheduler;
    @Resource
    ISearchService searchService;
    @Resource
    IAdminCountService deviceService;
    @Resource
    HibernateDao dao;
    private Logger logger = LoggerFactory.getLogger(TaskController.class);

    @RequestMapping(value = "/aggregationDayAlive", method = RequestMethod.GET)
    public
    @ResponseBody
    String aggregationDayAlive() {

        final String DATE_PATTERN_FROM_DB= "yyyy-MM-dd";

        List<String> alives = dao.findBySql("SHOW TABLES LIKE 'stsalive20%';");

        //03-01是起始时间
        Date DayaliveLogsMaxCreateTime = TimeUtils.stringToDate("2016-03-01", DATE_PATTERN_FROM_DB);

        for (String alive : alives) {
            List<Map<String, Object>> dayAlive = dao.findMapBySql("select * from " + alive + " order by wakeupTime desc limit 1");
            if(dayAlive.size()>0) {
                String wakeupTime = dayAlive.get(0).get("wakeupTime").toString();
                if (TimeUtils.stringToDate(wakeupTime, DATE_PATTERN_FROM_DB).getTime() > DayaliveLogsMaxCreateTime.getTime()) {
                    DayaliveLogsMaxCreateTime = TimeUtils.stringToDate(wakeupTime, DATE_PATTERN_FROM_DB);
                }
            }
        }

        deviceService.deviceRequestLogsToAlive(DayaliveLogsMaxCreateTime);
        logger.info("success insert mongodb data into stsAlive ...");

        deviceService.deviceRequestLogsAliveUpdate(DayaliveLogsMaxCreateTime);
        logger.info("success update stsAlive ...");

        List<Map<String,Object>> dayAlive = dao.findMapBySql("select * from StsDayAlive order by date desc limit 1");

        if (dayAlive!= null && dayAlive.size() > 0) {
            DayaliveLogsMaxCreateTime = TimeUtils.stringToDate(dayAlive.get(0).get("date").toString(),DATE_PATTERN_FROM_DB);
        }

        deviceService.cntFromAliveToDayAlive(DayaliveLogsMaxCreateTime);
        logger.info("success insert or update stsDayAlive data...");

        deviceService.cntFromAliveUpdateDayAlive(DayaliveLogsMaxCreateTime);
        logger.info("success  update stsDayAlive data... ");
        return "ok";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public
    @ResponseBody
    String
    listTrigger() {

        return "ok";
    }

    @RequestMapping(value = "/UnmatchedSearchRecordProcessTask", method = RequestMethod.GET)
    public
    @ResponseBody
    String f() {
        logger.debug("UnmatchedSearchRecordProcessTask START");

        /*SysConfig sysConfig = dbm.get(SysConfig.class, 1L);

        Date endTime = TimeUtils.add(sysConfig.getCreateTime(), TimeUtils.MILLISECONDS_OF_1_HOUR * 10);
        FilpkartAffilicateProductProcessor.setToken(sysConfig.getVal());*/

        final int page = 1, size = 100;

        PageableResult<SrmSearchLog> pagedSearchLog = searchService.listNoresultSearchLogs(page, size);

        long total = pagedSearchLog.getTotalPage();

        logger.debug(total + " pages searched[UnmatchedSearchRecordProcessTask].");

        for (int i = 0; i < total; i++) {
            logger.debug("No." + i + " page searched[UnmatchedSearchRecordProcessTask].");

            if (i > 0) {
                pagedSearchLog = searchService.listNoresultSearchLogs(page, size);
            }

            List<SrmSearchLog> searchLogs = pagedSearchLog.getData();

            if (ArrayUtils.hasObjs(searchLogs)) {
                for (SrmSearchLog log : searchLogs) {

                    try {
                        String keyword = log.getKeyword();

                        logger.debug("[UnmatchedSearchRecordProcessTask] - " + keyword);

                        Map<Website, ListProduct> listProductMap = SearchProductHelper.getProducts(log);

                        searchService.relateUnmatchedSearchLog(log, listProductMap);
                    } catch (Exception e) {
                        logger.debug("[UnmatchedSearchRecordProcessTask] ERROR ! - " + e.getMessage());
                    }
                }
            }
        }

        return "ok";
    }

}
