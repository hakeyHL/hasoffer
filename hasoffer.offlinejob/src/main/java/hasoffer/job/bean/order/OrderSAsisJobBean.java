package hasoffer.job.bean.order;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

public class OrderSAsisJobBean extends QuartzJobBean {
    private static Logger logger = LoggerFactory.getLogger(OrderSAsisJobBean.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {

            Date todayTime = new Date();
            ////头15天
            //Date day15AgoTime = TimeUtils.addDay(todayTime, -15);
            //// 头7天
            //Date day7AgoTime = TimeUtils.addDay(todayTime, -7);
            ////头三天
            //Date day3AgoTime = TimeUtils.addDay(todayTime, -3);
            ////头两天
            //Date day2AgoTime = TimeUtils.addDay(todayTime, -2);
            ////头一天
            //Date day1AgoTime = TimeUtils.addDay(todayTime, -1);
            //
            //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day15AgoTime, day15AgoTime);
            //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day3AgoTime, day3AgoTime);
            //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day2AgoTime, day2AgoTime);
            //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.toString(), day1AgoTime, day1AgoTime);

            int[] days = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 10, 13, 15, 18, 20, 25, 30};
            //int[] days = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 10, 13, 15, 18, 20, 25, 30, 35, 40, 45, 50};

            for (int i : days) {
                Date day = TimeUtils.addDay(todayTime, -i);
                orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), day, day);
                orderStatsAnalysisService.updateOrder(Website.FLIPKART.name(), day, day);
            }

        } catch (Exception e) {
            logger.error("reportOrderStatistic: update order fail. DATE:" + new Date() + ": msg:", e);
        }
    }
}
