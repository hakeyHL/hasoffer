package hasoffer.job.bean.order;

import hasoffer.core.admin.IOrderStatsAnalysisService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

public class OrderSCBean extends QuartzJobBean {

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

    }
}
