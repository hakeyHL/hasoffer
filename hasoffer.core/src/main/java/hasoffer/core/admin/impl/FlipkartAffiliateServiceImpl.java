package hasoffer.core.admin.impl;

import hasoffer.affiliate.affs.IAffiliateProcessor;
import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.AffiliateOrder;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.AffliIdHelper;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IFlipkartAffiliateService;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.user.IDeviceService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class FlipkartAffiliateServiceImpl implements IFlipkartAffiliateService {

    private final Logger logger = LoggerFactory.getLogger(FlipkartAffiliateServiceImpl.class);

    private IAffiliateProcessor<AffiliateOrder> flipProcessor = new FlipkartAffiliateProductProcessor();

    @Resource
    private IDeviceService deviceService;

    private static MarketChannel getChannelByAffId(String affId) {
        if (affId == null) {
            return MarketChannel.NONE;
        }
        if (Arrays.asList(AffliIdHelper.FLIKART_SHANCHUAN_FLIDS).contains(affId)) {
            return MarketChannel.SHANCHUAN;
        } else if (Arrays.asList(AffliIdHelper.FLIKART_NINEAPPS_FLIDS).contains(affId)) {
            return MarketChannel.NINEAPPS;
        } else if (Arrays.asList(AffliIdHelper.FLIKART_LEO_FLIDS).contains(affId)) {
            return MarketChannel.LEO;
        } else if (Arrays.asList(AffliIdHelper.FLIKART_GOOGLEPLAY_FLIDS).contains(affId)) {
            return MarketChannel.GOOGLEPLAY;
        } else if (Arrays.asList(AffliIdHelper.FLIKART_ZUK_FLIDS).contains(affId)) {
            return MarketChannel.ZUK;
        }
        return MarketChannel.NONE;

    }

//    private List<UrmDeviceRequestLog> getLogMap(String deviceId, Date before24H, Date endTime) {
//
//        List<UrmDeviceRequestLog> rediToAffiliateList = getRediToAffiliateTime(deviceId, before24H, endTime);
//        List<UrmDeviceRequestLog> shopList = getShopTime(deviceId, before24H, endTime);
//        List<UrmDeviceRequestLog> logList = new ArrayList<UrmDeviceRequestLog>();
//
//        for (UrmDeviceRequestLog log : rediToAffiliateList) {
//            logList.add(log);
//        }
//        for (UrmDeviceRequestLog log : shopList) {
//            logList.add(log);
//        }
//
//        Collections.sort(logList, new Comparator<UrmDeviceRequestLog>() {
//            public int compare(UrmDeviceRequestLog arg0, UrmDeviceRequestLog arg1) {
//                return arg0.getCreateTime().compareTo(arg1.getCreateTime());
//            }
//        });
//        return logList;
//    }
//
//    private String getOrderType(String orderDate, List<UrmDeviceRequestLog> logList) {
//
//        String result = "NONE";
//        if (logList == null || orderDate == null) {
//            return result;
//        }
//        for (UrmDeviceRequestLog log : logList) {
//            if (log.getQuery().contains("action=shop")) {
//                result = "SHOP";
//            } else if (log.getQuery().contains("action=rediToAffiliateUrl")) {
//                result = "REDI";
//            }
//            break;
//        }
//        try {
//            Date tempDate = DateUtils.parseDate(orderDate, "dd-MM-yyyy hh:mm:ss");
//
//            Iterator<UrmDeviceRequestLog> sListIterator = logList.iterator();
//            while (sListIterator.hasNext()) {
//                UrmDeviceRequestLog log = sListIterator.next();
//                if (log.getCreateTime().compareTo(tempDate) < 0) {
//                    sListIterator.remove();
//                }
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//
//
//    private String getUsertype(String affExtParam2, Map<String, UrmDevice> startTime) {
//
//        return "OLD";
//    }

    @Override
    public List<OrderStatsAnalysisPO> countOrderList(Date startTime, Date endTime) {
        List<OrderStatsAnalysisPO> orderPOList = new ArrayList<OrderStatsAnalysisPO>();
//        Date before24H = new Date(startTime.getTime() - 1000 * 60 * 60 * 24);
        List<AffiliateOrder> orderList = new ArrayList<>();
        try {
            orderList.addAll(getOrderList("affiliate357", "56e46c994b92488c91e43fad138d5c71", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("affiliate357", "56e46c994b92488c91e43fad138d5c71", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over affiliate357: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("xyangryrg", "c9c9b3d833054bf490c9989ac602b852", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("xyangryrg", "c9c9b3d833054bf490c9989ac602b852", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over xyangryrg: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("zhouxixi0", "55b1c6fa469b4e0296bb00259faf4056", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("zhouxixi0", "55b1c6fa469b4e0296bb00259faf4056", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over zhouxixi0: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("harveyouo", "c54bfd150ea74047a9233a4c3c3d356c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("harveyouo", "c54bfd150ea74047a9233a4c3c3d356c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over harveyouo: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("allenooou", "857de2a9c43e40bfbdf572db3d728db4", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("allenooou", "857de2a9c43e40bfbdf572db3d728db4", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over allenooou: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("747306881", "f3ec71e03799496d8b73c38b5456fb0b", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("747306881", "f3ec71e03799496d8b73c38b5456fb0b", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 747306881: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("hlhakeygm", "01cfa560bac247eaa8a37f57fa8149f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("hlhakeygm", "01cfa560bac247eaa8a37f57fa8149f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over hlhakeygm: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("oliviersl", "6cf21891892d4bd8b839d85d51ac809c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("oliviersl", "6cf21891892d4bd8b839d85d51ac809c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over oliviersl: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("wuningSFg", "04bece2ed64945a3bce45c2f51293ef0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("wuningSFg", "04bece2ed64945a3bce45c2f51293ef0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over wuningSFg: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("115377600", "2efcc3cffcca49f5b692a214b4d44cbd", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("115377600", "2efcc3cffcca49f5b692a214b4d44cbd", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 115377600: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("120527343", "56dfb6e061e8410a936acf4081e19e4f", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("120527343", "56dfb6e061e8410a936acf4081e19e4f", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 120527343: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("160082642", "997ec82cf2a445f2bd19b541d4514569", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("160082642", "997ec82cf2a445f2bd19b541d4514569", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 160082642: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("286867656", "cc0326abeff94e8493ef2d8888eac75d", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("286867656", "cc0326abeff94e8493ef2d8888eac75d", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 286867656: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("289063282", "3f6f61bf3142475a9ab9a990fb51dcf0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("289063282", "3f6f61bf3142475a9ab9a990fb51dcf0", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 289063282: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("514330076", "1fa77bd9ed754b27966704a30caea9a6", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("514330076", "1fa77bd9ed754b27966704a30caea9a6", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 514330076: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("602074420", "32b388fcd7a945478510208ef7f60300", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("602074420", "32b388fcd7a945478510208ef7f60300", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 602074420: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("943546560", "a344714f1aef45449e779fa8511686f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("943546560", "a344714f1aef45449e779fa8511686f8", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over 943546560: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("sunyukunj", "74140dfa56194bc4abab5c2d105fd12c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("sunyukunj", "74140dfa56194bc4abab5c2d105fd12c", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over sunyukunj: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("gczyfw201", "52932fcc543f45719cc0a6ffd8ad7794", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("gczyfw201", "52932fcc543f45719cc0a6ffd8ad7794", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over gczyfw201: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("huangmint", "d098f6096fbd40048741b7673f1391bc", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("huangmint", "d098f6096fbd40048741b7673f1391bc", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over huangmint: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("zhangchen", "e50a16f7a9ac4ef396097806ffe18e7a", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("zhangchen", "e50a16f7a9ac4ef396097806ffe18e7a", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over zhangchen: order.size={}, date:{} ", orderList.size(), startTime);
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("wangshuom", "fb22aa12b24249ab88a051070c314ab5", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("wangshuom", "fb22aa12b24249ab88a051070c314ab5", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over wangshuom: order.size={}", orderList.size());
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("abbott1981", "5cf8f2cf2b3b4dc1a8af9ece67a57698", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("abbott1981", "5cf8f2cf2b3b4dc1a8af9ece67a57698", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over abbott1981: order.size={}", orderList.size());

            orderList.addAll(getOrderList("raymondzh", "850098b32c044bd2a519eb976064709b", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_TENTATIVE, startTime, endTime));
            TimeUnit.SECONDS.sleep(3);
            orderList.addAll(getOrderList("raymondzh", "850098b32c044bd2a519eb976064709b", FlipkartAffiliateProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
            logger.info("over raymondzh: order.size={}", orderList.size());


        } catch (InterruptedException e) {
            logger.error("Get OrderTime error. Msg:{}", e);
        }
        Set<String> deviceSet = new HashSet<>();
        for (AffiliateOrder order : orderList) {
            String affExtParam2 = order.getAffExtParam2();
            if (affExtParam2 == null || "".equals(affExtParam2)) {
                continue;
            }
            String[] tempArray = affExtParam2.split("_");
            // 只有一个值的时候，肯定是设备ID
            // 有两个的时候，因为在后面加了一个用户
            // 有三个的时候，因为在首页劫持加了网盟，第一个参数表示渠道，第二个表示设备，第三个表示用户，所以，将来统一改成三个。
            if (tempArray.length == 1) {
                deviceSet.add(tempArray[0]);
            } else if (tempArray.length == 2) {
                deviceSet.add(tempArray[0]);
            } else if (tempArray.length == 3) {
                deviceSet.add(tempArray[1]);
            }
        }

        Map<String, UrmDevice> deviceRegTime = getDeviceRegTime(deviceSet);

        // 因为导出Excel需要按照时间降序排列，所以要排序。
        Collections.sort(orderList, new Comparator<AffiliateOrder>() {
            public int compare(AffiliateOrder arg0, AffiliateOrder arg1) {
                return arg0.getOrderDate().compareTo(arg1.getOrderDate());
            }
        });

        for (AffiliateOrder order : orderList) {
            OrderStatsAnalysisPO po = new OrderStatsAnalysisPO();
            po.setWebSite(Website.FLIPKART.name());
            po.setOrderId(order.getAffiliateOrderItemId());
            po.setAffID(order.getAffID());
            try {
                po.setOrderInTime(DateUtils.parseDate(order.getOrderDate(), "dd-MM-yyyy HH:mm:ss"));
                po.setOrderTime(new Date(po.getOrderInTime().getTime() + TimeUtils.MILLISECONDS_OF_1_MINUTE * 150));
            } catch (ParseException e) {
                logger.error("Get OrderTime error. Msg:{}", e);
            }
            String extParam1 = order.getAffExtParam1();
            String channel = extParam1 == null || "".equals(extParam1) ? MarketChannel.NONE.name() : extParam1;
            po.setChannel(channel);
            po.setChannelSrc(channel);
            try {
                if ((MarketChannel.NONE.name().equals(po.getChannel()) || MarketChannel.LeoMaster.name().equals(po.getChannel())) && po.getOrderTime().after(DateUtils.parseDate("2016-09-09 00:00:00", "yyyy-MM-dd HH:mm:ss"))) {
                    MarketChannel channelByAffId = getChannelByAffId(po.getAffID());
                    po.setChannel(channelByAffId.name());
                }
            } catch (ParseException e) {
                logger.error("Get channel error. Msg:{}", e);
            }
            String deviceId_userId = order.getAffExtParam2();
            if (deviceId_userId != null && !"".equals(deviceId_userId)) {
                String[] tempArray = deviceId_userId.split("_");
                if (tempArray.length == 1) {
                    po.setDeviceId(tempArray[0]);
                } else if (tempArray.length == 2) {
                    po.setDeviceId(tempArray[0]);
                    po.setUserId(tempArray[1]);
                } else if (tempArray.length == 3) {
                    po.setChannel(AffliIdHelper.getMarketChannelById(tempArray[0]).name());
                    po.setDeviceId(tempArray[1]);
                    po.setUserId(tempArray[2]);
                }
            }
            po.setOrderStatus("processed".equals(order.getStatus()) ? "approved" : order.getStatus());
            po.setCategory(order.getCategory());
            po.setTitle(order.getTitle());
            po.setProductId(order.getProductId());
            // OLD?NEW
            String deviceId = po.getDeviceId();
            UrmDevice device = null;
            if (deviceId != null) {
                device = deviceRegTime.get(deviceId);
            }
            if (device != null) {
                po.setDeviceRegTime(device.getCreateTime());
                po.setVersion(device.getAppVersion());
            }
            po.setUserType("NONE");
            if (device != null && device.getCreateTime().compareTo(startTime) > 0) {
                po.setUserType("NEW");
            }
            if (device != null && device.getCreateTime().compareTo(startTime) <= 0) {
                po.setUserType("OLD");
            }
            po.setSaleAmount(new BigDecimal(order.getSaleAmount()));
            po.setCommissionRate(new BigDecimal(order.getCommissionRate()));
            po.setTentativeAmount(new BigDecimal(order.getTentativeAmount()));
            orderPOList.add(po);
        }
        return orderPOList;

    }

    private List<AffiliateOrder> getOrderList(String affId, String token, String orderState, Date startTime, Date endTime) {
        logger.info("countOrderList: affid={},token={},startTime={},endTime={}", affId, token, startTime, endTime);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Fk-Affiliate-Id", affId);
        headerMap.put("Fk-Affiliate-Token", token);
        Map<String, String> approvedParamMap = new HashMap<>();
        approvedParamMap.put(FlipkartAffiliateProductProcessor.R_START_DATE, DateFormatUtils.format(startTime, "yyyy-MM-dd"));
        approvedParamMap.put(FlipkartAffiliateProductProcessor.R_END_DATE, DateFormatUtils.format(endTime, "yyyy-MM-dd"));
        approvedParamMap.put(FlipkartAffiliateProductProcessor.R_ORDER_STATUS, orderState);
        approvedParamMap.put(FlipkartAffiliateProductProcessor.R_OFFSET, "0");
        return flipProcessor.getAffiliateOrderList(headerMap, approvedParamMap);
    }

//    private List<UrmDeviceRequestLog> getShopTime(String deviceId, Date startTime, Date endTime) {
//        List<UrmDeviceRequestLog> result = new ArrayList<UrmDeviceRequestLog>();
//        Query query = new Query();
//        query.addCriteria(Criteria.where("deviceId").is(deviceId));
//        query.addCriteria(Criteria.where("createTime").gte(startTime).lt(endTime));
//        final String regex = "action=shop*";
//        query.addCriteria(Criteria.where("query").regex(regex));
//        List<UrmDeviceRequestLog> tempList = mongoDbManager.query(UrmDeviceRequestLog.class, query);
//        for(UrmDeviceRequestLog log:tempList){
//            if(log.getQuery().contains("FLIPKART")){
//                result.add(log);
//            }
//        }
//        return result;
//
//    }
//
//    private List<UrmDeviceRequestLog> getRediToAffiliateTime(String deviceId, Date startTime, Date endTime) {
//        List<UrmDeviceRequestLog> result = new ArrayList<UrmDeviceRequestLog>();
//        Query query = new Query();
//        query.addCriteria(Criteria.where("deviceId").is(deviceId));
//        query.addCriteria(Criteria.where("createTime").gte(startTime).lt(endTime));
//        final String regex = "action=rediToAffiliateUrl*";
//        query.addCriteria(Criteria.where("query").regex(regex));
//        List<UrmDeviceRequestLog> tempList = mongoDbManager.query(UrmDeviceRequestLog.class, query);
//        for(UrmDeviceRequestLog log:tempList){
//            if(log.getQuery().contains("FLIPKART")){
//                result.add(log);
//            }
//        }
//        return result;
//    }

    private Map<String, UrmDevice> getDeviceRegTime(Set<String> deviceSet) {
        Map<String, UrmDevice> deviceMap = new HashMap<String, UrmDevice>();
        List<UrmDevice> deviceByIdList = deviceService.findDeviceByIdList(new ArrayList<String>(deviceSet));
        if (deviceByIdList == null) {
            return deviceMap;
        }
        for (UrmDevice device : deviceByIdList) {
            deviceMap.put(device.getId(), device);
        }
        return deviceMap;
    }


}
