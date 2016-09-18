package hasoffer.job.bean.push;

import hasoffer.base.utils.JSONUtil;
import hasoffer.core.persistence.po.app.AppPush;
import hasoffer.core.push.IPushService;
import hasoffer.data.redis.IRedisListService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

/**
 * Created on 2016/9/18.
 */
public class AdminPushJobBean extends QuartzJobBean {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminPushJobBean.class);
    private static final String ADMIN_PUSH_QUEUE = "ADMIN_PUSH_QUEUE";

    @Resource
    IRedisListService redisListService;
    @Resource
    IPushService pushService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("AdminPushJobBean is run at {}", new Date());

        Object pop = redisListService.pop(ADMIN_PUSH_QUEUE);

        if (pop != null) {

            try {

                AppPush appPush = JSONUtil.toObject((String) pop, AppPush.class);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        logger.info("AdminPushJobBean will stop at {}", new Date());
    }
}
