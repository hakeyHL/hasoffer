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
            Date targetDate08 = DateUtils.parseDate("2016-11-22 23:00:00", "yyyy-MM-dd HH:mm:ss");
            Long x08 = (targetDate08.getTime() - date.getTime()) / (1000 * 60 * 60);
            logger.info("OrderSCJobBean x08={}", x08);
            Date start8mTime = DateUtils.parseDate("2016-08-31", "yyyy-MM-dd");
            Date end8mTime = DateUtils.parseDate("2016-09-01", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start8mTime, end8mTime, 32120, x08);

            Date targetDate09 = DateUtils.parseDate("2016-12-15 23:00:00", "yyyy-MM-dd HH:mm:ss");
            Long x09 = (targetDate09.getTime() - date.getTime()) / (1000 * 60 * 60);
            logger.info("OrderSCJobBean x09={}", x09);
            Date start9mTime = DateUtils.parseDate("2016-09-01", "yyyy-MM-dd");
            Date end9mTime = DateUtils.parseDate("2016-10-01", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start9mTime, end9mTime, 642120, x09);

            Date targetDate10 = DateUtils.parseDate("2016-12-15 23:00:00", "yyyy-MM-dd HH:mm:ss");
            Long x10 = (targetDate10.getTime() - date.getTime()) / (1000 * 60 * 60);
            logger.info("OrderSCJobBean x10={}", x10);
            Date start10mTime = DateUtils.parseDate("2016-10-01", "yyyy-MM-dd");
            Date end10mTime = DateUtils.parseDate("2016-11-01", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start10mTime, end10mTime, 721200, x10);

            Date targetDate11 = DateUtils.parseDate("2016-12-15 23:00:00", "yyyy-MM-dd HH:mm:ss");
            Long x11 = (targetDate11.getTime() - date.getTime()) / (1000 * 60 * 60);
            logger.info("OrderSCJobBean x11={}", x11);
            Date start11mTime = DateUtils.parseDate("2016-11-01", "yyyy-MM-dd");
            Date end11mTime = DateUtils.parseDate("2016-12-01", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start11mTime, end11mTime, 121200, x11);

        } catch (Exception e) {
            logger.error("reportOrderStatistic: update order fail. DATE:" + new Date() + ": msg:", e);
        }

        logger.info("OrderSCJobBean job end.");

    }

}
