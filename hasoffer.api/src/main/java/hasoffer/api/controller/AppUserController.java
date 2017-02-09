package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.util.Md5Utils;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.model.Website;
import hasoffer.base.redis.RedisKey;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.app.vo.SearchIO;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.mongo.UserSignLog;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmSignCoin;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.AppUserService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hs on 2016/7/8.
 */
@Controller
@RequestMapping("/app")
public class AppUserController {
    private final Logger logger = LoggerFactory.getLogger(AppUserController.class);
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
    @Resource
    ApiUtils apiUtils;
    @Resource
    IPtmStdPriceService ptmStdPriceService;
    @Resource
    AppUserService appUserService;

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
        SearchIO sio = new SearchIO(ConstantUtil.API_DATA_EMPTYSTRING, ConstantUtil.API_DATA_EMPTYSTRING, ConstantUtil.API_DATA_EMPTYSTRING, website, ConstantUtil.API_DATA_EMPTYSTRING, deviceInfo.getMarketChannel(), deviceId, 0, 0);
        UrmUser urmUser = appService.getUserByUserToken((String) Context.currentContext().get(StaticContext.USER_TOKEN));
        String affs[] = null;
        if (urmUser != null) {
            affs = new String[]{sio.getMarketChannel().name(), sio.getDeviceId(), urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING};
        } else {
            map.put("deeplink", deepLink);
            modelAndView.addObject(ConstantUtil.API_NAME_DATA, map);
            return modelAndView;
        }
        String affsUrl = WebsiteHelper.getDealUrlWithAff(Website.valueOf(website), deepLink, affs);
        logger.info("addUserId2DeepLink(): success ,time : " + currentTime + "  userId : " + urmUser.getId() + " ,sourceLink : " + deepLink + " ,result : " + affsUrl);
        map.put("deeplink", affsUrl);
        modelAndView.addObject(ConstantUtil.API_NAME_DATA, map);
        return modelAndView;
    }

    @RequestMapping("user/priceAlert")
    public String setPriceAlert(@RequestParam(defaultValue = "100") int type,
                                @RequestParam(defaultValue = "0") long skuId,
                                @RequestParam(defaultValue = "0") String skuPrice,
                                HttpServletResponse response,
                                HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        //绑定用户设备关系
        if (skuId <= 0) {
            jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            jsonObject.put(ConstantUtil.API_NAME_MSG, "id le than zero ");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        skuPrice = apiUtils.getStringNum(skuPrice);
        float truelySkuPrice = Float.valueOf(skuPrice);
        //get user by userToken
        String userToken = Context.currentContext().getHeader("usertoken");
        if (!StringUtils.isEmpty(userToken)) {
            System.out.println(" has userToken :" + userToken);
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                List<String> ids;
                String deviceId = JSON.parseObject(request.getHeader("deviceinfo")).getString("deviceId");
                String deviceKey = "urmDevice_ids_mapKey_" + deviceId;
                Map map;
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
                List<String> deviceIds = appService.getUserDevicesByUserId(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING);
                List<UrmUserDevice> urmUserDevices = new ArrayList<>();
                ApiUtils.bindUserAndDevices(urmUser, ids, deviceIds, urmUserDevices);
                //将关联关系插入到关联表中
                appService.addUrmUserDevice(urmUserDevices);
                //insert record into priceOffAlert
                long priceOffSkuId;
                float priceOffSkuPrice;
                if (skuId != 0) {
                    if ((skuId + ConstantUtil.API_DATA_EMPTYSTRING).length() >= 10) {
                        PtmStdPrice ptmStdPrice = ptmStdPriceService.getPtmStdPriceById(ApiUtils.removeBillion(skuId));
                        if (ptmStdPrice != null) {
                            priceOffSkuId = ApiUtils.addBillion(ptmStdPrice.getId());
                            priceOffSkuPrice = ptmStdPrice.getPrice();
                        } else {
                            PtmCmpSku cmpSku = cmpSkuService.getCmpSkuById(skuId);
                            priceOffSkuId = cmpSku.getId();
                            priceOffSkuPrice = cmpSku.getPrice();
                        }
                    } else {
                        PtmCmpSku cmpSku = cmpSkuService.getCmpSkuById(skuId);
                        priceOffSkuId = cmpSku.getId();
                        priceOffSkuPrice = cmpSku.getPrice();
                    }
                    if (priceOffSkuId != 0) {
                        PriceOffNotice priceOffNotice = iPriceOffNoticeService.getPriceOffNotice(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING, priceOffSkuId);
                        if (priceOffNotice != null) {
                            iPriceOffNoticeService.deletePriceOffNotice(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING, priceOffSkuId);
                        }
                        switch (type) {
                            case 0:
                                //cancel
                                System.out.println("cancel ");
                                if (priceOffNotice != null) {
                                    iPriceOffNoticeService.deletePriceOffNotice(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING, priceOffSkuId);
                                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                    return null;
                                } else {
                                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                    return null;
                                }
                            case 1:
                                //set
                                if (truelySkuPrice <= 0) {
                                    //not exist before
                                    iPriceOffNoticeService.createPriceOffNotice(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING, priceOffSkuId, priceOffSkuPrice, priceOffSkuPrice);

                                } else {
                                    //not exist before
                                    iPriceOffNoticeService.createPriceOffNotice(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING, priceOffSkuId, truelySkuPrice, truelySkuPrice);
                                }
                                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                                return null;
                            default:
                                jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, "10001");
                                jsonObject.put(ConstantUtil.API_NAME_MSG, "type error ");
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
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, "10001");
        jsonObject.put(ConstantUtil.API_NAME_MSG, "no");
        if (skuId <= 0) {
            jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, "10001");
            jsonObject.put(ConstantUtil.API_NAME_MSG, "id le zero ");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        String userToken = Context.currentContext().getHeader("usertoken");
        if (!StringUtils.isEmpty(userToken)) {
            System.out.println("usertoken is :" + userToken);
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                PriceOffNotice priceOffNotice = iPriceOffNoticeService.getPriceOffNotice(urmUser.getId() + ConstantUtil.API_DATA_EMPTYSTRING, skuId);
                if (priceOffNotice != null) {
                    System.out.println("user has concerned this sku :" + skuId);
                    jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
                    jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
                }
            }
        }
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
        String signFailMsg = "Unable to sign in, please contact customer service.";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, "10001");
        jsonObject.put(ConstantUtil.API_NAME_MSG, signFailMsg);
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
                jsonObject.put(ConstantUtil.API_NAME_MSG, "Request is being processed, please wait 5 seconds and try again later. ");
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }
            urmDeviceService.add(RedisKey.USER_SIGN_LAST_OP_TIME + userToken, "true", 5);
            Map<Integer, Integer> afwCfgMap = appService.getSignAwardNum();
            if (afwCfgMap.size() == 0) {
                logger.info("userSign():No award config data, over !");
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }
            Set<Integer> integers = afwCfgMap.keySet();
            Integer max = Collections.max(integers);
            Integer min = Collections.min(integers);
            long startTime = System.currentTimeMillis();
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                logger.info("userSign():get User " + urmUser.getUserName() + ":sign action ");

                UrmSignCoin urmSignCoin = appService.getSignCoinByUserId(urmUser.getId());

                if (urmSignCoin == null) {
                    //之前未签到过
                    urmSignCoin = new UrmSignCoin();
                    urmSignCoin.setUserId(urmUser.getId());
                }
                //判断今天是否已经签到
                long indiaSignTime = TimeUtils.getIndiaTime(urmSignCoin.getLastSignTime());
                long indiaCurrentTime = TimeUtils.getIndiaTime(nowTime);
                long indiaSignDay = indiaSignTime / TimeUtils.MILLISECONDS_OF_1_DAY + 1;
                long indiaCurrentDay = indiaCurrentTime / TimeUtils.MILLISECONDS_OF_1_DAY + 1;

                long daysAfterLastSing = indiaCurrentDay - indiaSignDay;
                if (daysAfterLastSing < 1) {
                    //如果今天已经签到,over
                    logger.info("userSign(): repetitive  operation , has been  signed today  !");
                    jsonObject.put(ConstantUtil.API_NAME_MSG, "you have signed today , please come tomorrow. ");
                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                    return null;
                } else if (daysAfterLastSing == 1) {//相隔一天,代表是连续的,连续+1
                    Integer conSignNum = urmSignCoin.getConSignNum() + 1;
                    //如果当前连续大于等于最大连续奖励数,按最大来
                    if (conSignNum >= max) {
                        urmSignCoin.setSignCoin(urmSignCoin.getSignCoin() + afwCfgMap.get(max));
                    } else {
                        urmSignCoin.setSignCoin(urmSignCoin.getSignCoin() + afwCfgMap.get(conSignNum));
                    }
                    urmSignCoin.setSumSignNum(urmSignCoin.getSumSignNum() + 1);
                    urmSignCoin.setConSignNum(conSignNum);
                    urmSignCoin.setLastSignTime(new Date().getTime());
                } else {
                    //断掉了,将本次最高签到数与上次相比
                    Integer maxConSignNum = urmSignCoin.getMaxConSignNum();
                    Integer conSignNum = urmSignCoin.getConSignNum();
                    if (conSignNum == 0) {
                        //之前未签到过
                        urmSignCoin.setMaxConSignNum(1);
                    } else if (urmSignCoin.getConSignNum() > maxConSignNum) {
                        //比之前高,更新最高签到记录
                        urmSignCoin.setMaxConSignNum(urmSignCoin.getConSignNum());
                    }
                    //比之前低,重置连续数,给coin
                    urmSignCoin.setConSignNum(1);
                    urmSignCoin.setSumSignNum(urmSignCoin.getSumSignNum() + 1);
                    urmSignCoin.setSignCoin(urmSignCoin.getSignCoin() + afwCfgMap.get(min));
                    urmSignCoin.setLastSignTime(new Date().getTime());
                }
                logger.info("userSign(): user sign info{ID:{}, userName:{}, sign Time:{}, signCoin:{}}", urmUser.getId(), urmUser.getUserName(), new Date(), urmSignCoin.getSignCoin());
                //执行更新
                try {
                    //由于此方法不返回操作结果,只能判断异常来处理操作为成功的情况
                    //appService.updateUserInfo(urmUser);
                    appService.updateUrmSignCoin(urmSignCoin);
                } catch (Exception e) {
                    //异常,结束
                    logger.error("userSign(): update sign fail ,message is :{}", e.getMessage(), e);
                    Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                    return null;
                }
                long endTime = System.currentTimeMillis();
                logger.info("userSign(): user: {} sign, and cost {} ms. ", urmUser.getId(), endTime - startTime);
                jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
                jsonObject.put(ConstantUtil.API_NAME_MSG, "sign success ! ");
                //插入记录到签到历史表
                UserSignLog userSignLog = new UserSignLog(urmSignCoin.getUserId(), urmSignCoin.getLastSignTime());
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

    @RequestMapping("analysis/signRecord")
    public String getSignRecord(String fromDate, String toDate, HttpServletResponse response) throws ParseException {
        JSONObject jsonObject = new JSONObject();
        Long startTime;
        Long endTime;
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        jsonObject.put(ConstantUtil.API_NAME_MSG, "date error");
        if (StringUtils.isEmpty(fromDate) && StringUtils.isEmpty(toDate)) {
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        if (StringUtils.isEmpty(fromDate)) {
            jsonObject.put(ConstantUtil.API_NAME_MSG, "from date  not empty ");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fromDate).getTime();
        if (StringUtils.isEmpty(toDate)) {
            endTime = new Date().getTime();
        } else {
            endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(toDate).getTime();
        }
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
//        Query query = new Query();
//        query.addCriteria(Criteria.where("signDate").gte(startTime).lt(endTime));
        List<UserSignLog> userSignLogs = mongoDbManager.query(UserSignLog.class, new Query().addCriteria(Criteria.where("signDate").gte(startTime).lt(endTime)));
        Map<Long, Integer> map = new HashMap();
        if (userSignLogs != null && userSignLogs.size() > 0) {
            for (UserSignLog userSignLog : userSignLogs) {
                if (map.containsKey(userSignLog.getUserId())) {
                    map.put(userSignLog.getUserId(), map.get(userSignLog.getUserId()) + 1);
                } else {
                    map.put(userSignLog.getUserId(), 1);
                }
            }
        } else {
            jsonObject.put(ConstantUtil.API_NAME_MSG, "no data ");
        }
        jsonObject.put(ConstantUtil.API_NAME_DATA, JSON.toJSONString(map));
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;

    }

    /**
     * &
     * 手机用户邮箱和手机号
     *
     * @param response
     * @return
     */
    @RequestMapping("user/getEAndP")
    public String getUserEmailAndPhone(HttpServletResponse response,
                                       @RequestParam(defaultValue = ConstantUtil.API_DATA_EMPTYSTRING) String telephone,
                                       @RequestParam(defaultValue = ConstantUtil.API_DATA_EMPTYSTRING) String email) {
        long startTime = System.currentTimeMillis();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        if (StringUtils.isEmpty(telephone) && StringUtils.isEmpty(email)) {
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        } else {
            //1.userToken 验证和用户查询
            UrmUser urmUser;
            String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
            if (!StringUtils.isEmpty(userToken)) {
                //暂时不能读缓存,因为缓存中的记录不存在手机号和邮箱
                //2.get用户
                urmUser = appService.getUserByUserToken(userToken);
                if (urmUser != null) {

                    if (!StringUtils.isEmpty(telephone) && !telephone.equals(urmUser.getTelephone())) {
                        //update
                        urmUser.setTelephone(telephone);
                    }
                    if (!StringUtils.isEmpty(email) && !email.equals(urmUser.getEmail())) {
                        urmUser.setEmail(email);
                    }
                    appService.updateUserInfo(urmUser);
                }
            } else {
                //3.无,分析deviceId
                DeviceInfoVo deviceInfoVo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
                if (deviceInfoVo != null && !StringUtils.isEmpty(deviceInfoVo.getDeviceId())) {
                    //4.有,获取urmDevice记录
                    List<String> deviceIds = null;
                    String deviceIdsCacheKey = "DEVICEIDS_" + deviceInfoVo.getDeviceId().toUpperCase();
                    String deviceIdsCacheValue = urmDeviceService.get(deviceIdsCacheKey, 0);
                    if (!StringUtils.isEmpty(deviceIdsCacheValue)) {
                        JSONArray deviceCacheArrays = JSONArray.parseArray(deviceIdsCacheValue);
                        if (deviceCacheArrays != null) {
                            deviceIds = new ArrayList<>();
                            Collections.addAll(deviceIds, deviceCacheArrays.toArray(new String[]{}));
                        }
                    } else {
                        deviceIds = appService.getUserDevices(deviceInfoVo.getDeviceId());
                        if (deviceIds != null && deviceIds.size() > 0) {
                            urmDeviceService.add(deviceIdsCacheKey, JSONArray.toJSONString(deviceIds), TimeUtils.SECONDS_OF_1_DAY);
                        }
                    }
                    for (String deviceId : deviceIds) {
                        //5.获取用户设备关系
                        List<String> userIds = null;
                        String userIdsCacheKey = "DEVICEIDS_" + deviceId;
                        String userIdsCacheValue = urmDeviceService.get(userIdsCacheKey, 0);
                        if (!StringUtils.isEmpty(userIdsCacheValue)) {
                            JSONArray userCacheArrays = JSONArray.parseArray(userIdsCacheValue);
                            if (userCacheArrays != null) {
                                userIds = new ArrayList<>();
                                Collections.addAll(userIds, userCacheArrays.toArray(new String[]{}));
                            }
                        } else {
                            userIds = appService.getUserIdsByDeviceId(deviceId);
                            if (userIds != null && userIds.size() > 0) {
                                urmDeviceService.add(userIdsCacheKey, JSONArray.toJSONString(userIds), TimeUtils.SECONDS_OF_1_DAY);
                            }
                        }
                        for (String userid : userIds) {
                            UrmUser user = appService.getUserById(Long.valueOf(userid));
                            if (user != null) {
                                //6.用户是否已绑定邮箱和手机号
                                //7.未绑定,直接绑定
                                if (!StringUtils.isEmpty(telephone) && StringUtils.isEmpty(user.getTelephone())) {
                                    //update
                                    user.setTelephone(telephone);
                                }
                                if (!StringUtils.isEmpty(email) && StringUtils.isEmpty(user.getEmail())) {
                                    user.setEmail(email);
                                }
                                //8.已绑定,放弃
                                appService.updateUserInfo(user);
                            }
                        }
                    }
                }
            }
            //9.其他,放弃
        }
        System.out.println("totalTime : " + (BigDecimal.valueOf(System.currentTimeMillis()).subtract(BigDecimal.valueOf(startTime)).divide(BigDecimal.valueOf(1000), 1, BigDecimal.ROUND_HALF_UP)));
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    @RequestMapping(value = "user/login")
    public ModelAndView regOrLogin(@RequestParam(defaultValue = "4") int type, String email, String passwd, String userName) {
        //邮箱校验
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        //用户名为邮箱
        //现在用户名只能是邮箱
        if (StringUtils.isEmpty(passwd)) {
            //拒绝
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "password is required .");
            return modelAndView;
        }
        Map resultMap = new HashMap();
        //头像为默认头像,客户端默认,server不管
        switch (type) {
            case 0:
                //注册
                if (StringUtils.isEmpty(email)) {
                    //拒绝
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "email is required .");
                    return modelAndView;
                }
                if (!ApiUtils.emailCheck(email)) {
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "Please enter a valid email.");
                    return modelAndView;
                }
                UrmUser urmUser = appUserService.getUrmUserByEmail(email);
                if (urmUser != null) {
                    //已存在,拒绝
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "The mail has been registered");
                    return modelAndView;
                }
                //添加
                UrmUser newUrmUser = new UrmUser();
                newUrmUser.setType(1);
                newUrmUser.setEmail(email);
                newUrmUser.setCreateTime(new Date());
                newUrmUser.setUserName(email);
                newUrmUser.setUserToken(UUID.randomUUID().toString());
                newUrmUser.setPasswd(Md5Utils.md5AsBase64(passwd.getBytes()));
                try {
                    appUserService.insertUser(newUrmUser);
                    resultMap.put("userToken", newUrmUser.getUserToken());
                } catch (Exception e) {
                    System.out.println("add user failed ." + e.getMessage());
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    //注册失败
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "Sign up failed,Please try again.");
                    return modelAndView;
                }
            case 1:
                //登录,用户名现在是邮箱
                if (!ApiUtils.emailCheck(userName)) {
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "Please enter a valid email");
                    return modelAndView;
                }
                if (StringUtils.isEmpty(userName)) {
                    //拒绝
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "email can not be empty .");
                    return modelAndView;
                }
                UrmUser authedUrmUser;
                //如果是登录,但是是未注册的邮箱,应该显示 The mail is not registered.
                //按照用户名和type去搜索
                authedUrmUser = appUserService.getUrmUserByUserNameAndType(userName, 1);
                if (authedUrmUser == null) {
                    modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                    modelAndView.addObject(ConstantUtil.API_NAME_MSG, "The mail is not registered.");
                    return modelAndView;
                } else {
                    //按照用户名和密码与去搜索
                    //登录失败 Sign in failed,Please try again. , 暂时应该用不到
                    authedUrmUser = appUserService.getUrmUserByUserNameAndPwd(userName, Md5Utils.md5AsBase64(passwd.getBytes()));
                    if (authedUrmUser == null) {
                        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                        modelAndView.addObject(ConstantUtil.API_NAME_MSG, "The mail and password are inconsistent");
                        return modelAndView;
                    } else {
                        authedUrmUser.setUserToken(UUID.randomUUID().toString());
                        appUserService.updateUrmUser(authedUrmUser);
                        resultMap.put("userToken", authedUrmUser.getUserToken());
                    }
                }
            default:
        }
        modelAndView.addObject(ConstantUtil.API_NAME_DATA, resultMap);
        return modelAndView;
    }

    /**
     * 异常控制器
     *
     * @return
     */
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ModelAndView exceptionHandler(Exception e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, "fail," + e.getMessage());
        logger.error(Thread.currentThread().getId() + " : " + e.getMessage());
        String[] exception = ApiUtils.getException(e, 2);
        for (String str : exception) {
            System.out.println(str);
        }
        return modelAndView;
    }
}
