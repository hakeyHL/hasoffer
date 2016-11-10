package hasoffer.core.admin.impl;

import hasoffer.affiliate.affs.IAffiliateProcessor;
import hasoffer.affiliate.affs.snapdeal.SnapdealProductProcessor;
import hasoffer.affiliate.affs.snapdeal.model.SnapDealAffiliateOrder;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.AffliIdHelper;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.ISnapdealAffiliateService;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.user.IDeviceService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
public class SnapdealAffiliateServiceImpl implements ISnapdealAffiliateService {

    private IAffiliateProcessor<SnapDealAffiliateOrder> snapDealProcessor = new SnapdealProductProcessor();

    @Resource
    private IDeviceService deviceService;

    @Override
    public List<OrderStatsAnalysisPO> countOrderList(Date startTime, Date endTime) {
        List<OrderStatsAnalysisPO> orderPOList = new ArrayList<OrderStatsAnalysisPO>();
        List<SnapDealAffiliateOrder> orderList = new ArrayList<>();
        orderList.addAll(getOrderList("82856", "09bf4a55fafe2ccc3c077e2ea48642", SnapdealProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
        orderList.addAll(getOrderList("89037", "c1050bf0b2c9b2f64c9c1e950ff53c", SnapdealProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
        orderList.addAll(getOrderList("104658", "2e89ca44b33433e2dfc953713ac472", SnapdealProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
        orderList.addAll(getOrderList("112338", "afb1c380c01ba806dc5c14e8c1d018", SnapdealProductProcessor.R_ORDER_STATUS_APPROVED, startTime, endTime));
        Set<String> deviceSet = new HashSet<String>();
        for (SnapDealAffiliateOrder order : orderList) {
            String affExtParam2 = order.getAffiliateSubId2();
            if (affExtParam2 == null || "".equals(affExtParam2)) {
                continue;
            }
            String[] tempArray = affExtParam2.split("_");
            if (deviceSet != null) {
                if (tempArray.length == 3) {
                    deviceSet.add(tempArray[1]);
                } else {
                    deviceSet.add(tempArray[0]);
                }
            }
        }
        Map<String, UrmDevice> deviceRegTime = getDeviceRegTime(deviceSet);

        Collections.sort(orderList, new Comparator<SnapDealAffiliateOrder>() {
            public int compare(SnapDealAffiliateOrder arg0, SnapDealAffiliateOrder arg1) {
                return arg0.getDateTime().compareTo(arg1.getDateTime());
            }
        });


        for (SnapDealAffiliateOrder order : orderList) {
            OrderStatsAnalysisPO po = new OrderStatsAnalysisPO();
            po.setAffID(order.getAffId());
            po.setWebSite(Website.SNAPDEAL.toString());
            po.setOrderId(order.getOrderCode());
            String channel = order.getAffiliateSubId1();
            po.setChannelSrc(channel);
            if (channel == null || "".equals(channel)) {
                po.setChannel(MarketChannel.NONE.name());
            } else {
                po.setChannel(MarketChannel.valueOfString(channel).name());
            }
            if (MarketChannel.NONE.name().equals(po.getChannel())) {
                po.setChannel(AffliIdHelper.getMarketChannelById(channel).name());
            }
            po.setOrderInTime(order.getDateTime());
            po.setOrderTime(new Date(po.getOrderInTime().getTime() + TimeUtils.MILLISECONDS_OF_1_MINUTE * 150));
            String deviceId_userId = order.getAffiliateSubId2();
            if (deviceId_userId != null) {
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
            po.setSaleAmount(order.getSale());
            po.setTitle(order.getProduct());
            po.setCategory(order.getCategory());
            po.setCommissionRate(order.getCommissionRate());
            po.setTentativeAmount(order.getCommissionEarned());
            po.setOrderStatus(order.getStatus());
            orderPOList.add(po);
        }
        return orderPOList;

    }

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

    private List<SnapDealAffiliateOrder> getOrderList(String affId, String token, String orderStatus, Date startTime, Date endTime) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Snapdeal-Token-Id", token);
        headerMap.put("Snapdeal-Affiliate-Id", affId);
        headerMap.put("Accept", "application/json");

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put(SnapdealProductProcessor.R_START_DATE, DateFormatUtils.format(startTime, "yyyy-MM-dd"));
        parameterMap.put(SnapdealProductProcessor.R_END_DATE, DateFormatUtils.format(endTime, "yyyy-MM-dd"));
        //parameterMap.put(SnapdealProductProcessor.R_ORDER_STATUS, SnapdealProductProcessor.R_ORDER_STATUS_APPROVED);
        parameterMap.put(SnapdealProductProcessor.R_ORDER_STATUS, orderStatus);

        return snapDealProcessor.getAffiliateOrderList(headerMap, parameterMap);
    }

}
