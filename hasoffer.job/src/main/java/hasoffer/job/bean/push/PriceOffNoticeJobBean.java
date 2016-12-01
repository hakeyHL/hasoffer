package hasoffer.job.bean.push;

import com.alibaba.fastjson.JSON;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchUrlResult;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/8/31.
 */
public class PriceOffNoticeJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(PriceOffNoticeJobBean.class);
    private static final String PUSH_FAIL_PRICEOFFNOTICE_INFO = "PUSH_FAIL_PRICEOFFNOTICE_INFO";

    @Resource
    IPriceOffNoticeService priceOffNoticeService;
    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IRedisListService redisListService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        while (true) {

            //从结果队列中取出结果        todo 考虑是否要在此处启用更新
            String priceOffNoticeResult = fetchDubboService.popFetchUrlResult(TaskTarget.PRICEOFF_NOTICE);

            //为空，休息1小时
            if (StringUtils.isEmpty(priceOffNoticeResult)) {
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {

                }
                continue;
            }

            //获得结果对象
            FetchUrlResult fetchUrlResult = JSON.parseObject(priceOffNoticeResult, FetchUrlResult.class);

            Long skuId = fetchUrlResult.getSkuId();
            float nowPrice = fetchUrlResult.getFetchProduct().getPrice();
            Website website = fetchUrlResult.getFetchProduct().getWebsite();
            String url = fetchUrlResult.getFetchProduct().getUrl();
            String fetchedTitle = fetchUrlResult.getFetchProduct().getTitle();

            //找到订阅当前skuid的降价提醒记录
            List<PriceOffNotice> priceOffNoticeList = dbm.query("SELECT t FROM PriceOffNotice t WHERE t.skuid = ?0 ", Arrays.asList(skuId));

            //遍历订阅该skuid的集合
            for (PriceOffNotice priceOffNotice : priceOffNoticeList) {

                Long priceOffNoticeId = priceOffNotice.getId();

                //针对单个priceoffnotice发送push
                boolean flag = priceOffNoticeService.priceOffNoticeSinglePush(nowPrice, website, url, fetchedTitle, priceOffNoticeId);

                if (!flag) {
                    //此处暂时使用这个对象的一个字段传值
                    fetchUrlResult.setSkuId(priceOffNoticeId);
                    String repushInfo = JSON.toJSONString(fetchUrlResult);
                    redisListService.push(PUSH_FAIL_PRICEOFFNOTICE_INFO, repushInfo);
                }
            }
        }
    }


}