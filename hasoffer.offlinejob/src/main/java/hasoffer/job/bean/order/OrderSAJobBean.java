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

public class OrderSAJobBean extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(OrderSAJobBean.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("OrderSAsisJobBean job start.");
        Date todayTime = new Date();

        //临时只处理7天的deal。
        int[] days = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
        //int[] days = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30, 35, 50, 60};
        //int[] days = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 10, 13, 15, 18, 20, 25, 30};
        //int[] days = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 40, 45, 50};

        //for (int i = 1; i < 50; i++) {
        for (int i : days) {
            Date day = TimeUtils.addDay(todayTime, -i);
            orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), day, day);
            orderStatsAnalysisService.updateOrder(Website.FLIPKART.name(), day, day);
        }
        logger.info("OrderSAsisJobBean job end.");

    }
}
