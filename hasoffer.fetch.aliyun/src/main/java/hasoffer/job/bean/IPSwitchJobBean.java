package hasoffer.job.bean;

import hasoffer.aliyun.enums.Group;
import hasoffer.aliyun.enums.IPState;
import hasoffer.proxy.service.ProxyIPService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

public class IPSwitchJobBean extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(IPSwitchJobBean.class);

    @Resource
    private ProxyIPService proxyIPService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.info("IPSwitchJobBean.switchIP start.");
        startTask();
        logger.info("IPSwitchJobBean.switchIP end.");
    }

    private void startTask() {
        String preActiveGroup = proxyIPService.selectGroupName(IPState.Y);
        logger.info(preActiveGroup);
        if (preActiveGroup == null || "".equals(preActiveGroup)) {
            return;
        }
        boolean isActive = false;
        Group nextActiveGroup = Group.A;
        for (Group group : Group.values()) {
            if (group.toString().equals(preActiveGroup)) {
                isActive = true;
            }
            if (isActive && !group.toString().equals(preActiveGroup)) {
                nextActiveGroup = group;
                isActive = false;

            }
        }

        proxyIPService.updateProxyStausByGroup(preActiveGroup, IPState.N);
        proxyIPService.updateProxyStausByGroup(nextActiveGroup.toString(), IPState.Y);
    }
}
