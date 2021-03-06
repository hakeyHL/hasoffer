package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.ApiHttpHelper;
import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.JsonHelper;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.vo.DeviceInfoVo;
import hasoffer.core.app.vo.PriceCurveVo;
import hasoffer.core.app.vo.PriceCurveXYVo;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.mongo.*;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.product.IPtmStdImageService;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.impl.ProductServiceImpl;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hs on 2016/7/25.
 * 专用于比价的Controller
 */
@Controller
@RequestMapping("sku")
public class AppSkuController {
    private static final String STRING_IMAGES = "images";
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IPtmCmpSkuImageService ptmCmpSkuImageService;
    @Resource
    ProductServiceImpl productService;
    @Resource
    IPtmStdPriceService ptmStdPriceService;
    @Resource
    IPtmStdImageService ptmStdImageService;
    @Resource
    MongoDbManager mongoDbManager;
    @Resource
    AppServiceImpl appService;
    Logger logger = LoggerFactory.getLogger(AppSkuController.class);

    public static List getImageArray(List<PtmCmpSkuImage> list) {
        List li = new ArrayList();
        if (list != null && list.size() > 0) {
            PtmCmpSkuImage ptmCmpSkuImage = list.get(0);
            String imagePath1 = ptmCmpSkuImage.getImagePath1();
            String imagePath2 = ptmCmpSkuImage.getImagePath2();
            String imagePath3 = ptmCmpSkuImage.getImagePath3();
            String imagePath4 = ptmCmpSkuImage.getImagePath4();
            if (!StringUtils.isEmpty(imagePath1)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath1()));
            }
            if (!StringUtils.isEmpty(imagePath2)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath2()));
            }
            if (!StringUtils.isEmpty(imagePath3)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath3()));
            }
            if (!StringUtils.isEmpty(imagePath4)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath4()));
            }
        }
        return li;
    }
    //计算当前x距离x轴起始点的距离
    public static int getDistance2X(Long priceX, Long wait2Consolve) {
        Long tempDateL = wait2Consolve - priceX;
        return BigDecimal.valueOf(tempDateL).divide(BigDecimal.valueOf(60 * 60 * 1000 * 24), BigDecimal.ROUND_HALF_UP).intValue();
    }

    public String getDateMMdd(Long time) {
        Date date = new Date();
        date.setTime(time);
        String format = null;
        try {
            format = new SimpleDateFormat("dd-MMM", Locale.ENGLISH).format(date);
        } catch (Exception e) {
            logger.error("transfer long date to MM-dd failed " + date);
        }
        return format;
    }

    /**
     * 根据sku的id获取sku详细信息
     *
     * @return
     */
    @RequestMapping("info")
    public String getSkuInfo(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        if (id <= 0) {
            jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            jsonObject.put(ConstantUtil.API_NAME_MSG, "id le zero ");
            ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        Map map = new HashMap<>();
        if (ApiUtils.removeBillion(id) > 0) {
            PtmStdPrice ptmStdPriceById = ptmStdPriceService.getPtmStdPriceById(ApiUtils.removeBillion(id));
            if (ptmStdPriceById != null) {
                PtmStdSkuDetail ptmCmpSkuDescription = mongoDbManager.queryOne(PtmStdSkuDetail.class, ptmStdPriceById.getStdSkuId());
                Map<String, String> specsMap = new HashMap();
                List<PtmStdSkuParamGroup> paramGroups = ptmCmpSkuDescription.getParamGroups();
                ApiUtils.setParameters(specsMap, paramGroups);
                map.put("specs", specsMap);//参数
            }
            List<PtmStdImage> skuImages = ptmStdImageService.getStdPriceImageByPriceId(ptmStdPriceById.getId());
            List<String> iamgeStringList = new ArrayList<>();
            for (PtmStdImage ptmStdImage : skuImages) {
                iamgeStringList.add(ImageUtil.getImageUrl(ptmStdImage.getSmallImagePath()));
            }
            if (skuImages != null && skuImages.size() > 0) {
                map.put(STRING_IMAGES, iamgeStringList);
            } else {
                List<PtmStdImage> stdPriceImageByPriceId = ptmStdImageService.getStdSkuImageBySkuId(ptmStdPriceById.getStdSkuId());
                if (stdPriceImageByPriceId != null) {
                    String imageUrl = ImageUtil.getImageUrl(stdPriceImageByPriceId.get(0).getSmallImagePath());
                    if (org.apache.commons.lang3.StringUtils.isNotEmpty(imageUrl)) {
                        map.put(STRING_IMAGES, Arrays.asList(imageUrl));
                    }
                }
            }
        } else {
            PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(id);
            if (ptmCmpSku != null) {
                logger.info(" has this sku " + id);
                PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, ptmCmpSku.getId());
                logger.info("get sku totalWeight from  mongo " + ptmCmpSkuDescription);
                if (ptmCmpSkuDescription != null) {
                    map.put("description", ptmCmpSkuDescription.getJsonDescription() == null ? ConstantUtil.API_DATA_EMPTYSTRING : ClientHelper.delHTMLTag(ptmCmpSkuDescription.getJsonDescription()));//描述
                    String tempJsonParam = ptmCmpSkuDescription.getJsonParam();
                    //去除html标签
                    if (!StringUtils.isEmpty(tempJsonParam)) {
                        tempJsonParam = ClientHelper.delHTMLTag(tempJsonParam);
                        map.put("specs", JsonHelper.getJsonMap(tempJsonParam));//参数
                    }
                }
                List<PtmCmpSkuImage> ptmCmpSkuImages = ptmCmpSkuImageService.findPtmCmpSkuImages(ptmCmpSku.getId());
                if (ptmCmpSkuImages != null && ptmCmpSkuImages.size() > 0) {
                    map.put(STRING_IMAGES, getImageArray(ptmCmpSkuImages));
                } else {
                    map.put(STRING_IMAGES, Arrays.asList(ptmCmpSku.getBigImagePath() == null ? ConstantUtil.API_DATA_EMPTYSTRING : ImageUtil.getImageUrl(ptmCmpSku.getBigImagePath())));
                }
            }
        }
        map.put("distribution", 5);
        jsonObject.put(ConstantUtil.API_NAME_DATA, JSONObject.toJSON(map));
        ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    /**
     * 获取sku的价格曲线
     *
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("curve")
    public String getPriceCurve(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        jsonObject.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        List<PriceNode> priceNodes = null;
        //1. 先拿到所有的价格数据
        if (id <= 0) {
            jsonObject.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            jsonObject.put(ConstantUtil.API_NAME_MSG, "id ls zero ");
            ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
        if (ApiUtils.removeBillion(id) > 0) {
            //从新的搜索
            PtmStdPriceHistoryPrice ptmStdPriceHistoryPrice = mongoDbManager.queryOne(PtmStdPriceHistoryPrice.class, ApiUtils.removeBillion(id));
            if (ptmStdPriceHistoryPrice != null) {
                priceNodes = ptmStdPriceHistoryPrice.getPriceNodes();
            }
        } else {
            priceNodes = cmpSkuService.queryHistoryPrice(id);
        }
        PtmStdPrice ptmStdPrice = null;
        if (priceNodes == null) {
            if ((id + ConstantUtil.API_DATA_EMPTYSTRING).length() >= 10) {
                ptmStdPrice = ptmStdPriceService.getPtmStdPriceById(ApiUtils.removeBillion(id));
            }
            if (ptmStdPrice != null) {
                //如果不存在历史价格数据将当前sku价格作为历史数据返回
                priceNodes = new ArrayList<>();
                priceNodes.add(new PriceNode(ptmStdPrice.getUpdateTime(), ptmStdPrice.getPrice()));
            } else {
                //如果不存在历史价格数据将当前sku价格作为历史数据返回
                priceNodes = new ArrayList<>();
                PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(id);
                if (ptmCmpSku != null) {
                    priceNodes.add(new PriceNode(ptmCmpSku.getUpdateTime(), ptmCmpSku.getPrice()));
                }
            }

        }
        boolean flag = false;
        if (priceNodes != null && priceNodes.size() != 0) {
            float referencePrice = priceNodes.get(0).getPrice();
            for (PriceNode priceNode : priceNodes) {
                if (referencePrice != priceNode.getPrice()) {
                    flag = true;
                }
            }
            Float maxPrice = Collections.max(priceNodes, new Comparator<PriceNode>() {
                @Override
                public int compare(PriceNode o1, PriceNode o2) {
                    if (o1.getPrice() < o2.getPrice()) {
                        return -1;
                    }
                    if (o1.getPrice() > o2.getPrice()) {
                        return 1;
                    }
                    return 0;
                }
            }).getPrice();

            Float minPrice = Collections.min(priceNodes, new Comparator<PriceNode>() {
                @Override
                public int compare(PriceNode o1, PriceNode o2) {
                    if (o1.getPrice() < o2.getPrice()) {
                        return -1;
                    }
                    if (o1.getPrice() > o2.getPrice()) {
                        return 1;
                    }
                    return 0;
                }
            }).getPrice();

            if (maxPrice - minPrice < 6) {
                //如果最大值与最小值相差小于6则不能很好地形成Y轴,视为一个价格
                flag = false;
            }

            if (flag) {
                PriceCurveVo priceCurveVo = getPriceCurveVo(priceNodes, false);
                priceCurveVo.setDistanceX2X(20);
                jsonObject.put(ConstantUtil.API_NAME_DATA, JSONObject.toJSON(priceCurveVo));
                ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            } else {
                PriceCurveVo priceCurveVo = getPriceCurveVo(priceNodes, true);
                priceCurveVo.setDistanceX2X(20);
                jsonObject.put(ConstantUtil.API_NAME_DATA, JSONObject.toJSON(priceCurveVo));
                ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
                return null;
            }

        }
        ApiHttpHelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    @RequestMapping("priceReport")
    public ModelAndView priceReport(@RequestParam(defaultValue = "0") long skuId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        Date currentDate = new Date();
        //要skuId
        if (skuId <= 0) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "is ls zero .");
        }


        PriceReportLog priceReportLog = new PriceReportLog();
        PriceReportStatistics priceReportStatistics;
        boolean flag = true;
        PriceReportStatistics statisticsReport = mongoDbManager.queryOne(PriceReportStatistics.class, skuId);
        if (statisticsReport != null) {
            priceReportStatistics = statisticsReport;
            Update update = new Update();
            update.set("updateTime", currentDate);
            update.set("updateStamp", currentDate.getTime());
            update.set("count", priceReportStatistics.getCount() + 1);
            mongoDbManager.update(PriceReportStatistics.class, priceReportStatistics.getId(), update);
            flag = false;
        } else {
            priceReportStatistics = new PriceReportStatistics();
        }
        //deviceId
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        if (deviceInfo != null) {
            String deviceId = deviceInfo.getDeviceId();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(deviceId)) {
                priceReportLog.setDeviceId(deviceId);
            }
        }
        //userId
        String userToken = (String) Context.currentContext().get(StaticContext.USER_TOKEN);
        UrmUser user = appService.getUserByUserToken(userToken);
        if (user != null) {
            priceReportLog.setUserId(user.getId());
        }

        priceReportLog.setSaveResult(ConstantUtil.API_NAME_MSG_SUCCESS);
        priceReportLog.setErrorMsg("no");
        priceReportLog.setId(skuId);
        priceReportLog.setTime(TimeUtils.parse(currentDate, "yyyyMMdd"));
        priceReportLog.setStamp(currentDate.getTime());

        if (flag) {
            priceReportStatistics.setId(skuId);
            priceReportStatistics.setTime(TimeUtils.parse(currentDate, "yyyyMMdd"));
            priceReportStatistics.setStamp(currentDate.getTime());
            priceReportStatistics.setSaveResult(ConstantUtil.API_NAME_MSG_SUCCESS);
            priceReportStatistics.setErrorMsg("no");
            priceReportStatistics.setCount(priceReportStatistics.getCount() + 1);
        }

        PtmStdPrice ptmStdPrice;
        PtmCmpSku ptmCmpSku;

        //校验skuId
        //根据skuId获取其商品Id
        if (ApiUtils.removeBillion(skuId) > 0) {
            ptmStdPrice = ptmStdPriceService.getPtmStdPriceById(ApiUtils.removeBillion(skuId));
            priceReportLog.setpId(ptmStdPrice.getStdSkuId());
            priceReportLog.setTitle(ptmStdPrice.getTitle());
            priceReportLog.setPrice(ptmStdPrice.getPrice());
            if (flag) {
                priceReportStatistics.setpId(ptmStdPrice.getStdSkuId());
                priceReportStatistics.setTitle(ptmStdPrice.getTitle());
                priceReportStatistics.setPrice(ptmStdPrice.getPrice());
            }
        } else {
            ptmCmpSku = cmpSkuService.getCmpSkuById(skuId);
            priceReportLog.setpId(ptmCmpSku.getProductId());
            priceReportLog.setTitle(ptmCmpSku.getTitle());
            priceReportLog.setPrice(ptmCmpSku.getPrice());

            if (flag) {
                priceReportStatistics.setpId(ptmCmpSku.getProductId());
                priceReportStatistics.setTitle(ptmCmpSku.getTitle());
                priceReportStatistics.setPrice(ptmCmpSku.getPrice());
            }
        }
        try {
            mongoDbManager.save(priceReportLog);
        } catch (Exception e) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "failed");
            Update update = new Update();
            update.set("saveResult", "failed");
            update.set("errorMsg", e.getMessage());
            mongoDbManager.update(PriceReportLog.class, priceReportLog.getId(), update);
        }
        if (flag) {
            try {
                priceReportStatistics.setUpdateTime(currentDate);
                priceReportStatistics.setCount(1);
                priceReportStatistics.setUpdateStamp(currentDate.getTime());
                mongoDbManager.save(priceReportStatistics);
            } catch (Exception e) {
                modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
                modelAndView.addObject(ConstantUtil.API_NAME_MSG, "failed");
                Update update = new Update();
                Date failDate = new Date();
                update.set("saveResult", "failed");
                update.set("errorMsg", e.getMessage());
                update.set("updateTime", failDate);
                update.set("updateStamp", failDate.getTime());
                mongoDbManager.update(PriceReportStatistics.class, priceReportStatistics.getId(), update);
            }
        }
        return modelAndView;
    }

    /**
     * 将给定集合整理出Y轴和坐标数据
     *
     * @return
     */
    public PriceCurveVo getPriceCurveVo(List<PriceNode> priceNodes, boolean isYSpecial) {

//        for (PriceNode priceNode : priceNodes) {
//            System.out.println(" TTT " + getDateMMdd(priceNode.getPriceTimeL()) + " PPP " + priceNode.getPrice());
//        }

//        System.out.println(" distinct list by price ");
        List<Long> Y = new ArrayList<>();
        BigDecimal maxPrice = BigDecimal.valueOf(Collections.max(priceNodes, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        }).getPrice());

        BigDecimal minPrice = BigDecimal.valueOf(Collections.max(priceNodes, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPrice() > o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() < o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        }).getPrice());


        //1. 创建一个临时列表,用于存储处理的结果
        List<PriceNode> tempPriceNodes = new ArrayList<>();
        //2. 将当期日期作为最后一个元素加入到列表中
        if (!getDateMMdd(priceNodes.get(priceNodes.size() - 1).getPriceTimeL()).equals(getDateMMdd(new Date().getTime()))) {
//            System.out.println(" add current date ");
            priceNodes.add(new PriceNode(new Date(), priceNodes.get(priceNodes.size() - 1).getPrice()));
        }

        sortPriceNoedesDateASC(priceNodes);

        List<String> X = new ArrayList<>();
        Long priceTimeL = priceNodes.get(priceNodes.size() - 1).getPriceTimeL();
