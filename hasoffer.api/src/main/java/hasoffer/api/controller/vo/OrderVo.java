package hasoffer.api.controller.vo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hs on 2016/6/16.
 * ∂©µ•œÍ«Èvo
 */
public class OrderVo {
    public OrderVo() {
    }
    private  String channel;
    private  String orderId;
    private  BigDecimal rate;
    private  int type;
    private BigDecimal account;

    private BigDecimal total;

    private Date orderTime;

    private  String createTime;

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCreateTime() {
        return new SimpleDateFormat("MM/dd/YYYY HH:mm:ss").format(this.orderTime);
    }
    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public BigDecimal getAccount() {
        return account;
    }

    public void setAccount(BigDecimal account) {
        this.account = account;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "OrderVo{" +
                "account=" + account +
                ", channel='" + channel + '\'' +
                ", orderId='" + orderId + '\'' +
                ", rate=" + rate +
                ", type=" + type +
                '}';
    }
    public OrderVo(BigDecimal account, String channel, String orderId, BigDecimal rate, int type) {
        this.account = account;
        this.channel = channel;
        this.orderId = orderId;
        this.rate = rate;
        this.type = type;
    }

}
