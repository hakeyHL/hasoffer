package hasoffer.job.bean.order;

import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

public class OrderSCJobBean extends QuartzJobBean {
    private static Logger logger = LoggerFactory.getLogger(OrderSCJobBean.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("OrderSCJobBean job start.");

        try {

            Date date = new Date();
            Date targetDate = DateUtils.parseDate("2016-11-21 12:00:00", "yyyy-MM-dd HH:mm:ss");
            Long x = (targetDate.getTime() - date.getTime()) / (1000 * 60 * 60);
            logger.info("OrderSCJobBean x={}", x);
            Date start8mTime = DateUtils.parseDate("2016-08-01", "yyyy-MM-dd");
            Date end8mTime = DateUtils.parseDate("2016-08-31", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start8mTime, end8mTime, 552120, x);

            Date start9mTime = DateUtils.parseDate("2016-09-01", "yyyy-MM-dd");
            Date end9mTime = DateUtils.parseDate("2016-09-30", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start9mTime, end9mTime, 622120, x);

            Date start10mTime = DateUtils.parseDate("2016-10-01", "yyyy-MM-dd");
            Date end10mTime = DateUtils.parseDate("2016-10-31", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start10mTime, end10mTime, 521200, x);

        } catch (Exception e) {
            logger.error("reportOrderStatistic: update order fail. DATE:" + new Date() + ": msg:", e);
        }

        logger.info("OrderSCJobBean job end.");

    }

}