//        System.out.println(" priceTimeL" + getDateMMdd(priceTimeL));

        //遍历日期
        int i = 4;
        //while (priceTimeL > priceNodes.get(0).getPriceTimeL()) {
        while (i > 0) {
            X.add(this.getDateMMdd(priceTimeL));
            priceTimeL = priceTimeL - 1000 * 60 * 60 * 24 * 20;
            i--;
        }
        //反转,按日期从小到大来
        Collections.reverse(X);

        //2.1 将第一个元素加入到列表
        tempPriceNodes.add(new PriceNode(priceNodes.get(0).getPriceTime(), priceNodes.get(0).getPrice()));
        //2.2 获取其长度
        int priceNodesSize = priceNodes.size();
//        System.out.println(" priceNodesSize  " + priceNodesSize);
//        for (PriceNode priceNode : priceNodes) {
//            System.out.println(" TAAA  " + getDateMMdd(priceNode.getPriceTimeL()) + " PAAAA " + priceNode.getPrice());
//        }
        //3.处理,添加辅助点
        for (int j = 1; j < priceNodesSize; j++) {
            PriceNode priceNo = priceNodes.get(j);
//            System.out.println("array " + j + "  is  " + getDateMMdd(priceNo.getPriceTimeL()) + "  and price is :" + priceNo.getPrice());
            //除了第一个,如果当前的前一天与上一个值不相同则增加前一天这个点
            long priorDateLong = priceNo.getPriceTimeL() - 1000 * 60 * 60 * 24;
            String priorDate = getDateMMdd(priorDateLong);
//            System.out.println(" priorDate " + priorDate);
            if (!priorDate.equals(getDateMMdd(priceNodes.get(j - 1).getPriceTimeL()))) {
                //3.1 如果如果当前元素的前一天日期与前一个元素的日期不相等,
//                System.out.println("not equal ");
                Date date = new Date();
                date.setTime(priorDateLong);
//                System.out.println("add node :  " + priorDate + " price " + priceNodes.get(j - 1).getPrice());
                PriceNode insertPriceNode = new PriceNode(date, priceNodes.get(j - 1).getPrice());
                //3.2 添加此辅助点,价格与前一个元素的价格相同
                if (!ifExistTime(getDateMMdd(date.getTime()), tempPriceNodes)) {
                    tempPriceNodes.add(insertPriceNode);
                }
                //3.3 将当前元素添加至临时列表中
                PriceNode tempPriceNode = new PriceNode(priceNo.getPriceTime(), priceNo.getPrice());
                if (!ifExistTime(getDateMMdd(priceNo.getPriceTimeL()), tempPriceNodes)) {
                    tempPriceNodes.add(tempPriceNode);
                }
            } else {
                //如果如果当前元素的前一天日期与前一个元素的日期相等,直接添加当前元素
                PriceNode tempPriceNode = new PriceNode(priceNo.getPriceTime(), priceNo.getPrice());
                if (!ifExistTime(getDateMMdd(priceNo.getPriceTimeL()), tempPriceNodes)) {
                    tempPriceNodes.add(tempPriceNode);
                }
            }
        }
        priceNodes = null;
        priceNodes = new ArrayList<>();
        priceNodes.addAll(tempPriceNodes);
