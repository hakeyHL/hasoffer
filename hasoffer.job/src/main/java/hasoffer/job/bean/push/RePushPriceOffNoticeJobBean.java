package hasoffer.job.bean.push;

import hasoffer.base.utils.TimeUtils;
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
public class RePushPriceOffNoticeJobBean extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(RePushPriceOffNoticeJobBean.class);
    private static final String PUSH_FAIL_PRICEOFFNOTICE_ID = "PUSH_FAIL_PRICEOFFNOTICE_ID";
    private static final String PUSH_FAIL_PRICEOFFNOTICE_ID_14 = "PUSH_FAIL_PRICEOFFNOTICE_ID_14";
    private static final String PUSH_FAIL_PRICEOFFNOTICE_ID_22 = "PUSH_FAIL_PRICEOFFNOTICE_ID_22";

    @Resource
    IRedisListService redisListService;
    @Resource
    IPriceOffNoticeService priceOffNoticeService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        logger.info("RePushPriceOffNoticeJobBean will start at {}", new Date());
        //此处需要将缓存中的push失败的priceOffId重新push一遍
        //缓存失败队列分3个，按照时间点
        while (true) {

            Object pop = null;

            if (TimeUtils.getHour() < 18) {
                pop = redisListService.pop(PUSH_FAIL_PRICEOFFNOTICE_ID);
            } else if (TimeUtils.getHour() > 20) {
                pop = redisListService.pop(PUSH_FAIL_PRICEOFFNOTICE_ID_22);
            } else {
                pop = redisListService.pop(PUSH_FAIL_PRICEOFFNOTICE_ID_14);
            }

            if (pop == null) {
                System.out.println("repush pop get null ,repush job finish");
                break;
            }

            long priceOffNoticeId = Long.parseLong((String) pop);
            System.out.println("send repush for " + priceOffNoticeId);

            //每天11,14,22点重发，最后一次失败不在缓存key
            if (TimeUtils.getHour() < 14) {
                boolean status = priceOffNoticeService.pushFailRePush(priceOffNoticeId, false);
                if (!status) {//push失败
                    redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_ID_14, priceOffNoticeId + "");
                    System.out.println("repush fail cache to 14:00 queue " + TimeUtils.nowDate());
                }
            } else if (TimeUtils.getHour() > 20) {
                priceOffNoticeService.pushFailRePush(priceOffNoticeId, false);
            } else {
                boolean status = priceOffNoticeService.pushFailRePush(priceOffNoticeId, false);
                if (!status) {//push失败
                    redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_ID_22, priceOffNoticeId + "");
                    System.out.println("repush fail cache to 22:00 queue " + TimeUtils.nowDate());
                }
            }

            System.out.println("repush don't cache fail true " + TimeUtils.nowDate());
        }

        logger.info("RePushPriceOffNoticeJobBean will stop at {}", new Date());
    }

}
