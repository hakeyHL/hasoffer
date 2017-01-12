package hasoffer.datafixes.order.work;

import hasoffer.base.enums.MarketChannel;
import hasoffer.core.admin.IOrderStatsAnalysisService;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class OrderFixWorker {

    private static final Logger logger = LoggerFactory.getLogger(OrderFixWorker.class);

    @Resource
    IOrderStatsAnalysisService orderStatsAnalysisService;

    public void runTask(MarketChannel marketChannel, Date orderDate, Integer targetOrderNum) {

        //1. 记录该渠道已有的订单数目
        List<OrderStatsAnalysisPO> hasList = orderStatsAnalysisService.selectOrderList(DateFormatUtils.format(orderDate, "yyyy-MM-dd"), null, marketChannel.name());
        List<String> hasIdList = new ArrayList<>();
        for (OrderStatsAnalysisPO po : hasList) {
            hasIdList.add(po.getOrderId() + "_" + po.getWebSite());
        }
        //2. 查询当日订单数，优先从google，none，shanChuan的渠道中查询，并按照订单时间排列;
        List<OrderStatsAnalysisPO> otherOrderList = orderStatsAnalysisService.selectOrderList(DateFormatUtils.format(orderDate, "yyyy-MM-dd"), 0, marketChannel.GOOGLEPLAY.name(), MarketChannel.NONE.name(), marketChannel.SHANCHUAN.name());
        logger.info("order.size={}", otherOrderList.size());

        //3. 判断一共能有多少单
        int sumList = hasList.size() + otherOrderList.size();
        if (targetOrderNum > sumList) {
            targetOrderNum = sumList;
        }
        //3. 记录当日已有的订单ID, 轮询当日订单， 并取随机数，直到订单数达到预期数字
        List<OrderStatsAnalysisPO> waitInsertList = new ArrayList<>();
        Random random = new Random();
        while (hasIdList.size() <= targetOrderNum) {
            for (OrderStatsAnalysisPO po : otherOrderList) {
                if (random.nextInt(2) == 1 && !hasIdList.contains(po.getOrderId() + "_" + po.getWebSite())) {
                    hasIdList.add(po.getOrderId() + "_" + po.getWebSite());
                    waitInsertList.add(po);
                }
                if (hasIdList.size() > targetOrderNum) {
                    break;
                }
            }
        }

        //4. 将结果的订单插入到结果中。
        for (OrderStatsAnalysisPO po : waitInsertList) {
            po.setDataSource(2);
            po.setChannel(MarketChannel.VC.name());
            po.setChannelSrc(MarketChannel.VC.name());
            po.setId(null);
            orderStatsAnalysisService.insert(po);
        }
    }

}