//        System.out.println(" priceNodes " + priceNodes.size());
        BigDecimal middlePrice = (maxPrice.add(minPrice)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP);
        if (isYSpecial) {
            BigDecimal minY = BigDecimal.ZERO;
            //3.2 最大值 b
            BigDecimal maxY = BigDecimal.valueOf(priceNodes.get(0).getPrice() * 2);

            //比最小Y大一级的Y
            BigDecimal maxMinY = middlePrice.divide(BigDecimal.valueOf(2));
            //比最大Y小一级的Y
            BigDecimal minMaxY = middlePrice.add(maxMinY);

            Y.add(minY.longValue());
            Y.add(maxMinY.longValue());
            Y.add(middlePrice.longValue());
            Y.add(minMaxY.longValue());
            Y.add(maxY.longValue());

        } else {
            //3. 计算获得Y轴显示数据
            BigDecimal DIF = (middlePrice.subtract(minPrice)).multiply(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP));
            BigDecimal minY = minPrice.subtract(DIF);
            BigDecimal maxY = middlePrice.subtract(minY).add(middlePrice);
            //比最小Y大一级的Y
            BigDecimal maxMinY = minPrice.add(DIF);
            //比最大Y小一级的Y
            BigDecimal minMaxY = middlePrice.subtract(maxMinY).add(middlePrice);
            //Y轴
            Y.add(minY.longValue());
            Y.add(maxMinY.longValue());
            Y.add(middlePrice.longValue());
            Y.add(minMaxY.longValue());
            Y.add(maxY.longValue());
        }
        PriceCurveVo priceCurveVo = null;
        List<PriceCurveXYVo> priceXY = new ArrayList<PriceCurveXYVo>();
        sortPriceNoedesDateASC(priceNodes);
        if (priceNodes != null && priceNodes.size() > 0) {
            for (PriceNode priceNode : priceNodes) {
//                System.out.println(" Time :" + getDateMMdd(priceNode.getPriceTimeL()) + " price :" + priceNode.getPrice());
                //查询到价格历史,开始分析priceTimeL
                PriceCurveXYVo priceCurveXYVo = new PriceCurveXYVo(this.getDateMMdd(priceNode.getPriceTimeL()), BigDecimal.valueOf(priceNode.getPrice()).longValue(), getDistance2X(priceTimeL, priceNode.getPriceTimeL()));
                priceXY.add(priceCurveXYVo);
            }
            priceCurveVo = new PriceCurveVo(X, Y, priceXY, minPrice.longValue(), maxPrice.longValue());
            priceCurveVo.setDistanceX2X(20);
        }
        return priceCurveVo;
    }

    public List sortPriceNoedesDateASC(List<PriceNode> priceNodes) {
        //过滤不合理数据
        Iterator<PriceNode> iterator = priceNodes.iterator();
        while (iterator.hasNext()) {
            PriceNode priceNode = iterator.next();
            if (priceNode.getPrice() <= 0) {
                iterator.remove();
            }
        }

        Collections.sort(priceNodes, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPriceTimeL() < o2.getPriceTimeL()) {
                    return -1;
                } else if (o1.getPriceTimeL() > o2.getPriceTimeL()) {
                    return 1;
                }
                return 0;
            }
        });
        return priceNodes;
    }

    public boolean ifExistTime(String time, List<PriceNode> priceNodes) {
        boolean flag = false;
        for (PriceNode priceNode : priceNodes) {
            if (getDateMMdd(priceNode.getPriceTimeL()).equals(time)) {
                flag = true;
            }
        }
        return flag;
    }
}
