package hasoffer.job.bean.push;

import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created on 2016/8/31.
 */
public class PriceOffNoticeJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(PriceOffNoticeJobBean.class);
    private static final String PRICE_OFF_SKUID_QUEUE = "PRICE_OFF_SKUID_QUEUE";

    @Resource
    IRedisListService redisListService;
    @Resource
    IPriceOffNoticeService priceOffNoticeService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        Long size = redisListService.size(PRICE_OFF_SKUID_QUEUE);

        if (size > 0) {

            logger.info("PriceOffNoticeJobBean is run at {}", new Date());
            logger.info("Need push " + size + "sku");

            for (int i = 0; i < size; i++) {

                Long skuid = Long.parseLong((String) redisListService.pop(PRICE_OFF_SKUID_QUEUE));
                logger.info("price off push for " + skuid);

                priceOffNoticeService.priceOffCheck(skuid);
            }
        }

        logger.info("PriceOffNoticeJobBean will stop at {}", new Date());
    }

}