package hasoffer.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.gcm.server.MulticastResult;
import hasoffer.admin.controller.vo.PushVo;
import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.push.*;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.PushSourceType;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppPush;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.push.IPushService;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.webcommon.helper.PageHelper;
import jodd.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value = "/push")
public class PushController {
    private static final String ADMIN_PUSH_QUEUE = "ADMIN_PUSH_QUEUE";
    private static final Logger logger = LoggerFactory.getLogger(PushController.class);
    static Map<Website, String> packageMap = new HashMap<Website, String>();

    static {
        packageMap.put(Website.SNAPDEAL, "com.snapdeal.main");
        packageMap.put(Website.FLIPKART, "com.flipkart.android");
        packageMap.put(Website.AMAZON, "in.amazon.mShop.android.shopping");
        packageMap.put(Website.PAYTM, "net.one97.paytm");
        packageMap.put(Website.EBAY, "com.ebay.mobile");
        packageMap.put(Website.SHOPCLUES, "com.shopclues");
        packageMap.put(Website.INFIBEAM, "com.infibeam.infibeamapp");
        packageMap.put(Website.MYNTRA, "com.myntra.android");
        packageMap.put(Website.JABONG, "com.jabong.android");
        packageMap.put(Website.VOONIK, "com.voonik.android");
    }

