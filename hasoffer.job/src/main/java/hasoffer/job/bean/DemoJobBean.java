package hasoffer.job.bean;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DemoJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(DemoJobBean.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("DemoJobBean is run at {}" ,new Date());
    }

}
