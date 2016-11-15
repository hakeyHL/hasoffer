package hasoffer.datafixes.order.work;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;

public class OrderFixWorker {

    private static final Logger logger = LoggerFactory.getLogger(OrderFixWorker.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    public void runTask(Date startDate, Date endDate) {
        for (Date day = startDate; day.compareTo(endDate) <= 0; day = TimeUtils.addDay(day, 1)) {
            logger.info("OrderFixWorker date:{} start.");
            doTask(day, day);
            logger.info("OrderFixWorker date:{} end.");
        }
    }

    private void doTask(Date startDate, Date endDate) {
        // 由于flipkart订单api存在问题，只能一次一天一天的取。目前不确定flipkart何时能够修改该问题。
        //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), startDate, endDate);
        orderStatsAnalysisService.updateOrder(Website.FLIPKART.name(), startDate, endDate);
    }

}
