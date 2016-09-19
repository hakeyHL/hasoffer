package hasoffer.job.bean.push;

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
                    new AppMsgDisplay(appPush.getTitle() + appPush.getContent(), appPush.getTitle(), appPush.getContent()),
                    new AppMsgClick(clickType, appPush.getSourceId(), WebsiteHelper.getPackage(website))
            );

            AppPushBo pushBo = new AppPushBo("678678", "19:50", message);


            //暂定推送人群是所有app用户
            int curPage = 1;
            int pageSize = 1000;

            PageableResult<UrmDevice> pageableResult = deviceService.findPagedUrmDeviceByAppType(AppType.APP, curPage, pageSize);

            long totalPage = pageableResult.getTotalPage();

            while (curPage <= totalPage) {

                List<String> gcmTokenList = new ArrayList<>();

                if (curPage > 1) {
                    pageableResult = deviceService.findPagedUrmDeviceByAppType(AppType.APP, curPage, pageSize);
                }

                List<UrmDevice> urmDeviceList = pageableResult.getData();

                for (UrmDevice urmDevice : urmDeviceList) {

                    String shopApp = urmDevice.getShopApp();
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
                    pushService.GroupPush(gcmTokenList, pushBo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                curPage++;
            }
        }


        logger.info("AdminPushJobBean will stop at {}", new Date());
    }
}