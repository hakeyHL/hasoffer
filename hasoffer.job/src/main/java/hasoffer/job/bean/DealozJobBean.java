package hasoffer.job.bean;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.HasofferRegion;
import hasoffer.job.service.IDealozFetchService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

public class DealozJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(DealozJobBean.class);

    @Resource
    private IDealozFetchService dealozFetchService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context) - start");
        }
        if (HasofferRegion.USA.equals(AppConfig.getSerRegion())) {
            dealozFetchService.fetchAllSite();
        } else {
            logger.info("It not in use. Don't execute this job.");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context={}) - end", context);
        }
    }

}
