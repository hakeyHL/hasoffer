package hasoffer.job.bean.order;

import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;

public class OrderSAsisJobBean extends QuartzJobBean {
    private static Logger logger = LoggerFactory.getLogger(OrderSAsisJobBean.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    public static void main(String[] args) {
        Date date = new Date();
        Date targetDate = null;
        try {
            targetDate = DateUtils.parseDate("2016-11-21", "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long x = (targetDate.getTime() - date.getTime()) / (1000 * 60 * 60);
        System.out.println(x);
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {

            Date date = new Date();
            Date targetDate = DateUtils.parseDate("2016-11-21", "yyyy-MM-dd");
            Long x = (targetDate.getTime() - date.getTime()) / (1000 * 60 * 60);

            Date start8mTime = DateUtils.parseDate("2016-08-01", "yyyy-MM-dd");
            Date end8mTime = DateUtils.parseDate("2016-08-31", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start8mTime, end8mTime, 592120, x);

            Date start9mTime = DateUtils.parseDate("2016-08-01", "yyyy-MM-dd");
            Date end9mTime = DateUtils.parseDate("2016-08-31", "yyyy-MM-dd");
            orderStatsAnalysisService.updateOrderToLow(start9mTime, end9mTime, 692120, x);

        } catch (Exception e) {
            logger.error("reportOrderStatistic: update order fail. DATE:" + new Date() + ": msg:", e);
        }
    }
}
