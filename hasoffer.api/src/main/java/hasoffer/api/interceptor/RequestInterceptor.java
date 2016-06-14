package hasoffer.api.interceptor;

import com.google.gson.Gson;
import hasoffer.api.controller.vo.DeviceEventVo;
import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.api.controller.vo.DeviceRequestVo;
import hasoffer.api.worker.DeviceRequestQueue;
import hasoffer.core.bo.enums.MarketChannel;
import hasoffer.core.user.IDeviceService;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);
    @Resource
    IDeviceService deviceService;

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        Gson gson = new Gson();

        String deviceInfoStr = Context.currentContext().getHeader("deviceinfo");
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
            String deviceId = deviceService.getDeviceId(deviceInfoVo.getDeviceId(), deviceInfoVo.getImeiId(), deviceInfoVo.getSerial());
            Context.currentContext().set(StaticContext.DEVICE_ID, deviceId);
        } catch (Exception e) {
            logger.error(String.format("RequestInterceptor Has Error: %s. request = [%s]. query = [%s] .device=[%s]",
                    e.getMessage(), requestUri, queryStr, deviceInfoStr));
            // + e.getMessage() + " , device = [" + deviceInfoStr + "]"
            return false;
        }

        return true;
    }

//    private void addLogToDB(DeviceRequestBo requestVo) {
//        ActorRef defaultActorRef = AkkaActorRef.getDefaultActorRef();
//        AkkaJobConfigMessage message = new AkkaJobConfigMessage(DeviceLogActor.class, 1);
//        defaultActorRef.tell(message, ActorRef.noSender());
//        AkkaJobMessage akkaJobMessage = new AkkaJobMessage(DeviceLogActor.class, requestVo);
//        defaultActorRef.tell(akkaJobMessage, ActorRef.noSender());
//    }

    private void recordClientEvent(DeviceRequestVo requestVo, HttpServletRequest request) {
        if (requestVo.getRequestUri().equals("/app/log")) {
            String event = request.getParameter("event");
            String info = request.getParameter("info");

            requestVo.setDeviceEvent(new DeviceEventVo(event, info));
        }
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
                           ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception ex)
            throws Exception {

    }
}