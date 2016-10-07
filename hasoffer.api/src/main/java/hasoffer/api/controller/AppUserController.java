package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.api.controller.vo.SearchIO;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.model.Website;
import hasoffer.base.redis.RedisKey;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.mongo.UserSignLog;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmSignAwdCfg;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hs on 2016/7/8.
 */
@Controller
@RequestMapping("/app")
public class AppUserController {

    private final Logger logger = LoggerFactory.getLogger(AppUserController.class);

    private final Long jotLag = TimeUtils.MILLISECONDS_OF_1_MINUTE * 150;

    @Resource
    AppServiceImpl appService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IPriceOffNoticeService iPriceOffNoticeService;
    @Resource
    ICacheService<Map> urmDeviceService;
    @Resource
    MongoDbManager mongoDbManager;

    public static void main(String[] args) {
        //String affs[] = null;
        //affs = new String[]{"GOOGLEPLAY", "240a00b4f81c11da"};
        //String affsUrl = WebsiteHelper.getDealUrlWithAff(Website.SNAPDEAL,
        //        "http://m.snapdeal.com?utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_id=82856", affs);
        //System.out.println(affsUrl);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(1475397136114L));
        System.out.println((System.currentTimeMillis() - 1475226587161L) / 1000 / 60 / 60);
    }

    @RequestMapping("common/addUserId2DeepLink")
    public ModelAndView addUserId2DeepLink(@RequestParam String deepLink, @RequestParam String website) {
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        ModelAndView modelAndView = new ModelAndView();
        Map map = new HashMap();
        DeviceInfoVo deviceInfo = null;
        String currentTime = new SimpleDateFormat("MMM dd,yyyy ", Locale.ENGLISH).format(new Date());
        deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        SearchIO sio = new SearchIO("", "", "", website, "", deviceInfo.getMarketChannel(), deviceId, 0, 0);
        UrmUser urmUser = appService.getUserByUserToken((String) Context.currentContext().get(StaticContext.USER_TOKEN));
        String affs[] = null;
        if (urmUser != null) {
            affs = new String[]{sio.getMarketChannel().name(), sio.getDeviceId(), urmUser.getId() + ""};
        } else {
            map.put("deeplink", deepLink);
            modelAndView.addObject("data", map);
            return modelAndView;
        }
        String affsUrl = WebsiteHelper.getDealUrlWithAff(Website.valueOf(website), deepLink, affs);
        logger.info("addUserId2DeepLink(): success ,time : " + currentTime + "  userId : " + urmUser.getId() + " ,sourceLink : " + deepLink + " ,result : " + affsUrl);
        map.put("deeplink", affsUrl);
        modelAndView.addObject("data", map);
        return modelAndView;
    }

