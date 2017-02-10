package hasoffer.api.controller;

import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.enums.MarketChannel;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.third.impl.ThirdServiceImpl;
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

/**
 * Created by hs on 2016/7/4.
 */
@Controller
@RequestMapping(value = "/third")
public class OfferController {
    @Resource
    ThirdServiceImpl thirdService;

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
        Httphelper.sendJsonMessage(result, response);
        return null;
    }

    //offer for india

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
        String deviceId = ClientHelper.getAndroidId();
        DeviceInfoVo deviceInfo = ClientHelper.getDeviceInfo();
        MarketChannel marketChannel = deviceInfo.getMarketChannel();
        String[] filterProperties = new String[]{};
        switch (marketChannel) {
            case GMOBI:
                filterProperties[0] = "category";
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
     * @param dateFrom 开始日期
     * @param dateTo   结束日期
     * @param response
     * @return
     */
    @RequestMapping(value = "/offer/orderInfo", method = RequestMethod.GET)
    public String getOrderInfo(
            @DateTimeFormat(pattern = "yyyyMMdd") Date dateFrom,
            @DateTimeFormat(pattern = "yyyyMMdd") Date dateTo,
            HttpServletResponse response) {
        //如果起始日期或者结束日期为空则默认返回昨天开始30天的数据
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(currentCalendar.YEAR, Calendar.MONTH, Calendar.DATE);

        Date dateEnd = currentCalendar.getTime();
        Date dateStart = new Date(dateEnd.getTime() - 1000 * 60 * 60 * 24 * 30);

        if (dateFrom != null && dateTo != null) {
            dateStart = dateFrom;
            dateEnd = dateTo;
        }
        String orderInfo = thirdService.getOfferOrderInfo(dateStart, dateEnd);
        Httphelper.sendJsonMessage(orderInfo, response);
        return null;
    }
}
