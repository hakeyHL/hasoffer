package hasoffer.api.controller;

import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.utils.AffliIdHelper;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.system.IAppService;
import hasoffer.core.third.impl.ThirdServiceImpl;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.CipherUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hs on 2016/7/4.
 */
@Controller
@RequestMapping(value = "/third")
public class OfferController {
    @Resource
    ThirdServiceImpl thirdService;
    @Resource
    IAppService appService;

    public static void main(String[] args) {
        String key = "GMOBI20170213142800HRGI";
        String s = CipherUtil.encryptWithSHA256(key);
        System.out.println(s);
    }

    //offer for india

    /**
     * provide API to get deals for Gmobi
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/gmobi")
    public String config(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int pageSize,
                         HttpServletResponse response) {
        String result = thirdService.getDealsForGmobi(page, pageSize, new String[]{"discount", "category"});
        //增加返回次数
        appService.recordOfferReturnCount(ClientHelper.getDeviceInfo().getMarketChannel());
        Httphelper.sendJsonMessage(result, response);
        return null;
    }

    /**
     * get方式获取deal列表数据
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/india", method = RequestMethod.GET)
    public String getDealsForIndia(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   HttpServletResponse response) {
        String dealsForIndia = thirdService.getDealsForIndia(page, pageSize, "originPrice", "presentPrice");
        Httphelper.sendJsonMessage(dealsForIndia, response);
        return null;
    }

    @RequestMapping(value = "/offer/dealInfo/{id}", method = RequestMethod.GET)
    public String getDealsForIndia(@PathVariable("id") String id,
                                   HttpServletResponse response) {
        if (StringUtils.isEmpty(id) || !StringUtils.isNumericSpace(id)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            jsonObject.put(ConstantUtil.API_NAME_MSG, "id is empty");
            jsonObject.put(ConstantUtil.API_NAME_DATA, new HashMap<>());
            Httphelper.sendJsonMessage(jsonObject.toJSONString(), response);
            return null;
        }
        String deviceId = ClientHelper.getAndroidId();
        DeviceInfoVo deviceInfo = ClientHelper.getDeviceInfo();
        MarketChannel marketChannel = deviceInfo.getMarketChannel();
        String[] filterProperties = null;
        switch (marketChannel) {
            case GMOBI:
                filterProperties = new String[]{"category"};
                break;
//            case INVENO:
//                break;
            default:
        }
        String dealInfoForIndia = thirdService.getDealInfo(id, deviceInfo.getMarketChannel().name(), deviceId, filterProperties);
        Httphelper.sendJsonMessage(dealInfoForIndia, response);
        return null;
    }

    /**
     * mexico offer list
     *
     * @param page
     * @param pageSize
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/inveno", method = RequestMethod.GET)
    public String getDealsForMexico(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    HttpServletResponse response) {
        String dealsForMexico = thirdService.getDealsForInveno(page, pageSize, new String[]{"discount", "originPrice", "presentPrice"});
        Httphelper.sendJsonMessage(dealsForMexico, response);
        return null;
    }

    /**
     * 获取订单详情
     *
     * @param dateFrom  开始日期
     * @param dateTo    结束日期
     * @param response
     * @param key       加密后的key
     * @param timestamp 时间戳
     * @return
     */
    @RequestMapping(value = "/offer/orderInfo")
    public String getOrderInfo(
            @DateTimeFormat(pattern = "yyyyMMdd") Date dateFrom,
            @DateTimeFormat(pattern = "yyyyMMdd") Date dateTo,
            String key,
            String timestamp,
            HttpServletResponse response) {

        //验证key和时间戳
        DeviceInfoVo deviceInfo = ClientHelper.getDeviceInfo();
        boolean accessed = CipherUtil.validationWithSHA256(deviceInfo.getMarketChannel(), key, timestamp);
        if (!accessed) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            jsonObject.put(ConstantUtil.API_NAME_MSG, "request refused.");
            jsonObject.put(ConstantUtil.API_NAME_DATA, new HashMap<>());
            Httphelper.sendJsonMessage(jsonObject.toJSONString(), response);
            return null;
        }
        //如果起始日期或者结束日期为空则默认返回昨天开始30天的数据
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(currentCalendar.MONTH), currentCalendar.get(currentCalendar.DATE), 0, 0, 0);

        Date dateEnd = currentCalendar.getTime();
        Date dateStart = new Date(dateEnd.getTime() - (1000 * 60 * 60 * 24 * 30l));
        if (dateFrom != null && dateTo != null) {
            dateStart = dateFrom;
            dateEnd = dateTo;
        }
        String[] affIds = null;
        if (deviceInfo != null && deviceInfo.getMarketChannel() != null) {
            affIds = AffliIdHelper.getAffIdsByChannel(deviceInfo.getMarketChannel());
        }
        String orderInfo = thirdService.getOfferOrderInfo(dateStart, dateEnd, affIds, deviceInfo.getMarketChannel());
        Httphelper.sendJsonMessage(orderInfo, response);
        return null;
    }
}
