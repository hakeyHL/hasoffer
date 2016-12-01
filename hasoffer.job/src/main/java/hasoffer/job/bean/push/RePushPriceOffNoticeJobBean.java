package hasoffer.job.bean.push;

import com.alibaba.fastjson.JSON;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.spider.model.FetchUrlResult;
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
    private static final String PUSH_FAIL_PRICEOFFNOTICE_INFO = "PUSH_FAIL_PRICEOFFNOTICE_INFO";
    private static final String PUSH_FAIL_PRICEOFFNOTICE_INFO_14 = "PUSH_FAIL_PRICEOFFNOTICE_INFO_14";
    private static final String PUSH_FAIL_PRICEOFFNOTICE_INFO_22 = "PUSH_FAIL_PRICEOFFNOTICE_INFO_22";

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

            if (TimeUtils.getHour() < 14) {
                pop = redisListService.pop(PUSH_FAIL_PRICEOFFNOTICE_INFO);
            } else if (TimeUtils.getHour() > 20) {
                pop = redisListService.pop(PUSH_FAIL_PRICEOFFNOTICE_INFO_22);
            } else {
                pop = redisListService.pop(PUSH_FAIL_PRICEOFFNOTICE_INFO_14);
            }

            if (pop == null) {
                System.out.println("repush pop get null ,repush job finish");
                break;
            }


            FetchUrlResult fetchUrlResult = JSON.parseObject((String) pop, FetchUrlResult.class);

            Long priceOffNoticeId = fetchUrlResult.getSkuId();
            float nowPrice = fetchUrlResult.getFetchProduct().getPrice();
            Website website = fetchUrlResult.getFetchProduct().getWebsite();
            String url = fetchUrlResult.getFetchProduct().getUrl();
            String fetchedTitle = fetchUrlResult.getFetchProduct().getTitle();

            //每天11,14,22点重发，最后一次失败不在缓存key
            if (TimeUtils.getHour() < 14) {
                boolean status = priceOffNoticeService.priceOffNoticeSinglePush(nowPrice, website, url, fetchedTitle, priceOffNoticeId);
                if (!status) {//push失败
                    redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_INFO_14, JSON.toJSONString(fetchUrlResult));
                    System.out.println("repush fail cache to 14:00 queue " + TimeUtils.nowDate());
                }
            } else if (TimeUtils.getHour() > 20) {
                priceOffNoticeService.priceOffNoticeSinglePush(nowPrice, website, url, fetchedTitle, priceOffNoticeId);
            } else {
                boolean status = priceOffNoticeService.priceOffNoticeSinglePush(nowPrice, website, url, fetchedTitle, priceOffNoticeId);
                if (!status) {//push失败
                    redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_INFO_22, JSON.toJSONString(fetchUrlResult));
                    System.out.println("repush fail cache to 14:00 queue " + TimeUtils.nowDate());
                }
            }

            System.out.println("repush don't cache fail true " + TimeUtils.nowDate());
        }

        logger.info("RePushPriceOffNoticeJobBean will stop at {}", new Date());
    }

}