    @RequestMapping("user/priceAlert")
    public String setPriceAlert(@RequestParam(defaultValue = "100") int type,
                                @RequestParam(defaultValue = "0") long skuId,
                                @RequestParam(defaultValue = "0") float skuPrice,
                                HttpServletResponse response,
                                HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        //绑定用户设备关系
        System.out.println("get info : type " + type + " skuId :" + skuId + " skuPrice " + skuPrice);
        //get user by userToken
        String userToken = Context.currentContext().getHeader("usertoken");
        if (!StringUtils.isEmpty(userToken)) {
            System.out.println(" has userToken :" + userToken);
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                List<String> ids = null;
                String deviceId = JSON.parseObject(request.getHeader("deviceinfo")).getString("deviceId");
                String deviceKey = "urmDevice_ids_mapKey_" + deviceId;
                Map map = null;
                String deviceValue = urmDeviceService.get(deviceKey, 0);

                if (!StringUtils.isEmpty(deviceValue)) {
                    ids = new ArrayList<>();
                    JSONObject jsonObjects = JSONObject.parseObject(deviceValue);
                    JSONArray urmDevice_ids1 = jsonObjects.getJSONArray("urmDevice_ids");
                    String[] strings = urmDevice_ids1.toArray(new String[]{});
                    for (String str : strings) {
                        ids.add(str);
                    }
                } else {
                    ids = appService.getUserDevices(deviceId);
                    map = new HashMap();
                    map.put("urmDevice_ids", ids);
                    urmDeviceService.add(deviceKey, JSONUtil.toJSON(map), TimeUtils.SECONDS_OF_1_DAY);
                }
                System.out.println("update user and device relationship ");
                List<String> deviceIds = appService.getUserDevicesByUserId(urmUser.getId() + "");
                System.out.println("get ids  by userId from urmUserDevice :" + deviceIds.size());
                List<UrmUserDevice> urmUserDevices = new ArrayList<>();
                for (String id : ids) {
                    boolean flag = false;
                    for (String dId : deviceIds) {
                        if (id.equals(dId)) {
                            flag = true;
                            System.out.println("dId by UserId :" + dId + " is  equal to id from deviceId :" + id);
                        }
                    }
                    if (!flag) {
                        System.out.println("id :" + id + " is not exist before ");
                        UrmUserDevice urmUserDevice = new UrmUserDevice();
                        urmUserDevice.setDeviceId(id);
                        urmUserDevice.setUserId(urmUser.getId() + "");
                        urmUserDevices.add(urmUserDevice);
                    }
                }
                //将关联关系插入到关联表中
                int count = appService.addUrmUserDevice(urmUserDevices);
                System.out.println(" batch save  result size : " + count);


                System.out.println("get this user " + urmUser.getUserName() + " and id is :" + urmUser.getId());
                //insert record into priceOffAlert
                if (skuId != 0) {
                    PtmCmpSku cmpSku = cmpSkuService.getCmpSkuById(skuId);
                    if (cmpSku != null) {
                        PriceOffNotice priceOffNotice = iPriceOffNoticeService.getPriceOffNotice(urmUser.getId() + "", cmpSku.getId());
                        if (priceOffNotice != null) {
                            System.out.println("delete record :" + priceOffNotice.toString());
                            iPriceOffNoticeService.deletePriceOffNotice(urmUser.getId() + "", cmpSku.getId());
                        }
                        System.out.println("has this sku ,id is " + skuId + " and it's price in database is :" + cmpSku.getPrice());
                        switch (type) {
                            case 0:
                                //cancel
                                System.out.println("cancel ");
                                if (priceOffNotice != null) {
                                    iPriceOffNoticeService.deletePriceOffNotice(urmUser.getId() + "", cmpSku.getId());
                                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                    return null;
                                } else {
                                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                    return null;
                                }
                            case 1:
                                //set
                                if (skuPrice <= 0) {
                                    System.out.println("not permit set this price :" + skuPrice);
                                    System.out.println("use sku currentPrice " + cmpSku.getPrice());
                                    //not exist before
                                    System.out.println("add record ");
                                    boolean notice = iPriceOffNoticeService.createPriceOffNotice(urmUser.getId() + "", cmpSku.getId(), cmpSku.getPrice(), cmpSku.getPrice());
                                    System.out.println(" result is :" + notice);
                                } else {
                                    System.out.println("price is lg than zero ");
                                    //not exist before
                                    boolean notice = iPriceOffNoticeService.createPriceOffNotice(urmUser.getId() + "", cmpSku.getId(), skuPrice, skuPrice);
                                    System.out.println("   is :" + notice);
                                }
                                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                return null;
                            default:
                                jsonObject.put("errorCode", "10001");
                                jsonObject.put("msg", "type error ");
                                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                return null;
                        }
                    }
                }
            }
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    @RequestMapping("user/check/priceOff")
    public String checkPriceOff(@RequestParam(defaultValue = "0") long skuId,
                                HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "10001");
        jsonObject.put("msg", "no");
        String userToken = Context.currentContext().getHeader("usertoken");
        if (!StringUtils.isEmpty(userToken)) {
            System.out.println("userToken is :" + userToken);
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                System.out.println("this userToken has user ");
                System.out.println("check :  " + urmUser.getId() + "  skuId: " + skuId);
                PriceOffNotice priceOffNotice = iPriceOffNoticeService.getPriceOffNotice(urmUser.getId() + "", skuId);
                if (priceOffNotice != null) {
                    System.out.println("user has concerned this sku :" + skuId);
                    jsonObject.put("errorCode", "00000");
                    jsonObject.put("msg", "ok");
                }
            }
        }
        System.out.println(" response result is : " + JSON.toJSONString(jsonObject));
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    /**
     * 签到
     *
     * @param response
     * @return
     */
    @RequestMapping("user/sign")
    public String userSign(HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "10001");
        jsonObject.put("msg", "sign  fail ");
        // 无论用户是否存在，应该记住用户签到记录，防止由于某些信息错误，丢失用户签到记录。
        String userToken = Context.currentContext().getHeader("usertoken");
        String deviceInfo = Context.currentContext().getHeader("deviceinfo");
        logger.info("userSign(): User sign log. userToken:{}, deviceInfo:{}", userToken, deviceInfo);
        //1. 用户是否存在
        if (!StringUtils.isEmpty(userToken)) {
            logger.info("userSign(): userToken is :" + userToken);
            Date currentDate = new Date();
            Long nowTime = currentDate.getTime();
            String s = urmDeviceService.get(RedisKey.USER_SIGN_LAST_OP_TIME + userToken, 0);
            //注意：此处加一层Redis缓存，拦截。
            if (s != null) {
                logger.info("userSign(): repetitive  operation ,UserToken:{} has been  signed today  !", userToken);
                jsonObject.put("msg", "Request is being processed, please wait 5 seconds and try again later. ");
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }
            urmDeviceService.add(RedisKey.USER_SIGN_LAST_OP_TIME + userToken, "true", 5);
            long startTime = System.currentTimeMillis();
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                logger.info("userSign():get User " + urmUser.getUserName() + ":sign action ");

                if (urmUser.getLastSignTime() == null) {
                    //之前未签到过
                    urmUser.setLastSignTime(0L);
                }
                //判断今天是否已经签到
                long indiaSignTime = TimeUtils.getIndiaTime(urmUser.getLastSignTime());
                long indiaCurrentTime = TimeUtils.getIndiaTime(nowTime);
                long indiaSignDay = indiaSignTime / TimeUtils.MILLISECONDS_OF_1_DAY + 1;
                long indiaCurrentDay = indiaCurrentTime / TimeUtils.MILLISECONDS_OF_1_DAY + 1;

                long daysAfterLastSing = indiaCurrentDay - indiaSignDay;
                if (daysAfterLastSing < 1) {
                    //如果今天已经签到,over
                    logger.info("userSign(): repetitive  operation , has been  signed today  !");
                    jsonObject.put("msg", "you have signed today , please come tomorrow. ");
                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                    return null;
                }
                //如果之前未签到,那么上次签到时间已经被更新为当前时间
                List<UrmSignAwdCfg> signConfigs = appService.getSignAwardNum();
                //按照连续次数降序查询奖励配置表
                if (daysAfterLastSing == 1) {
                    //如果两个相等,代表是连续的,连续+1
                    urmUser.setConSignNum(urmUser.getConSignNum() + 1);
                    if (signConfigs != null && signConfigs.size() > 0) {
                        //如果当前连续大于等于最大连续奖励数,按最大来
                        if (urmUser.getConSignNum() >= signConfigs.get(0).getCount()) {
                            urmUser.setSignCoin(urmUser.getSignCoin() + signConfigs.get(0).getAwardCoin());
                        } else {
                            for (UrmSignAwdCfg urmSign : signConfigs) {
                                if (urmSign.getCount().equals(urmUser.getConSignNum())) {
                                    //匹配到
                                    urmUser.setSignCoin(urmUser.getSignCoin() + urmSign.getAwardCoin());
                                }
                            }
                        }
                        urmUser.setLastSignTime(new Date().getTime());
                    } else {
                        //未查询到配置表,无法计算,结束,报错
                        logger.info("userSign():No award config data, over !");
                        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                        return null;
                    }
                } else {
                    //断掉了,将本次最高签到数与上次相比
                    Integer maxConSignNum = urmUser.getMaxConSignNum();
                    Integer conSignNum = urmUser.getConSignNum();
                    if (conSignNum == 0) {
                        //之前未签到过
                        urmUser.setMaxConSignNum(1);
                    } else if (urmUser.getConSignNum() > maxConSignNum) {
                        //比之前高,更新最高签到记录
                        urmUser.setMaxConSignNum(urmUser.getConSignNum());
                    }
                    //比之前低,重置连续数,给coin
                    urmUser.setConSignNum(1);
                    for (UrmSignAwdCfg urmSign : signConfigs) {
                        if (urmSign.getCount().equals(urmUser.getConSignNum())) {
                            //匹配到
                            urmUser.setSignCoin(urmUser.getSignCoin() + urmSign.getAwardCoin());
                        }
                    }
                    urmUser.setLastSignTime(new Date().getTime());
                }
                logger.info("userSign(): user sign info{ID:{}, userName:{}, sign Time:{}, signCoin{}}", urmUser.getId(), urmUser.getUserName(), new Date(), urmUser.getSignCoin());
                //执行更新
                try {
                    //由于此方法不返回操作结果,只能判断异常来处理操作为成功的情况
                    appService.updateUserInfo(urmUser);
                } catch (Exception e) {
                    //异常,结束
                    logger.error("userSign(): update sign fail ,message is :{}", e.getMessage(), e);
                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                    return null;
                }
                long endTime = System.currentTimeMillis();
                logger.info("userSign(): user: {} sign, and cost {} ms. ", urmUser.getId(), startTime - endTime);
                jsonObject.put("errorCode", "00000");
                jsonObject.put("msg", "sign success ! ");
                //插入记录到签到历史表
                UserSignLog userSignLog = new UserSignLog(urmUser);
                mongoDbManager.save(userSignLog);
            } else {
                //token未查询到用户
                logger.info("userSign(): this userToken not get record " + userToken);
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }
        } else {
            //无token,over
            logger.info("userSign(): userToken is empty ,over  !");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        logger.info("userSign(): response result is : " + JSON.toJSONString(jsonObject));
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

}
