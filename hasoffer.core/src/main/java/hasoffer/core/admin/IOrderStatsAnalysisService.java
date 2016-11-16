package hasoffer.core.admin;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IOrderStatsAnalysisService {

    int insert(OrderStatsAnalysisPO po);

    int delete(String webSite, Date startTime, Date endTime);

    void updateOrder(String webSite, Date startTime, Date endTime);

    void importAmazonOrder(Date startTime, Date endTime, List<OrderStatsAnalysisPO> orderModelList);

    PageableResult<Map<String, Object>> selectPageableResult(String webSite, String channel, String orderStatus, Date startYmd, Date endYmd, int page, int size);

    void mergeOldUserOrderToNewUser(String oldUserId, String newUserId);

    @Transactional(rollbackFor = Exception.class)
    void updateOrderToLow(Date startTime, Date endTime, double targetAmount, double hour);

    @Transactional(rollbackFor = Exception.class)
    BigDecimal querySumOrderAmount(Date startTime, Date endTime);
}
