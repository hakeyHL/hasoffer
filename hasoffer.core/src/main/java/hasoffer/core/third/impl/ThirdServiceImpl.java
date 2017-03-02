package hasoffer.core.third.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.app.vo.AppOfferOrderDetailVo;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.app.AppOfferStatistics;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSkuDetail;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.impl.CmpSkuServiceImpl;
import hasoffer.core.system.IAppService;
import hasoffer.core.third.ThirdService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.JsonHelper;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.fetch.helper.WebsiteHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hs on 2016/7/4.
 */
@Service
public class ThirdServiceImpl implements ThirdService {
    private static final String THIRD_GMOBI_DEALS = "SELECT t from AppDeal t where t.createTime <=?0  and t.expireTime >= ?1  ";
    private static final String str_createTime = "createTime";
    @Resource
    Hibernate4DataBaseManager hdm;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    CmpSkuServiceImpl cmpSkuService;
    @Resource
    IAppService appService;
    @Resource
    ApiUtils apiUtils;
    @Resource
    IPtmStdPriceService ptmStdPriceService;
    @Resource
    MongoDbManager mongoDbManager;

    Logger logger = LoggerFactory.getLogger(ThirdServiceImpl.class);
    @Resource
    private AppCacheService appCacheService;

    public String getDeals(String acceptJson) {
        JSONObject resJson = new JSONObject();
        StringBuilder sb = new StringBuilder();
        sb.append(THIRD_GMOBI_DEALS);
        if (StringUtils.isEmpty(acceptJson)) {
            logger.error(String.format("json parseException , %s is not a json String", acceptJson));
            resJson.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            resJson.put(ConstantUtil.API_NAME_MSG, "you should send a json String ,start with '{' and end with '}' ");
            return resJson.toJSONString();
        }
        JSONObject jsonObject = JSONObject.parseObject(acceptJson);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date createTime = new Date();
        try {
            if (StringUtils.isNotEmpty(jsonObject.getString(str_createTime))) {
                createTime = sf.parse(jsonObject.getString(str_createTime));
            }
        } catch (ParseException e) {
            logger.error("dataFormat  " + jsonObject.getString(str_createTime) + " to format yyyyMMddHHmmss failed ");
            resJson.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            resJson.put(ConstantUtil.API_NAME_MSG, "can't parse your createTime " + jsonObject.getString(str_createTime) + "  , because it is not the pattern as yyyyMMddHHmmss ");
            return resJson.toJSONString();
        }
        JSONArray sites;
        try {
            sites = jsonObject.getJSONArray("sites");
        } catch (Exception e) {
            logger.error(" sites is not a JsonArray String ");
            resJson.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            resJson.put(ConstantUtil.API_NAME_MSG, "required a Array like [\"a\",\"b\"] ");
            return resJson.toJSONString();
        }
        List dataList = new ArrayList();
        if (sites != null) {
            logger.error("has sites");
            sb.append(" and t.website=?2 ");
            sb.append(" order by createTime desc  ");
            for (int i = 0; i < sites.size(); i++) {
                List li = new ArrayList();
                Website website = Website.valueOf((String) sites.get(i));
                li.add(createTime);
                li.add(new Date());
                li.add(website);
                List<AppDeal> deals = hdm.query(sb.toString(), li);
                if (deals != null && deals.size() > 0) {
                    dataList.addAll(deals);
                }
            }
        } else {
            logger.error("no sites");
            sb.append(" order by createTime desc  ");
            List<AppDeal> deals = hdm.query(sb.toString(), Arrays.asList(createTime, new Date()));
            if (deals != null && deals.size() > 0) {
                dataList.addAll(deals);
            }
        }
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"push", "display"});
        for (AppDeal appDeal : (List<AppDeal>) dataList) {
            appDeal.setImageUrl(ImageUtil.getImageUrl(appDeal.getImageUrl()));
        }
        resJson.put("deals", dataList);
        resJson.put(ConstantUtil.API_NAME_ERRORCODE, "00000");
        resJson.put(ConstantUtil.API_NAME_MSG, "ok");
        return JSON.toJSONString(resJson, propertyFilter);
    }

    @Override
    public String listDealsForIndia(int page, int pageSize, String... filterProperties) {
        Map resultMap = new HashMap();
        Map dataMap = new HashMap();
          /* 返回数据为
                * id,deal类目名称、图片、名称、折扣值、deal的价格描述、点赞数、site、创建时间
           */
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultMap.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        //获取的是 有效的,display的,列表页图不为空的
        PageableResult<AppDeal> result = appService.getDeals(page, pageSize);
        //ArrayList,内部为数组实现,对元素快速随机访问
        List dealList = new ArrayList();
        for (AppDeal appDeal : result.getData()) {
            JSONObject dealJson = new JSONObject();
            getDealModel(appDeal, dealJson);
            dealList.add(dealJson);
        }
        dataMap.put("currentPage", result.getCurrentPage());
        dataMap.put("totalPage", result.getTotalPage());
        dataMap.put("offerList", dealList);
        resultMap.put(ConstantUtil.API_NAME_DATA, dataMap);
        if (filterProperties.length > 0) {
            PropertyFilter propertyFilter = JsonHelper.filterProperty(filterProperties);
            return JSON.toJSONString(resultMap, propertyFilter);
        }
        return JSON.toJSONString(resultMap);
    }

    private void getDealModel(AppDeal appDeal, JSONObject dealJson) {
        dealJson.put("id", appDeal.getId());
        dealJson.put("category", appDeal.getCategory());
        dealJson.put("image", appDeal.getListPageImage() == null ? ConstantUtil.API_DATA_EMPTYSTRING : ImageUtil.getImageUrl(appDeal.getListPageImage()));
        dealJson.put("title", appDeal.getTitle());
        dealJson.put("discount", appDeal.getDiscount());
        dealJson.put("priceDescription", appDeal.getPriceDescription() == null ? ConstantUtil.API_DATA_EMPTYSTRING : appDeal.getPriceDescription());
        dealJson.put("thumbCount", appDeal.getDealThumbNumber() == null ? 0 : appDeal.getDealThumbNumber());
        dealJson.put("website ", appDeal.getWebsite() == Website.UNKNOWN ? WebsiteHelper.getAllWebSiteString(appDeal.getLinkUrl()) : appDeal.getWebsite().name());
        dealJson.put(str_createTime, TimeUtils.getDifference2Date(new Date(), appDeal.getCreateTime()));
        dealJson.put("presentPrice", appDeal.getPresentPrice() == null ? 0 : appDeal.getPresentPrice());
        dealJson.put("originPrice", appDeal.getOriginPrice() == null ? 0 : appDeal.getOriginPrice());
        dealJson.put("couponCode", appDeal.getCouponCode() == null ? ConstantUtil.API_DATA_EMPTYSTRING : appDeal.getCouponCode());
    }

    @Override
    public String getDealInfo(String id, String marketChannel, String deviceId, String... filterProperties) {
        Map resultMap = new HashMap();
         /* 返回数据为
                * id,deal类目名称、详情页图片、名称、折扣值、deal的价格描述、点赞数、site、创建时间,跳转链接
           */
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        if (StringUtils.isEmpty(id)) {
            resultMap.put(ConstantUtil.API_NAME_MSG, "id required");
            return JSON.toJSONString(resultMap);
        }
        Long dealId = Long.valueOf(id);
        if (dealId > 0) {
            AppDeal appDeal = appService.getDealDetail(dealId);
            if (appDeal != null) {
                JSONObject dealJson = new JSONObject();
                getDealModel(appDeal, dealJson);
                dealJson.put("imageUrl", appDeal.getInfoPageImage() == null ? ConstantUtil.API_DATA_EMPTYSTRING : ImageUtil.getImageUrl(appDeal.getInfoPageImage()));
                dealJson.put("description", apiUtils.getPriceOffDealDes(appDeal));
                if (MarketChannel.INVENO.name().equals(marketChannel)) {

                }
                String deepLink = appDeal.getLinkUrl() == null ? ConstantUtil.API_DATA_EMPTYSTRING : WebsiteHelper.getDealUrlWithAff(appDeal.getWebsite(), appDeal.getLinkUrl(), new String[]{marketChannel, deviceId});
                logger.info("get offerInfo , deepLink is {}", deepLink);
                dealJson.put("deeplink", deepLink);
                resultMap.put(ConstantUtil.API_NAME_DATA, dealJson);
            } else {
                resultMap.put(ConstantUtil.API_NAME_MSG, "not found this deal, with id " + id);
                return JSON.toJSONString(resultMap);
            }
        }
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultMap.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        synchronized (this) {
            appService.recordOfferClickCount(MarketChannel.valueOf(marketChannel), Long.parseLong(id));
        }
        if (filterProperties != null && filterProperties.length > 0) {
            PropertyFilter propertyFilter = JsonHelper.filterProperty(filterProperties);
            return JSON.toJSONString(resultMap, propertyFilter);
        }
        return JSON.toJSONString(resultMap);
    }

    @Override
    public String listDealsForInveno(int page, int pageSize, String... filterProperties) {
        Map resultMap = new HashMap();
        Map dataMap = new HashMap();
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultMap.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        //获取的是 有效的,display的,列表页图不为空的
        PageableResult<AppDeal> result = appService.getDealsForMexico(page, pageSize);
        //ArrayList,内部为数组实现,对元素快速随机访问
        List dealList = new ArrayList();
        for (AppDeal appDeal : result.getData()) {
            JSONObject dealJson = new JSONObject();
            getDealModel(appDeal, dealJson);
            dealList.add(dealJson);
        }
        dataMap.put("currentPage", result.getCurrentPage());
        dataMap.put("totalPage", result.getTotalPage());
        dataMap.put("offerList", dealList);
        resultMap.put(ConstantUtil.API_NAME_DATA, dataMap);
        if (filterProperties.length > 0) {
            PropertyFilter propertyFilter = JsonHelper.filterProperty(filterProperties);
            return JSON.toJSONString(resultMap, propertyFilter);
        }
        return JSON.toJSONString(resultMap);
    }

    @Override
    public String listDealsForGmobi(int page, int pageSize, String... filterProperties) {
        Map resultMap = new HashMap();
        Map dataMap = new HashMap();
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultMap.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        //获取的是 有效的,display的,列表页图不为空的
        PageableResult<AppDeal> result = appService.getDeals(page, pageSize);
        //ArrayList,内部为数组实现,对元素快速随机访问
        List dealList = new ArrayList();
        for (AppDeal appDeal : result.getData()) {
            JSONObject dealJson = new JSONObject();
            getDealModel(appDeal, dealJson);
            dealList.add(dealJson);
        }
        dataMap.put("offerList", dealList);
        dataMap.put("currentPage", result.getCurrentPage());
        dataMap.put("totalPage", result.getTotalPage());
        resultMap.put(ConstantUtil.API_NAME_DATA, dataMap);
        if (filterProperties.length > 0) {
            PropertyFilter propertyFilter = JsonHelper.filterProperty(filterProperties);
            return JSON.toJSONString(resultMap, propertyFilter);
        }
        return JSON.toJSONString(resultMap);
    }

    @Override
    public String getOfferOrderInfo(Date dateStart, Date dateEnd, MarketChannel marketChannel) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultJsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);

        //一个map集合,用户存放yrm的vo对象
        Map<String, AppOfferOrderDetailVo> appOfferOrderDetailVoMap = new HashMap<>();

        BigDecimal orderTotal = BigDecimal.ZERO;
        BigDecimal commissionTotal = BigDecimal.ZERO;
        //获取内容如下
        //每天各个网站的订单数--属于GMobi的
        //订单金额
        //佣金金额
        //当日offer列表总展示次数
        //offer的点击总次数

        //分析每一天的订单的各个site的订单数
        List<OrderStatsAnalysisPO> orders = appService.getOrderDetailByAffId(dateStart, dateEnd, marketChannel);
        for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
            orderTotal = orderTotal.add(orderStatsAnalysisPO.getSaleAmount());
            commissionTotal = commissionTotal.add(orderStatsAnalysisPO.getTentativeAmount());

            String ymd = simpleDateFormat.format(orderStatsAnalysisPO.getOrderTime());
            //以ymd为key获取vo对象
            AppOfferOrderDetailVo appOfferOrderDetailVo = appOfferOrderDetailVoMap.get(ymd);
            if (appOfferOrderDetailVo == null) {
                appOfferOrderDetailVo = new AppOfferOrderDetailVo();
                appOfferOrderDetailVoMap.put(ymd, appOfferOrderDetailVo);
            }
            fillSiteOrderList(orderStatsAnalysisPO, appOfferOrderDetailVo);
            //算ymd的订单金额和佣金金额
            appOfferOrderDetailVo.setTotalOrderAmount(appOfferOrderDetailVo.getTotalOrderAmount().add(orderStatsAnalysisPO.getSaleAmount()));
            appOfferOrderDetailVo.setTotalCommissionAmount(appOfferOrderDetailVo.getTotalCommissionAmount().add(orderStatsAnalysisPO.getTentativeAmount()));
        }
        //请求返回次数,按日期范围查询
        List<AppOfferStatistics> offerRecords = appService.getOfferClickCountBetDate(dateStart, dateEnd, marketChannel);

        boolean isOfferOrderMapEmpty = false;
        Set<String> ymds = appOfferOrderDetailVoMap.keySet();
        if (ymds.size() < 1) {
            isOfferOrderMapEmpty = true;
        }
        for (AppOfferStatistics appOfferStatistics : offerRecords) {
            //遍历voMap的key与此ymd对照然后更新
            if (!isOfferOrderMapEmpty) {
                for (String ymd : ymds) {
                    if (ymd.equals(appOfferStatistics.getYmd())) {
                        AppOfferOrderDetailVo appOfferOrderDetailVo = appOfferOrderDetailVoMap.get(ymd);
                        appOfferOrderDetailVo.setClickCount(appOfferStatistics.getOfferClickCount());
                        appOfferOrderDetailVo.setShowCount(appOfferStatistics.getOfferScanCount());
                        appOfferOrderDetailVo.setListUV(appOfferStatistics.getScanUV());
                        appOfferOrderDetailVo.setInfoUV(appOfferStatistics.getClickUV());
                    }
                }
            } else {
                //即使没有订单也要返回点击和展示数据
                appOfferOrderDetailVoMap.put(appOfferStatistics.getYmd(), new AppOfferOrderDetailVo());
                AppOfferOrderDetailVo appOfferOrderDetailVo = appOfferOrderDetailVoMap.get(appOfferStatistics.getYmd());
                appOfferOrderDetailVo.setClickCount(appOfferStatistics.getOfferClickCount());
                appOfferOrderDetailVo.setShowCount(appOfferStatistics.getOfferScanCount());
                appOfferOrderDetailVo.setListUV(appOfferStatistics.getScanUV());
                appOfferOrderDetailVo.setInfoUV(appOfferStatistics.getClickUV());
            }
        }
        resultJsonObject.put(ConstantUtil.API_NAME_DATA, appOfferOrderDetailVoMap);
        return resultJsonObject.toJSONString();
    }

    /**
     * 获取热卖商品列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public String listTopSkusForNineApps(String page, String pageSize, Date updateTime, int commentNumber, String[] affs) {
        //从91mobile的sku中筛选状态是onsale , 评论数值大于1000,最近价格更新时间12小时以内按照更新时间降序返回
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultJsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        if (commentNumber == 0) {
            commentNumber = 1000;
        }
        if (updateTime == null) {
            //12个小时
            updateTime = new Date(new Date().getTime() - 1000 * 60 * 60 * 12);
        }
        PageableResult pagedTopPtmStdPrice = null;
        try {
            pagedTopPtmStdPrice = ptmStdPriceService.getPagedTopPtmStdPrice(page, pageSize, updateTime, commentNumber);
            System.out.println("dataSize = " + pagedTopPtmStdPrice.getData().size());
        } catch (NullPointerException e) {

        }
        List priceList = new LinkedList();
        PtmStdPrice ptmStdPrice;
        JSONObject dataJsonObj = new JSONObject();
        JSONObject jsonObject;
        if (pagedTopPtmStdPrice != null) {
            dataJsonObj.put("currentPage", pagedTopPtmStdPrice.getCurrentPage());
            dataJsonObj.put("totalPage", pagedTopPtmStdPrice.getTotalPage());
            for (Object stdPriceId : pagedTopPtmStdPrice.getData()) {
                //来源网站,原价,现价,折扣值,名称,图片,link
                //从缓存中获取ptmSTDPrice'
                String strPriceId = String.valueOf(stdPriceId);
                PtmStdSkuDetail ptmStdSkuDetail = mongoDbManager.queryOne(PtmStdSkuDetail.class, stdPriceId);
                if (ptmStdSkuDetail == null || ptmStdSkuDetail.getParamGroups() == null) {
                    continue;
                }
                ptmStdPrice = appCacheService.getPtmStdPrice(Long.parseLong(strPriceId));
                if (ptmStdPrice != null) {
                    jsonObject = new JSONObject();
                    jsonObject.put("id", ptmStdPrice.getId());
                    jsonObject.put("website", ptmStdPrice.getWebsite());
                    jsonObject.put("price", ptmStdPrice.getPrice());
                    jsonObject.put("title", ptmStdPrice.getTitle());
                    jsonObject.put("imageUrl", productCacheManager.getPtmStdPriceImageUrl(ptmStdPrice, false));
                    jsonObject.put("deepLink", WebsiteHelper.getDeeplinkWithAff(ptmStdPrice.getWebsite(), ptmStdPrice.getUrl(), affs));
                    priceList.add(jsonObject);
                }
            }
        }
        dataJsonObj.put("proList", priceList);
        resultJsonObject.put(ConstantUtil.API_NAME_DATA, dataJsonObj);
        return resultJsonObject.toJSONString();
    }

    @Override
    public List listBannerForNineApp() {
        List<AppBanner> banners = appService.getBannersForNineApp().getData();
        List bList = new LinkedList();
        for (AppBanner appBanner : banners) {
            JSONObject bannerJsonObj = new JSONObject();
            bannerJsonObj.put("rank", appBanner.getRank());
            bannerJsonObj.put("imageUrl", appBanner.getImageUrl() == null ? ConstantUtil.API_DATA_EMPTYSTRING : ImageUtil.getImageUrl(appBanner.getImageUrl()));
            bannerJsonObj.put("expireDate", appBanner.getDeadline());
            bannerJsonObj.put("id", Long.valueOf(appBanner.getSourceId()));
            bList.add(bannerJsonObj);
        }
        return bList;
    }

    /**
     * 获取ptmStdPrice详情
     *
     * @param stdPriceId
     * @return
     */
    @Override
    public JSONObject getPtmStdPriceInfo(long stdPriceId) {
        PtmStdPrice ptmStdPrice = appCacheService.getPtmStdPrice(stdPriceId);
        JSONObject jsonObject = new JSONObject();
        if (ptmStdPrice != null) {
            jsonObject.put("id", ptmStdPrice.getId());
            jsonObject.put("website", ptmStdPrice.getWebsite());
            jsonObject.put("price", ptmStdPrice.getPrice());
            if (ptmStdPrice.getOriPrice() > 0) {
                jsonObject.put("originPrice", ptmStdPrice.getOriPrice());
                jsonObject.put("discount", BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(ptmStdPrice.getPrice()).divide(BigDecimal.valueOf(ptmStdPrice.getOriPrice()), 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))));
            }
            jsonObject.put("title", ptmStdPrice.getTitle());
            jsonObject.put("imageUrl", productCacheManager.getPtmStdPriceImageUrl(ptmStdPrice, true));
            PtmStdSkuDetail ptmStdSkuDetail = mongoDbManager.queryOne(PtmStdSkuDetail.class, stdPriceId);
            Map<String, String> specsMap = new HashMap();
            ApiUtils.setParameters(specsMap, ptmStdSkuDetail.getParamGroups());
            jsonObject.put("specs", specsMap);
        }
        return jsonObject;
    }

    private void fillSiteOrderList(OrderStatsAnalysisPO orderStatsAnalysisPO, AppOfferOrderDetailVo appOfferOrderDetailVo) {
        //算每个site的订单数
        List<Map<String, Integer>> siteOrderList = appOfferOrderDetailVo.getSiteOrderList();
        if (siteOrderList.size() > 0) {
            for (Map<String, Integer> map : siteOrderList) {
                Integer siteOrderCount = map.get(orderStatsAnalysisPO.getWebSite());
                if (map.get(orderStatsAnalysisPO.getWebSite()) != null) {
                    //已经有此site的数据
                    siteOrderCount += 1;
                    map.put(orderStatsAnalysisPO.getWebSite(), siteOrderCount);
                } else {
                    map.put(orderStatsAnalysisPO.getWebSite(), 1);
                }
            }
        } else {
            Map<String, Integer> tempSiteOrderMap = new HashMap<>();
            tempSiteOrderMap.put(orderStatsAnalysisPO.getWebSite(), 1);
            siteOrderList.add(tempSiteOrderMap);
        }
    }
}
