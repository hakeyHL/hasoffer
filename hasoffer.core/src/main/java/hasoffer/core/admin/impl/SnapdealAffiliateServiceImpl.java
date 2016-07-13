package hasoffer.core.admin.impl;

import hasoffer.affiliate.affs.IAffiliateProcessor;
import hasoffer.affiliate.affs.snapdeal.SnapdealProductProcessor;
import hasoffer.affiliate.affs.snapdeal.model.SnapDealAffiliateOrder;
import hasoffer.base.model.Website;
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
        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put(SnapdealProductProcessor.R_START_DATE, DateFormatUtils.format(startTime, "yyyy-MM-dd"));
        parameterMap.put(SnapdealProductProcessor.R_END_DATE, DateFormatUtils.format(endTime, "yyyy-MM-dd"));
        parameterMap.put(SnapdealProductProcessor.R_ORDER_STATUS, SnapdealProductProcessor.R_ORDER_STATUS_APPROVED);
        List<SnapDealAffiliateOrder> orderList = snapDealProcessor.getAffiliateOrderList(parameterMap);
        Set<String> deviceSet = new HashSet<String>();
        if (orderList != null) {
            for (SnapDealAffiliateOrder order : orderList) {
                if (order.getAffiliateSubId2() == null || "".equals(order.getAffiliateSubId2())) {
                    continue;
                }
                deviceSet.add(order.getAffiliateSubId2());
            }
            Collections.sort(orderList, new Comparator<SnapDealAffiliateOrder>() {
                public int compare(SnapDealAffiliateOrder arg0, SnapDealAffiliateOrder arg1) {
                    return arg0.getDateTime().compareTo(arg1.getDateTime());
                }
            });


            Map<String, UrmDevice> deviceRegTime = getDeviceRegTime(deviceSet);
            for (SnapDealAffiliateOrder order : orderList) {
                OrderStatsAnalysisPO po = new OrderStatsAnalysisPO();
                po.setWebSite(Website.SNAPDEAL.toString());
                po.setOrderId(order.getOrderCode());
                String channel = order.getAffiliateSubId1();
                po.setChannel(channel == null || "".equals(channel) ? "NONE" : channel);
                po.setOrderTime(order.getDateTime());
                String deviceId_userId = order.getAffiliateSubId2();
                if (deviceId_userId != null) {
                    String[] tempArray = deviceId_userId.split("_");
                    if (tempArray.length == 2) {
                        po.setDeviceId(tempArray[0]);
                        po.setUserId(tempArray[1]);
                    } else {
                        po.setDeviceId(tempArray[0]);
                    }
                }
                // OLD?NEW
                String deviceId = po.getDeviceId();
                UrmDevice device = null;
                if (deviceId != null) {
                    device = deviceRegTime.get(deviceId);
                }
                po.setUserType("NONE");
                if (device != null && device.getCreateTime().compareTo(startTime) > 0) {
                    po.setUserType("NEW");
                }
                if (device != null && device.getCreateTime().compareTo(startTime) <= 0) {
                    po.setUserType("OLD");
                }
                po.setSaleAmount(order.getSale());
                po.setCommissionRate(order.getCommissionRate());
                po.setTentativeAmount(order.getCommissionEarned());
                po.setOrderStatus(order.getStatus());
                orderPOList.add(po);
            }
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





}
