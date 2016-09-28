package hasoffer.api.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import hasoffer.api.controller.vo.DeviceEventVo;
import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.api.controller.vo.DeviceRequestVo;
import hasoffer.api.controller.vo.ResultVo;
import hasoffer.api.worker.DeviceRequestQueue;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.utils.DeviceUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.IAppService;
import hasoffer.core.user.IDeviceService;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class RequestInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);
    @Resource
    IDeviceService deviceService;
    @Resource
    IAppService appService;
    @Resource
    ICacheService<UrmUser> userICacheService;

    @Resource
    ICacheService<Map> urmDeviceService;

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        Gson gson = new Gson();

        String deviceInfoStr = Context.currentContext().getHeader("deviceinfo");
        String userToken = Context.currentContext().getHeader("usertoken");
        String requestUri = httpServletRequest.getRequestURI();
        String queryStr = httpServletRequest.getQueryString();
        try {
            DeviceInfoVo deviceInfoVo = gson.fromJson(deviceInfoStr, DeviceInfoVo.class);

            if (deviceInfoVo.getMarketChannel() == null) {
                deviceInfoVo.setMarketChannel(MarketChannel.OFFICIAL);
            }

            DeviceRequestVo requestVo = new DeviceRequestVo(deviceInfoVo, requestUri, queryStr);

            recordClientEvent(requestVo, httpServletRequest);

            if (!requestUri.equals("/app/config")) {
                DeviceRequestQueue.addLog(requestVo);
            }
//            System.out.println("RequestInterceptor.time1=" + System.currentTimeMillis());
//            addLogToDB(requestVo.getBo());
//            System.out.println("RequestInterceptor.time2=" + System.currentTimeMillis());
            Context.currentContext().set(Context.DEVICE_INFO, deviceInfoVo);

            //比价的设备 setContext
            String deviceId = DeviceUtils.getDeviceId(deviceInfoVo.getDeviceId(), deviceInfoVo.getImeiId(), deviceInfoVo.getSerial());
            Context.currentContext().set(StaticContext.DEVICE_ID, deviceId);

            if (!StringUtils.isEmpty(userToken)) {
                Context.currentContext().set(StaticContext.USER_TOKEN, userToken);

                //todo add cache
                UrmUser user = appService.getUserByUserToken(userToken);
                if (user != null) {
                    Context.currentContext().set(StaticContext.USER_ID, user.getId());
                }
            }

        } catch (Exception e) {
            logger.error(String.format("RequestInterceptor Has Error: %s. request = [%s]. query = [%s] .device=[%s]",
                    e.getMessage(), requestUri, queryStr, deviceInfoStr));
            // + e.getMessage() + " , device = [" + deviceInfoStr + "]"
            return false;
        }

        return true;
    }

    private void recordClientEvent(DeviceRequestVo requestVo, HttpServletRequest request) {
        if (requestVo.getRequestUri().equals("/app/log")) {
            String event = request.getParameter("event");
            String info = request.getParameter("info");

            requestVo.setDeviceEvent(new DeviceEventVo(event, info));
        }
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
                           ModelAndView modelAndView) throws Exception {
        //去除不必要的返回
        if (modelAndView != null && modelAndView.getModel().containsKey("searchCriteria")) {
            modelAndView.getModel().remove("searchCriteria");
        }
        UrmUser urmUser = null;
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        System.out.println("userToken is : " + userToken);
        if (StringUtils.isNotBlank(userToken)) {
            String key = "user_" + userToken;
            urmUser = userICacheService.get(UrmUser.class, key, 0);
            if (urmUser == null) {
                System.out.println("user not exist in cache ,query it from database ");
                urmUser = appService.getUserByUserToken(userToken);
                userICacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
            }
        }

        if (modelAndView == null) {
            modelAndView = new ModelAndView();
        }
        if (urmUser == null) {
            modelAndView.addObject("result", new ResultVo("10010", "login expired"));
        } else {
            System.out.println("userName  is " + urmUser.getUserName());
            System.out.println("----------------------------------" + JSON.parseObject(httpServletRequest.getHeader("deviceinfo")).toJSONString() + "-------------------");
            String gcmToken = JSON.parseObject(httpServletRequest.getHeader("deviceinfo")).getString("gcmToken");
            System.out.println("get gcmtoken ++++++++++++++++++++++++++++++++++++" + gcmToken + "++++++++++++++++++++++++++++++++");
            System.out.println("gcmtoken from database :" + urmUser.getGcmToken() == null ? "is null .." : urmUser.getGcmToken());
            //用户与gcmtoken绑定
            //1. 获取gcmtoken
            if (!StringUtils.isEmpty(gcmToken)) {
                //3. 不为空,比对
                if (urmUser.getGcmToken() == null) {
                    System.out.println("user'Gcmtoken not exist before ");
                    //5. 更新
                    urmUser.setGcmToken(gcmToken);
                    appService.updateUserInfo(urmUser);
                } else if (!urmUser.getGcmToken().equals(gcmToken)) {
                    System.out.println("update , not equal ");
                    //5. 更新
                    urmUser.setGcmToken(gcmToken);
                    appService.updateUserInfo(urmUser);
                }
                //4. 相等,过
            }
            //2. 为空,过
            modelAndView.addObject("result", new ResultVo("00000", "ok"));
                /*List<String> ids = null;
                String deviceId = JSON.parseObject(httpServletRequest.getHeader("deviceinfo")).getString("deviceId");
                String deviceKey = "urmDevice_ids_mapKey_" + deviceId;
                Map map = null;
                String deviceValue = urmDeviceService.get(deviceKey, 0);

                if (!StringUtils.isEmpty(deviceValue)) {
                    ids = new ArrayList<>();
                    JSONObject jsonObject = JSONObject.parseObject(deviceValue);
                    JSONArray urmDevice_ids1 = jsonObject.getJSONArray("urmDevice_ids");
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
                    System.out.println(" id id id :" + id);
                    boolean flag = false;
                    for (String dId : deviceIds) {
                        System.out.println(" dId dId dId :" + dId);
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
                System.out.println(" batch save  result size : " + count);*/
        }
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception ex)
            throws Exception {

    }
}