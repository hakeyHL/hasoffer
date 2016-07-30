package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.*;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IHiJackReportService;
import hasoffer.core.admin.IHijackFetchService;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import hasoffer.core.persistence.mongo.StatDayAlive;
import hasoffer.core.persistence.mongo.StatDevice;
import hasoffer.core.persistence.mongo.StatHijackFetchCount;
import hasoffer.core.persistence.po.log.SkuUpdateLog;
import hasoffer.core.persistence.po.stat.StatPtmCmpSkuUpdate;
import hasoffer.core.product.ICmpSkuUpdateStatService;
import hasoffer.core.user.IDeviceService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.helper.PageHelper;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;


/**
 * Created on 2016/4/6.
 */

@Controller
@RequestMapping(value = "/showstat")
public class ShowStatController {

    private static final String DEFAULT_START_TIME = "2016-02-23";
    private static final String DEFAULT_END_TIME = TimeUtils.now("yyyy-MM-dd");
    private static final String DEFAULT_TIME = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    @Resource
    IDeviceService deviceService;

    @Resource
    IHiJackReportService hiJackReportService;

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    @Resource
    ICmpSkuUpdateStatService cmpSkuUpdateStatService;

    @Resource
    IHijackFetchService hijackFetchService;

    @RequestMapping(value = "/alive", method = RequestMethod.GET)
    public ModelAndView listStatistical(HttpServletRequest request,
                                        @RequestParam(defaultValue = "") String market,
                                        @RequestParam(defaultValue = "") String brand,
                                        @RequestParam(defaultValue = "") String os,
                                        @RequestParam(defaultValue = DEFAULT_START_TIME) String startTime,
                                        @RequestParam(defaultValue = "") String endTime,
                                        @RequestParam(defaultValue = "0") String sort,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "50") int size) {

        Map<String, String> params = new HashMap<String, String>();

        if (StringUtils.isEmpty(endTime)) {
            endTime = DEFAULT_END_TIME;
        }

        // convert yyyy-MM-dd to yyyyMMdd
        String YMD_WEB_PATTERN = "yyyy-MM-dd";
        String startYmd = TimeUtils.parse(TimeUtils.stringToDate(startTime, YMD_WEB_PATTERN), TimeUtils.PATTERN_YMD);
        String endYmd = TimeUtils.parse(TimeUtils.stringToDate(endTime, YMD_WEB_PATTERN), TimeUtils.PATTERN_YMD);

        params.put("startTime", startYmd);
        params.put("endTime", endYmd);
        params.put("brand", brand);
        params.put("os", os);
        params.put("marketChannel", market);
        params.put("sort", sort);

        PageableResult<DeviceAliveVo> pagedAliveVos = getAliveStatis(startYmd, endYmd, market, brand, os, page, size);

        long deviceWithShop = 0;
        long aliveDevices = 0;

        ModelAndView mav = new ModelAndView("showstat/alive");

        mav.addObject("aliveStats", pagedAliveVos.getData());

        mav.addObject("market", market);
        mav.addObject("startTime", startTime);
        mav.addObject("endTime", endTime);
        mav.addObject("sort", sort);
        mav.addObject("page", PageHelper.getPageModel(request, pagedAliveVos));

        return mav;
    }

    private PageableResult<DeviceAliveVo> getAliveStatis(String startYmd, String endYmd,
                                                         String marketChannel, String brand, String osVersion,
                                                         int page, int size) {

        if (StringUtils.isEmpty(marketChannel)) {
            marketChannel = "ALL";
        }
        if (StringUtils.isEmpty(brand)) {
            brand = "ALL";
        }
        if (StringUtils.isEmpty(osVersion)) {
            osVersion = "ALL";
        }

        PageableResult<StatDayAlive> pagedSdas = deviceService.findAliveStats(startYmd, endYmd, marketChannel, brand, osVersion, page, size, "time_desc");

        List<StatDayAlive> sdas = pagedSdas.getData();

        List<DeviceAliveVo> davos = new ArrayList<DeviceAliveVo>();

        for (StatDayAlive sda : sdas) {
            davos.add(new DeviceAliveVo(sda));
        }

        return new PageableResult<DeviceAliveVo>(davos, pagedSdas.getNumFund(), pagedSdas.getCurrentPage(), pagedSdas.getPageSize());
    }

    @RequestMapping(value = "/listHiJackReport", method = RequestMethod.GET)
    public ModelAndView listHijackReport(HttpServletRequest request,
                                         @RequestParam(defaultValue = "") String webSite,
                                         @RequestParam(defaultValue = "") String startTime,
                                         @RequestParam(defaultValue = "") String endTime,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "100") int size) {


        if (StringUtils.isEmpty(startTime)) {
            startTime = DEFAULT_TIME;
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = DEFAULT_TIME;
        }

        String YMD_WEB_PATTERN = "yyyy-MM-dd";
        Date startYmd = TimeUtils.stringToDate(startTime, YMD_WEB_PATTERN);
        Date endYmd = TimeUtils.stringToDate(endTime, YMD_WEB_PATTERN);
        PageableResult<Map<String, Object>> pagedHiJackList = hiJackReportService.selectPageableResult(webSite, startYmd, endYmd, page, size);
        List<HiJackReportVo> reportList = new ArrayList<HiJackReportVo>();
        for (Map<String, Object> po : pagedHiJackList.getData()) {
            try {
                HiJackReportVo vo = new HiJackReportVo();
//                BeanUtils.copyProperties(vo, po);
                vo.setCalStartTime(DateUtils.parseDate(po.get("calStartTime").toString(), "yyyy-MM-dd"));
                vo.setWebSite(po.get("webSite").toString());
                vo.setCaptureFail(Integer.valueOf(po.get("captureFail").toString()));
                vo.setCaptureMultipleSuccess(Integer.valueOf(po.get("captureMultipleSuccess").toString()));
                vo.setCaptureSingleSuccess(Integer.valueOf(po.get("captureSingleSuccess").toString()));
                vo.setDeeplinkCount(Integer.valueOf(po.get("deeplinkCount").toString()));
                vo.setDeeplinkDoubleCount(Integer.valueOf(po.get("deeplinkDoubleCount").toString()));
                vo.setDeeplinkExceptionCount(Integer.valueOf(po.get("deeplinkExceptionCount").toString()));
                vo.setDeeplinkNullCount(Integer.valueOf(po.get("deeplinkNullCount").toString()));
                vo.setPricelistCount(Integer.valueOf(po.get("pricelistCount").toString()));
                vo.setCmpSkuCount(Integer.valueOf(po.get("cmpSkuCount").toString()));
                vo.setRediToAffiliateUrlCount(Integer.valueOf(po.get("rediToAffiliateUrlCount").toString()));
                reportList.add(vo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PageableResult<HiJackReportVo> pageableResult = new PageableResult<HiJackReportVo>(reportList, pagedHiJackList.getNumFund(), pagedHiJackList.getCurrentPage(), pagedHiJackList.getPageSize());
        ModelAndView mav = new ModelAndView("showstat/listHiJackReport");
        mav.addObject("startTime", startTime);
        mav.addObject("endTime", endTime);
        mav.addObject("webSite", webSite);
        mav.addObject("size", size);
        mav.addObject("hiJackList", pageableResult.getData());
        mav.addObject("page", PageHelper.getPageModel(request, pageableResult));
        return mav;
    }

    @RequestMapping(value = "/listCmpskuStat", method = RequestMethod.GET)
    public ModelAndView listCmpskuStat(HttpServletRequest request,
                                       @RequestParam(defaultValue = DEFAULT_START_TIME) String startTime,
                                       @RequestParam(defaultValue = "") String market,
                                       @RequestParam(defaultValue = "") String endTime,
                                       @RequestParam(defaultValue = "10") int days,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "100") int size) {

        if (StringUtils.isEmpty(endTime)) {
            endTime = DEFAULT_END_TIME;
        }

        // convert yyyy-MM-dd to yyyyMMdd
        String YMD_WEB_PATTERN = "yyyy-MM-dd";
        String startYmd = TimeUtils.parse(TimeUtils.stringToDate(startTime, YMD_WEB_PATTERN), TimeUtils.PATTERN_YMD);
        String endYmd = TimeUtils.parse(TimeUtils.stringToDate(endTime, YMD_WEB_PATTERN), TimeUtils.PATTERN_YMD);
        PageableResult<CmpStatVo> pagedCmpskuList = getCmpskuStat(market, startYmd, endYmd, days, page, size);


        ModelAndView mav = new ModelAndView("showstat/listCmpskuStat");

        mav.addObject("startTime", startTime);
        mav.addObject("endTime", endTime);
        mav.addObject("size", size);
        mav.addObject("aliveStats", pagedCmpskuList.getData());
        mav.addObject("page", PageHelper.getPageModel(request, pagedCmpskuList));
        return mav;
    }

    @RequestMapping(value = "/listOrderReport", method = RequestMethod.GET)
    public ModelAndView listOrderStats(HttpServletRequest request,
                                       @RequestParam(defaultValue = "") String webSite,
                                       @RequestParam(defaultValue = "") String channel,
                                       @RequestParam(defaultValue = "") String startTime,
                                       @RequestParam(defaultValue = "") String endTime,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "100") int size) {


        if (StringUtils.isEmpty(startTime)) {
            startTime = DEFAULT_TIME;
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = DEFAULT_TIME;
        }
        webSite = webSite.toUpperCase();
        String orderStatus = "tentative";
        String YMD_WEB_PATTERN = "yyyy-MM-dd";
        Date startYmd = TimeUtils.stringToDate(startTime, YMD_WEB_PATTERN);
        Date endYmd = TimeUtils.stringToDate(endTime, YMD_WEB_PATTERN);
        PageableResult<Map<String, Object>> pagedHiJackList = orderStatsAnalysisService.selectPageableResult(webSite, channel, orderStatus, startYmd, endYmd, page, size);
        List<OrderStatsAnalysisVO> reportList = new ArrayList<OrderStatsAnalysisVO>();
        for (Map<String, Object> po : pagedHiJackList.getData()) {
            try {
                OrderStatsAnalysisVO vo = new OrderStatsAnalysisVO();
//                BeanUtils.copyProperties(vo, po);
                vo.setDateTime(po.get("dateTime").toString());
//                vo.setWebSite(po.get("webSite").toString());
//                vo.setChannel(po.get("channel").toString());
                vo.setSumCount(po.get("sumCount").toString());
                vo.setNewUserCount(po.get("newUserCount").toString());
                vo.setOldUserCount(po.get("oldUserCount").toString());
                vo.setNoneUserCount(po.get("noneUserCount").toString());
//                vo.setRediCount(po.get("rediCount").toString());
//                vo.setShopCount(po.get("shopCount").toString());
                vo.setGoogleChannel(po.get("googleChannel").toString() + " /" + po.get("googleOldChannel").toString() + " /" + po.get("googleNewChannel").toString() + " /" + po.get("googleNoneChannel").toString());
                vo.setShanchuanChannel(po.get("shanchuanChannel").toString() + " /" + po.get("shanchuanOldChannel").toString() + " /" + po.get("shanchuanNewChannel").toString() + " /" + po.get("shanchuanNoneChannel").toString());
                vo.setNineAppChannel(po.get("nineAppChannel").toString() + " /" + po.get("nineAppOldChannel").toString() + " /" + po.get("nineAppNewChannel").toString() + " /" + po.get("nineAppNoneChannel").toString());
                vo.setNoneChannel(po.get("noneChannel").toString());
                reportList.add(vo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PageableResult<OrderStatsAnalysisVO> pageableResult = new PageableResult<OrderStatsAnalysisVO>(reportList, pagedHiJackList.getNumFund(), pagedHiJackList.getCurrentPage(), pagedHiJackList.getPageSize());
        ModelAndView mav = new ModelAndView("showstat/listOrderReport");
        mav.addObject("startTime", startTime);
        mav.addObject("endTime", endTime);
        mav.addObject("webSite", webSite.toLowerCase());
        mav.addObject("channel", channel);
        mav.addObject("size", size);
        mav.addObject("orderList", pageableResult.getData());
        mav.addObject("page", PageHelper.getPageModel(request, pageableResult));
        return mav;
    }


    private PageableResult<CmpStatVo> getCmpskuStat(String marketChannel, String startYmd, String endYmd, int days, int page, int size) {
        Map<String, CmpStatVo> resultMap = new HashMap<String, CmpStatVo>();
        List<StatDevice> pagedSdas = deviceService.findCmpskuStat(marketChannel, days, startYmd, endYmd);
        for (StatDevice device : pagedSdas) {
            CmpStatVo cmpStatVo = resultMap.get(device.getDeviceId());
            if (cmpStatVo == null) {
                cmpStatVo = new CmpStatVo();
                cmpStatVo.setDeviceYmd(device.getDeviceYmd());
                cmpStatVo.setDeviceId(device.getDeviceId());
                cmpStatVo.setMarketChannel(device.getMarketChannel());
                resultMap.put(device.getDeviceId(), cmpStatVo);
            }
            cmpStatVo.getUseDaySet().add(device.getYmd());
        }
        List<CmpStatVo> resultList = new ArrayList<CmpStatVo>(resultMap.values());
        int begin = (page - 1) * size;
        if (begin < 0) {
            begin = 0;
        }
        int end = page * size;
        if (end >= resultList.size()) {
            end = resultList.size() - 1;
        }
        if (end < 0) {
            end = 0;
        }
//        return resultList.subList(begin, end);
        return new PageableResult<CmpStatVo>(resultList.subList(begin, end), resultList.size(), page, size);
    }

    /**
     * 日活分布
     *
     * @param request
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/distribution", method = RequestMethod.GET)
    public ModelAndView listDistribution(HttpServletRequest request,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        String baseDate = request.getParameter("baseDate");
        String marketChannelString = request.getParameter("marketChannel");
        String sort = request.getParameter("sort");

        if (StringUtils.isEmpty(baseDate)) {
            baseDate = DEFAULT_END_TIME;
        }
        if (StringUtils.isEmpty(sort)) {
            sort = "0";
        }

        int deviceNum = 0;//deviceService.totalAliveDevice(baseDate);
        int ratioNum = 0;// deviceService.totalRatioDevice(baseDate);

        Map<String, String> params = new HashMap<String, String>();
        params.put("baseDate", baseDate);
        params.put("marketChannel", marketChannelString);
        params.put("deviceNum", String.valueOf(deviceNum));
        params.put("ratioNum", String.valueOf(ratioNum));
        params.put("sort", sort);

        PageableResult<Map<String, Object>> pageResult = null;//deviceService.findStsAlive(params, page, size);

        ModelAndView mav = new ModelAndView("showstat/distribution");
        mav.addObject("deviceNum", deviceNum);
        mav.addObject("ratioNum", ratioNum);
        mav.addObject("data", pageResult.getData());
        mav.addObject("page", PageHelper.getPageModel(request, pageResult));

        if (StringUtils.isEmpty(marketChannelString)) {
            marketChannelString = "";
        }
        mav.addObject("marketChannelString", marketChannelString);
        mav.addObject("baseDate", baseDate);
        mav.addObject("sort", sort);

        return mav;
    }


    @RequestMapping(value = "/listskuupdate", method = RequestMethod.GET)
    public ModelAndView listSkuUpdate(HttpServletRequest request,
                                      @RequestParam(defaultValue = "ALL") String webSite,
                                      @RequestParam(defaultValue = "") String startTime,
                                      @RequestParam(defaultValue = "") String endTime,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "100") int size) throws ParseException {

        if (StringUtils.isEmpty(startTime)) {
            startTime = TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd");
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd");
        }

        ModelAndView modelAndView = new ModelAndView("showstat/listSkuUpdateStat");

        List<StatPtmCmpSkuUpdate> cmpUpdateList = new ArrayList<StatPtmCmpSkuUpdate>();
        // 获取idlist
        List<String> idList = getStatSkuUpdateIdList(startTime, endTime, webSite);
        // 通过idlist查询
        for (String idString : idList) {
            StatPtmCmpSkuUpdate cmpUpdateStat = cmpSkuUpdateStatService.findStatPtmCmpSkuUpdateById(idString);
            if (cmpUpdateStat != null) {
                cmpUpdateList.add(cmpUpdateStat);
            }
        }

        //封装一个返回前台的vo集合
        List<StatPtmCmpSkuUpdateVo> cmpUpdateVoList = getCmpUpdateStatVoList(cmpUpdateList);

        // 集合返回前台
        modelAndView.addObject("cmpUpdateVoList", cmpUpdateVoList);
        modelAndView.addObject("webSite", webSite.toLowerCase());
        modelAndView.addObject("startTime", startTime);
        modelAndView.addObject("endTime", endTime);

        return modelAndView;
    }

    private List<StatPtmCmpSkuUpdateVo> getCmpUpdateStatVoList(List<StatPtmCmpSkuUpdate> cmpUpdateList) {

        List<StatPtmCmpSkuUpdateVo> cmpUpdateStatVoList = new ArrayList<StatPtmCmpSkuUpdateVo>();

        for (StatPtmCmpSkuUpdate cmpUpdateStat : cmpUpdateList) {
            StatPtmCmpSkuUpdateVo cmpUpdateStatVo = getCmpUpdateVo(cmpUpdateStat);
            cmpUpdateStatVoList.add(cmpUpdateStatVo);
        }

        return cmpUpdateStatVoList;
    }

    private StatPtmCmpSkuUpdateVo getCmpUpdateVo(StatPtmCmpSkuUpdate cmpUpdateStat) {

        StatPtmCmpSkuUpdateVo cmpUpdateStatVo = new StatPtmCmpSkuUpdateVo();

        cmpUpdateStatVo.setDate(cmpUpdateStat.getDate());
        cmpUpdateStatVo.setWebsite(cmpUpdateStat.getWebsite());
        cmpUpdateStatVo.setOnSaleAmount(cmpUpdateStat.getOnSaleAmount());
        cmpUpdateStatVo.setSoldOutAmount(cmpUpdateStat.getSoldOutAmount());
        cmpUpdateStatVo.setOffsaleAmount(cmpUpdateStat.getOffsaleAmount());
        cmpUpdateStatVo.setUpdateSuccessAmount(cmpUpdateStat.getUpdateSuccessAmount());
        cmpUpdateStatVo.setAlwaysFailAmount(cmpUpdateStat.getAlwaysFailAmount());
        cmpUpdateStatVo.setNewSkuAmount(cmpUpdateStat.getNewSkuAmount());
        cmpUpdateStatVo.setIndexAmount(cmpUpdateStat.getIndexAmount());
        cmpUpdateStatVo.setNewIndexAmount(cmpUpdateStat.getNewIndexAmount());
        cmpUpdateStatVo.setUpdateTime(cmpUpdateStat.getUpdateTime());
        long all = cmpUpdateStat.getOnSaleAmount() + cmpUpdateStat.getSoldOutAmount() + cmpUpdateStat.getOffsaleAmount();
        if (all == 0) {
            cmpUpdateStatVo.setProportion(0);
        } else {
            long proportion = cmpUpdateStat.getUpdateSuccessAmount() * 100 / all;
            cmpUpdateStatVo.setProportion(proportion);
        }


        return cmpUpdateStatVo;
    }

    private List<String> getStatSkuUpdateIdList(String startTime, String endTime, String webSite) {

        List<String> idList = new ArrayList<String>();

        Date startDate = TimeUtils.stringToDate(startTime, "yyyy-MM-dd");
        Date endDate = TimeUtils.stringToDate(endTime, "yyyy-MM-dd");

        if (StringUtils.isEqual("ALL", webSite)) {
            while (startDate.getTime() <= endDate.getTime()) {
                for (Website website : WebsiteHelper.DEFAULT_WEBSITES) {
                    String todayString = TimeUtils.parse(startDate, "yyyyMMdd");
                    String id = HexDigestUtil.md5(website.name() + todayString);
                    idList.add(id);
                }
                startDate = TimeUtils.addDay(startDate, 1);
            }
        } else {
            while (startDate.getTime() <= endDate.getTime()) {
                for (Website website : WebsiteHelper.DEFAULT_WEBSITES) {
                    if (website.name().equals(webSite)) {
                        String todayString = TimeUtils.parse(startDate, "yyyyMMdd");
                        String id = HexDigestUtil.md5(website.name() + todayString);
                        idList.add(id);
                    }
                }
                startDate = TimeUtils.addDay(startDate, 1);
            }

        }

        return idList;
    }

    @RequestMapping(value = "/listsearchloghijacktest", method = RequestMethod.GET)
    public ModelAndView listSearchlogHijackTest(
            @RequestParam(defaultValue = "") String webSite,
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime) {

        ModelAndView modelAndView = new ModelAndView("showstat/hijackFetch");

        if (StringUtils.isEmpty(startTime)) {
            startTime = TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd");
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd");
        }

        Date startDate = TimeUtils.stringToDate(startTime, "yyyy-MM-dd");
        Date endDate = TimeUtils.stringToDate(endTime, "yyyy-MM-dd");

        List<String> idList = getStatHijackFetchResultIdList(startDate, endDate, webSite);
        List<StatHijackFetchCount> countList = new ArrayList<StatHijackFetchCount>();

        for (String id : idList) {

            StatHijackFetchCount statHijackCount = hijackFetchService.findStatHijackCount(id);
            countList.add(statHijackCount);
        }

        modelAndView.addObject("countList", countList);
        modelAndView.addObject("webSite", webSite);
        modelAndView.addObject("startTime", startTime);
        modelAndView.addObject("endTime", endTime);

        return modelAndView;
    }

    private List<String> getStatHijackFetchResultIdList(Date startDate, Date endDate, String webSite) {

        List<String> idList = new ArrayList<String>();

        if (StringUtils.isEmpty(webSite)) {//if null for 3 website

            List<Website> websiteList = new ArrayList<Website>();

            websiteList.add(Website.FLIPKART);
            websiteList.add(Website.SNAPDEAL);
            websiteList.add(Website.SHOPCLUES);

            for (Website website : websiteList) {
                List<String> idByDate = getIdByDate(startDate, endDate, website.name());
                idList.addAll(idByDate);
            }

        } else {//if not null get from startDate to endDate

            List<String> singleWebsiteIdList = getIdByDate(startDate, endDate, webSite.toUpperCase());
            idList.addAll(singleWebsiteIdList);
        }

        return idList;
    }

    private List<String> getIdByDate(Date startDate, Date endDate, String webSite) {

        List<String> idList = new ArrayList<String>();

        long dayStart = TimeUtils.getDayStart(startDate.getTime());
        long dayEnd = TimeUtils.getDayStart(endDate.getTime() + TimeUtils.MILLISECONDS_OF_1_DAY);

        while (dayStart < dayEnd) {

            String todayString = TimeUtils.parse(dayStart, "yyyyMMdd");

            String id = HexDigestUtil.md5(webSite + todayString);

            idList.add(id);
            dayStart += TimeUtils.MILLISECONDS_OF_1_DAY;
        }

        return idList;
    }

    @RequestMapping(value = "/skuvisitupdate", method = RequestMethod.GET)
    public ModelAndView skuVisitUpdate(@RequestParam(defaultValue = "") String webSite,
                                       @RequestParam(defaultValue = "") String startTime,
                                       @RequestParam(defaultValue = "") String endTime) {

        ModelAndView modelAndView = new ModelAndView("showstat/skuVisitUpdate");

        if (StringUtils.isEmpty(startTime)) {
            startTime = TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd");
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = TimeUtils.parse(TimeUtils.today(), "yyyy-MM-dd");
        }

        Date startDate = TimeUtils.stringToDate(startTime, "yyyy-MM-dd");
        Date endDate = TimeUtils.stringToDate(endTime, "yyyy-MM-dd");

        List<String> idList = new ArrayList<String>();

        if (StringUtils.isEmpty(webSite)) {
            for (Website website : WebsiteHelper.DEFAULT_WEBSITES) {
                idList.addAll(getIdByDate(startDate, endDate, website.name()));
            }
        } else {
            idList.addAll(getIdByDate(startDate, endDate, webSite.toUpperCase()));
        }


        List<SkuUpdateLog> logList = new ArrayList<SkuUpdateLog>();

        for (String id : idList) {

            SkuUpdateLog skuUpdateLog = cmpSkuUpdateStatService.findSkuUpdateLog(id);
            logList.add(skuUpdateLog);
        }

        modelAndView.addObject("logList", logList);
        modelAndView.addObject("webSite", webSite);
        modelAndView.addObject("startTime", startTime);
        modelAndView.addObject("endTime", endTime);

        return modelAndView;
    }

}
