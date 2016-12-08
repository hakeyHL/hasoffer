package hasoffer.job.bean;

import hasoffer.state.service.UpdateStateService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

public class UpdateStateJobBean extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(EipJobBean.class);

    @Resource
    private UpdateStateService updateStateService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("UpdateStateJobBean.updateState start.");
        updateStateService.stateTask();
        logger.info("UpdateStateJobBean.updateState end.");
    }
}
