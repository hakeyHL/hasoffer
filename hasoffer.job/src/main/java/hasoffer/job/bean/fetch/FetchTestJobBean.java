package hasoffer.job.bean.fetch;

import hasoffer.job.service.IFetchTestService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

public class FetchTestJobBean extends QuartzJobBean {
    /**
     * Logger for this class 
     */
    private static final Logger logger = LoggerFactory.getLogger(FetchTestJobBean.class);

    @Resource
    private IFetchTestService fetchTestService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("FetchTestJobBean is run at {}", new Date());
        //System.out.println(new Date()+":任务执行。");
        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context) - start");
        }
        fetchTestService.commitTask();
        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context={}) - end", context);
        }
    }

}
