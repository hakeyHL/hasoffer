package hasoffer.admin.controller;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.ExcelUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping(value = "/orderStats")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;


    @RequestMapping(value = "/updateOrderReport", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView listOrderStats(HttpServletRequest request, @RequestParam(defaultValue = "") String webSite) {


        try {

            Date todayTime = new Date();

            //头一天
            //Date day1AgoTime = TimeUtils.addDay(todayTime, -1);
            //当天
            if (webSite == null || "".equals(webSite) || "ALL".equals(webSite) || Website.SNAPDEAL.name().equals(webSite.toUpperCase())) {
                //orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), todayTime, todayTime);
                for (int i = 0; i < 15; i++) {
                    orderStatsAnalysisService.updateOrder(Website.SNAPDEAL.name(), TimeUtils.addDay(todayTime, -i), TimeUtils.addDay(todayTime, -i));
                }
            }
            if (webSite == null || "".equals(webSite) || "ALL".equals(webSite) || Website.FLIPKART.name().equals(webSite.toUpperCase())) {
                for (int i = 0; i < 15; i++) {
                    orderStatsAnalysisService.updateOrder(Website.FLIPKART.name(), TimeUtils.addDay(todayTime, -i), TimeUtils.addDay(todayTime, -i));
                }
            }
        } catch (Exception e) {
            logger.debug("reportOrderStatistic:任务失败,   DATE:" + new Date() + ":具体如下");
            logger.debug(e.toString());
        }
        return new ModelAndView("showstat/listOrderReport");
    }

    @RequestMapping(value = "/importAmazonOrderList", method = RequestMethod.POST)
    public ModelAndView importAmazonOrder(MultipartFile multiFile) {

        ModelAndView modelAndView = new ModelAndView("showstat/listOrderReport");
        if (multiFile == null || multiFile.getOriginalFilename().length() == 0) {

            return modelAndView;
        }
        File temp = new File(System.getProperty("user.home") + File.separator + "temp" + File.separator + UUID.randomUUID().toString() + multiFile.getOriginalFilename().substring(multiFile.getOriginalFilename().lastIndexOf(".")));
        try {
            FileUtils.writeByteArrayToFile(temp, multiFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<OrderStatsAnalysisPO> orderModelList = new ArrayList<>();
        try {
            List<Map<String, String>> mapList = ExcelUtils.readRows(2, temp);
            Date startDate = null;
            Date endDate = null;
            for (Map<String, String> mapinfo : mapList) {
                String dateStr = mapinfo.get("5");
                Date date = null;
                if (dateStr != null) {
                    try {
                        date = DateUtils.parseDate(dateStr, "yyyy-MM-dd HH:mm:ss");
                    } catch (ParseException e) {
                        logger.error("Parse Date:{} is error.", dateStr, e);
                    }
                }
                if (startDate == null && date != null) {
                    startDate = date;
                }
                if (startDate != null && date != null && startDate.compareTo(date) > 0) {
                    startDate = date;
                }

                if (endDate == null && date != null) {
                    endDate = date;
                }
                if (endDate != null && date != null && endDate.compareTo(date) < 0) {
                    endDate = date;
                }


                OrderStatsAnalysisPO orderModel = new OrderStatsAnalysisPO();
                orderModel.setCategory(mapinfo.get("0"));
                orderModel.setTitle(mapinfo.get("1"));
                orderModel.setProductId(mapinfo.get("2"));
                //orderModel.setSeller(mapinfo.get("seller"));
                //TODO add the market channel.
//                orderModel.setAffID();
                orderModel.setChannel(MarketChannel.GOOGLEPLAY.name());
                orderModel.setOrderInTime(date);
                orderModel.setOrderTime(new Date(date.getTime() + TimeUtils.MILLISECONDS_OF_1_MINUTE * 150));
                orderModel.setWebSite(Website.AMAZON.name());
                orderModel.setUserType("NONE");
                orderModel.setOrderStatus("tentative");

                //orderModel.setShipped(mapinfo.get("shipped"));
                orderModel.setSaleAmount(new BigDecimal(mapinfo.get("6")));
                //orderModel.setItemsShipped(mapinfo.get("itemsShipped"));
                //orderModel.setReturns(mapinfo.get("returns"));
                orderModel.setTentativeAmount(new BigDecimal(mapinfo.get("9")));
                orderModelList.add(orderModel);
            }
            if (!ArrayUtils.isNullOrEmpty(orderModelList)) {
                orderStatsAnalysisService.importAmazonOrder(startDate, endDate, orderModelList);
            }
            FileUtils.deleteQuietly(temp);
        } catch (IOException e) {
            logger.error("import amazon order file:{} fail.", temp.getName(), e);
        }
        return modelAndView;

    }

}
