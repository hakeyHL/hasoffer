package hasoffer.job.bean.fetch;

import hasoffer.job.service.IFetchTestService;
import hasoffer.job.service.ITopSellingTaskService;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

import javax.annotation.Resource;

public class TopSellingTaskJobBean extends QuartzJobBean {
    /**
     * Logger for this class 
     */
    private static final Logger logger = LoggerFactory.getLogger(TopSellingTaskJobBean.class);

    @Resource
    private ITopSellingTaskService topSellingTaskService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("TopSellingTaskJobBean is run at {}", new Date());
        //System.out.println(new Date()+":任务执行。");
        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context) - start");
        }
        topSellingTaskService.commitTask();
        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context={}) - end", context);
        }
    }

}