    @Resource
    IPushService pushService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IRedisListService redisListService;
    @Resource
    IDeviceService deviceService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request,
                             @RequestParam(defaultValue = "") String startTime,
                             @RequestParam(defaultValue = "") String endTime,
                             @RequestParam(defaultValue = "DEAL") String pushSourceTypeString,
                             @RequestParam(defaultValue = "1") int curPage,
                             @RequestParam(defaultValue = "20") int pageSize) {

        ModelAndView modelAndView = new ModelAndView("push/list");

        final String YMD_WEB_PATTERN = "yyyy-MM-dd";

        Date startDate = null;
        Date endDate = null;
        if (StringUtils.isEmpty(startTime)) {
            startDate = new Date(TimeUtils.today());
            endDate = new Date();
            startTime = TimeUtils.parse(startDate, YMD_WEB_PATTERN);
            endTime = startTime;
        } else {
            startDate = TimeUtils.stringToDate(startTime, YMD_WEB_PATTERN);
            endDate = TimeUtils.addDay(TimeUtils.stringToDate(endTime, YMD_WEB_PATTERN), 1);
        }

        PushSourceType pushSourceType = PushSourceType.valueOf(pushSourceTypeString);

        PageableResult pagedAppPush = pushService.getPagedAppPush(pushSourceType, startDate, endDate, curPage, pageSize);

        List<AppPush> appPushList = pagedAppPush.getData();

        PageModel pageModel = PageHelper.getPageModel(request, pagedAppPush);

        modelAndView.addObject("appPushList", appPushList);
        modelAndView.addObject("page", pageModel);
        modelAndView.addObject("startTime", startTime);
        modelAndView.addObject("endTime", endTime);

        return modelAndView;
    }

    @RequestMapping(value = "/pushInit/{pushSourceTypeString}/{sourceId}", method = RequestMethod.GET)
    public ModelAndView pushInit(@PathVariable String pushSourceTypeString, @PathVariable String sourceId) {
        ModelAndView mav = new ModelAndView("push/pushInit");

        if (StringUtils.isEmpty(pushSourceTypeString)) {
            pushSourceTypeString = "DEAL";
        }

        PushSourceType pushSourceType = PushSourceType.valueOf(pushSourceTypeString.toUpperCase());
        //        crowd
        String pushSourceId = sourceId;
        String pushTitle = "";
        String pushContent = "";
        String imageUrl = "";
        if (PushSourceType.DEAL.equals(pushSourceType)) {

            AppDeal appDeal = dbm.get(AppDeal.class, Long.valueOf(sourceId));

            pushTitle = appDeal.getTitle();
            pushContent = appDeal.getPriceDescription();
            imageUrl = ImageUtil.getImageUrl(appDeal.getImageUrl());
            mav.addObject("hasoferOriImageUrl", appDeal.getImageUrl());
        }

        AppPush appPush = new AppPush();
        appPush.setTitle(pushTitle);
        appPush.setContent(pushContent);
        appPush.setCreateTime(TimeUtils.nowDate());
        appPush.setPushSourceType(PushSourceType.DEAL);
        appPush.setSourceId(sourceId);
        mav.addObject("imageUrl", imageUrl);
        mav.addObject("pushSourceType", pushSourceType);
        mav.addObject("pushSourceId", pushSourceId);
        mav.addObject("pushTitle", pushTitle);
        mav.addObject("pushContent", pushContent);

        return mav;
    }

    @RequestMapping(value = "/create/{pushSourceTypeString}/{sourceId}", method = RequestMethod.POST)
    @ResponseBody
    public String create(HttpServletRequest request,
                         @PathVariable String pushSourceTypeString,
                         @PathVariable String sourceId,
                         MultipartFile pushImageFile,
                         String imageUrl) {
        String pushImageUrl = "";
        if (StringUtils.isEmpty(imageUrl)) {
            if (pushImageFile != null) {
                try {
                    File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
                    FileUtil.writeBytes(imageFile, pushImageFile.getBytes());
                    pushImageUrl = ImageUtil.uploadImage(imageFile);
                } catch (Exception e) {
                    System.out.println("upload image failed .");
                }
            }
        }
        String pushContent = request.getParameter("pushContent");
        if (StringUtils.isEmpty(pushSourceTypeString)) {
            pushSourceTypeString = "DEAL";
        }

        PushSourceType pushSourceType = PushSourceType.valueOf(pushSourceTypeString.toUpperCase());
        //        crowd
        String pushTitle = "";
        if (pushContent == null) {
            pushContent = "";
        }

        if (PushSourceType.DEAL.equals(pushSourceType)) {

            AppDeal appDeal = dbm.get(AppDeal.class, Long.valueOf(sourceId));

            pushTitle = appDeal.getTitle();
            if (StringUtils.isEmpty(pushContent)) {
                pushContent = appDeal.getPriceDescription();
            }

        }

        AppPush appPush = new AppPush();
        if (!StringUtils.isEmpty(pushImageUrl)) {
            appPush.setPushImageUrl(ImageUtil.getImageUrl(pushImageUrl));
        }
        appPush.setTitle(pushTitle);
        appPush.setContent(pushContent);
        appPush.setCreateTime(TimeUtils.nowDate());
        appPush.setPushSourceType(PushSourceType.DEAL);
        appPush.setSourceId(sourceId);

        //创建apppush对象
        pushService.createAppPush(appPush);
        //加入队列,由别的线程来完成push操作
        redisListService.push(ADMIN_PUSH_QUEUE, JSONUtil.toJSON(appPush));

        return "ok";
    }

    @RequestMapping(value = "/pushIndex")
    public ModelAndView PushIndex() {
        ModelAndView mav = new ModelAndView("push/push");
        List<MarketChannel> channles = pushService.getAllMarketChannels();
        List<Website> websites = new ArrayList<>();
        Class classzz = Website.class;
        for (Object o : classzz.getEnumConstants()) {
            websites.add((Website) o);
        }
        //获得所有APP版本
        List<String> versions = pushService.getAllAppVersions();
        mav.addObject("channels", channles);
        mav.addObject("websites", websites);
        mav.addObject("versions", versions);
        return mav;
    }

    @RequestMapping(value = "/pushMessage")
    public ModelAndView PushMessage(PushVo pushVol) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("success", true);
        //1.推送类型
        switch (pushVol.getPushType()) {
            case "single":
                //2.1单
                AppPushMessage message = new AppPushMessage(
                        new AppMsgDisplay(pushVol.getOutline(), pushVol.getTitle(), pushVol.getContent()),
                        new AppMsgClick(AppMsgClickType.valueOf(pushVol.getMessageType()), pushVol.getValue(), packageMap.get(pushVol.getWebsite()))
                );
                mv.addObject("pushCount", 1);
                mv.addObject("pushType", "single");
                AppPushBo pushBo = new AppPushBo("5x1", "15:10", message);
                //                //3.渠道
                //                for (String channel : pushVol.getChannel()) {
                //                    //4.版本列表
                //                    for (String version : pushVol.getVersion()) {
                //
                //                        for (String website : pushVol.getWebsite()) {
                //
                //                        }
                //                        //5.app列表
                //                    }
                //                }
                String pushResult = pushService.push(pushVol.getGcmToken(), pushBo);
                if (pushResult != null) {
                    JSONObject JsonObject = JSONObject.parseObject(pushResult);
                    if (JsonObject.getInteger("success") == 1) {
                        mv.addObject("successCount", 1);
                    } else {
                        mv.addObject("successCount", 0);
                        mv.addObject("failedCount", 1);
                    }
                    //                    JSONArray results = JsonObject.getJSONArray("results");
                    //                    JSONObject jsonObject = results.getJSONObject(0);
                    //                    String error = jsonObject.getString("error");
                } else {
                    mv.addObject("success", true);
                    mv.addObject("msg", "网络请求失败!");
                }
                break;
            case "group":
                //2.2群
                break;
            default:
                break;
        }
        return mv;
    }

    /**
     * @param appPush     封装对象
     * @param type        点击类型,推送类型
     * @param packageName 包名
     * @param toType      0 默认所有设备,1特指哪一个,需要给出设备id
     */
    @RequestMapping(value = "tempPushTask")
    public void tempPushTask(AppPush appPush, AppMsgClickType type, @RequestParam String packageName,
                             @RequestParam(defaultValue = "0") int toType,
                             String deviceId,
                             @RequestParam(defaultValue = "0") int version,
                             @RequestParam(defaultValue = "none") String versionCompare) {
        logger.info("apiTemPush interface   is run at {}", new Date());
        //只推送渠道为googlePlay,版本小于等于27的设备
        //type GOOGLEPLAY
        //url 无
        //包名com.india.hasoffer

        //title为空也默认是版本推送
        if (appPush == null || org.apache.commons.lang3.StringUtils.isEmpty(appPush.getTitle())) {
            appPush = new AppPush();
            appPush.setContent("Earn hasoffer coins to redeem the Rs.100 Amazon gift card！");
            appPush.setCreateTime(new Date());
            appPush.setTitle("Update the latest version.");
            type = AppMsgClickType.GOOGLEPLAY;
        }
        switch (toType) {
            case 0:
                if (appPush != null) {
                    AppMsgClick appMsgClick = new AppMsgClick(type, appPush.getSourceId() == null ? null : appPush.getSourceId(), packageName);
                    if (packageName.equals("com.india.hasoffer")) {
                        appMsgClick = new AppMsgClick(type, null, packageName);
                    }
                    AppPushMessage message = new AppPushMessage(
                            new AppMsgDisplay(appPush.getTitle() + appPush.getContent(), appPush.getTitle(), appPush.getContent(), appPush.getPushImageUrl()),
                            appMsgClick
                    );

                    AppPushBo pushBo = new AppPushBo("678678", "19:50", message);

                    System.out.println("push currentTime is :" + new SimpleDateFormat("yyyyMMddHHmmss").
                            format(new Date()) + " and content is : " + JSON.toJSONString(message));
                    //暂定推送人群是所有app用户
                    int curPage = 1;
                    int pageSize = 1000;
                    //只推送渠道为googlePlay,版本小于等于27的设备
                    //type GOOGLEPLAY
                    //appType 是app
                    PageableResult<UrmDevice> pageableResult = deviceService.findPagedUrmdeviceByAPPTypeAndChannel(AppType.APP, MarketChannel.GOOGLEPLAY, curPage, pageSize);
                    long totalPage = pageableResult.getTotalPage();
                    System.out.println("totalpage = " + totalPage);

                    while (curPage <= totalPage) {

                        System.out.println("curPage = " + curPage);

                        List<String> gcmTokenList = new ArrayList<>();

                        if (curPage > 1) {
                            pageableResult = deviceService.findPagedUrmdeviceByAPPTypeAndChannel(AppType.APP, MarketChannel.GOOGLEPLAY, curPage, pageSize);
                        }

                        List<UrmDevice> urmDeviceList = pageableResult.getData();

                        for (UrmDevice urmDevice : urmDeviceList) {

                            String shopApp = urmDevice.getShopApp();
                            String gcmToken = urmDevice.getGcmToken();
                            if (!hasoffer.base.utils.StringUtils.isEmpty(gcmToken)) {
                                if (version > 0) {
                                    //有版本要求
                                    if (!StringUtils.isEmpty(urmDevice.getAppVersion())) {
                                        if (!versionCompare.equals("none")) {
                                            //
                                            if (versionCompare.equals("le")) {
                                                if (Integer.valueOf(urmDevice.getAppVersion()) <= version) {
                                                    gcmTokenList.add(gcmToken);
                                                }
                                            } else {
                                                //大于等于
                                                if (Integer.valueOf(urmDevice.getAppVersion()) >= version) {
                                                    gcmTokenList.add(gcmToken);
                                                }
                                            }
                                        } else {
                                            //无限制
                                            gcmTokenList.add(gcmToken);
                                        }
                                    }
                                    //else 不添加
                                } else {
                                    //直接加
                                    gcmTokenList.add(gcmToken);
                                }
                            }
                            if (urmDevice.getId().equals("dd3af1280b74a528f073316c17425841")) {
                                System.out.println("shitTime:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "found ashit");
                                System.out.println("website " + packageName);
                                List<String> shitGcmTokenList = new ArrayList<>();
                                System.out.println("shopApp = " + shopApp);
                                List<UrmDevice> shitIds = deviceService.getDevicesByDeviceId(deviceId);
                                for (UrmDevice urmDevice1 : shitIds) {
                                    if (!StringUtils.isEmpty(urmDevice1.getGcmToken())) {
                                        gcmTokenList.add(urmDevice1.getGcmToken());
                                    }
                                }
                                if (shitGcmTokenList != null && shitGcmTokenList.size() > 0) {
                                    try {
                                        MulticastResult multicastResult = pushService.GroupPush(gcmTokenList, pushBo);
                                        System.out.println("shitPushResult = " + multicastResult.toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        try {
                            MulticastResult multicastResult = pushService.GroupPush(gcmTokenList, pushBo);

                            System.out.println(multicastResult.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        curPage++;
                    }
                }
                break;
            case 1:
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(deviceId)) {
                    List<String> gcmTokenList = new ArrayList<>();
                    AppMsgClick appMsgClick = new AppMsgClick(type, appPush.getSourceId() == null ? null : appPush.getSourceId(), packageName);
                    if (packageName.equals("com.india.hasoffer")) {
                        appMsgClick = new AppMsgClick(type, null, packageName);
                    }

                    AppPushMessage message = new AppPushMessage(
                            new AppMsgDisplay(appPush.getTitle() + appPush.getContent(), appPush.getTitle(), appPush.getContent(), appPush.getPushImageUrl()),
                            appMsgClick
                    );
                    AppPushBo pushBo = new AppPushBo("678678", "19:50", message);
                    List<UrmDevice> devicesByDeviceId = deviceService.getDevicesByDeviceId(deviceId);
                    for (UrmDevice urmDevice : devicesByDeviceId) {
                        String gcmToken = urmDevice.getGcmToken();
                        if (!hasoffer.base.utils.StringUtils.isEmpty(gcmToken)) {
                            if (version > 0) {
                                //有版本要求
                                if (!StringUtils.isEmpty(urmDevice.getAppVersion())) {
                                    if (!versionCompare.equals("none")) {
                                        //
                                        if (versionCompare.equals("le")) {
                                            if (Integer.valueOf(urmDevice.getAppVersion()) <= version) {
                                                gcmTokenList.add(gcmToken);
                                            }
                                        } else {
                                            //大于等于
                                            if (Integer.valueOf(urmDevice.getAppVersion()) >= version) {
                                                gcmTokenList.add(gcmToken);
                                            }
                                        }
                                    } else {
                                        //无限制
                                        gcmTokenList.add(gcmToken);
                                    }
                                }
                                //else 不添加
                            } else {
                                //直接加
                                gcmTokenList.add(gcmToken);
                            }
                        }
                    }
                    MulticastResult multicastResult = null;
                    try {
                        if (gcmTokenList != null && gcmTokenList.size() > 0) {
                            multicastResult = pushService.GroupPush(gcmTokenList, pushBo);
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    System.out.println(multicastResult.toString());
                }
                break;
            default:
        }
        logger.info("temp Push   stop at {}", new Date());
    }
}
