package hasoffer.job.bean.push;

import com.google.android.gcm.server.MulticastResult;
import hasoffer.base.enums.AppType;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.push.*;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.PushSourceType;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppPush;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.push.IPushService;
import hasoffer.core.user.IDeviceService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.fetch.helper.WebsiteHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    IDataBaseManager dbm;
    @Resource
    IPushService pushService;
    @Resource
    IDeviceService deviceService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("AdminPushJobBean is run at {}", new Date());

        Object pop = redisListService.pop(ADMIN_PUSH_QUEUE);

        if (pop != null) {


            Website website = null;
            AppMsgClickType clickType = null;

            AppPush appPush = null;
            try {
                appPush = JSONUtil.toObject((String) pop, AppPush.class);
            } catch (IOException e) {
                System.out.println("pop string form object error");
                System.out.println((String) pop);
            }


            if (PushSourceType.DEAL.equals(appPush.getPushSourceType())) {
                clickType = AppMsgClickType.DEAL;
                website = dbm.get(AppDeal.class, Long.valueOf(appPush.getSourceId())).getWebsite();
            }

            AppPushMessage message = new AppPushMessage(
                    new AppMsgDisplay(appPush.getTitle() + appPush.getContent(), appPush.getTitle(), appPush.getContent(), appPush.getPushImageUrl()),
                    new AppMsgClick(clickType, appPush.getSourceId(), WebsiteHelper.getPackage(website))
            );

            AppPushBo pushBo = new AppPushBo("678678", "19:50", message);


            //暂定推送人群是所有app用户
            int curPage = 1;
            int pageSize = 1000;

            PageableResult<UrmDevice> pageableResult = deviceService.findPagedUrmDeviceByAppType(AppType.APP, curPage, pageSize);

            long totalPage = pageableResult.getTotalPage();
            System.out.println("totalpage = " + totalPage);

            while (curPage <= totalPage) {

                System.out.println("curPage = " + curPage);

                List<String> gcmTokenList = new ArrayList<>();

                if (curPage > 1) {
                    pageableResult = deviceService.findPagedUrmDeviceByAppType(AppType.APP, curPage, pageSize);
                }

                List<UrmDevice> urmDeviceList = pageableResult.getData();

                for (UrmDevice urmDevice : urmDeviceList) {

                    String shopApp = urmDevice.getShopApp();

                    if (urmDevice.getId().equals("dd3af1280b74a528f073316c17425841")) {
                        System.out.println("shitTime:" + new SimpleDateFormat("yyyyMMddHHmmss") + "found ashit");
                        System.out.println("website " + website.name());
                        System.out.println("shopApp = " + shopApp);
                    }

                    if (shopApp.contains(website.name())) {
                        //按照appVersion排除
                        //按照shopApp排除
                        //按照版本号过滤，有不支持推送的版本
                        //按照gcmtoken过滤
                        String gcmToken = urmDevice.getGcmToken();
                        if (!StringUtils.isEmpty(gcmToken)) {
                            gcmTokenList.add(gcmToken);
                        }
                    }
                }

                try {

                    if (gcmTokenList != null && gcmTokenList.contains("diME4RkV_6A:APA91bGHSgs6e6RyDjnKH2DPq3Ca7Q_D4cSRRq_JySvRO8txSIJgDgHFi1JULM7uM-EXwxTkswtP1PoKJzZ0l0jUdaAf88-VfZcVkE8C5rPEO-neb3hOdZjT0mjGsa002vLwdYHgyU3S")) {
                        System.out.println("push to ashit ");
                        String push = pushService.push(("diME4RkV_6A:APA91bGHSgs6e6RyDjnKH2DPq3Ca7Q_D4cSRRq_JySvRO8txSIJgDgHFi1JULM7uM-EXwxTkswtP1PoKJzZ0l0jUdaAf88-VfZcVkE8C5rPEO-neb3hOdZjT0mjGsa002vLwdYHgyU3S"), pushBo);
                        System.out.println("push result = " + push);
                    }

                    MulticastResult multicastResult = pushService.GroupPush(gcmTokenList, pushBo);

                    System.out.println(multicastResult.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                curPage++;
            }
        }


        logger.info("AdminPushJobBean will stop at {}", new Date());
    }
}
