package hasoffer.core.app.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2017年02月10日.
 * Time 16:38
 * 用于包装返回offer的订单详情
 */
public class AppOfferOrderDetailVo {
    //ymd的offer请求次数
    private long showCount;
    //ymd的offer点击总次数
    private long clickCount;
    //ymd的每个site的订单数
    private List<Map<String, Integer>> siteOrderList = new ArrayList<>();
    //ymd的订单金额
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;
    //ymd的佣金金额
    private BigDecimal totalCommissionAmount = BigDecimal.ZERO;

    public long getShowCount() {
        return showCount;
    }

    public void setShowCount(long showCount) {
        this.showCount = showCount;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }

    public List<Map<String, Integer>> getSiteOrderList() {
        return siteOrderList;
    }

    public void setSiteOrderList(List<Map<String, Integer>> siteOrderList) {
        this.siteOrderList = siteOrderList;
    }

    public BigDecimal getTotalOrderAmount() {
        return totalOrderAmount;
    }

    public void setTotalOrderAmount(BigDecimal totalOrderAmount) {
        this.totalOrderAmount = totalOrderAmount;
    }

    public BigDecimal getTotalCommissionAmount() {
        return totalCommissionAmount;
    }

    public void setTotalCommissionAmount(BigDecimal totalCommissionAmount) {
        this.totalCommissionAmount = totalCommissionAmount;
    }
}
