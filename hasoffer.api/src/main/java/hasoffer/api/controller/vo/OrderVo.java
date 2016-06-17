package hasoffer.api.controller.vo;

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
    private  Long Total;
    private  Long rate;
    private  int type;
    private  Long account;

    private Date orderTime;

    private  String createTime;

    public String getCreateTime() {
        return new SimpleDateFormat("MM/dd/YYYY HH:mm:ss").format(this.orderTime);
    }
    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long account) {
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

    public Long getTotal() {
        return Total;
    }

    public void setTotal(Long total) {
        Total = total;
    }

    public Long getRate() {
        return rate;
    }

    public void setRate(Long rate) {
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
                ", Total=" + Total +
                ", rate=" + rate +
                ", type=" + type +
                '}';
    }
    public OrderVo(Long account, String channel, String orderId, Long total, Long rate, int type) {
        this.account = account;
        this.channel = channel;
        this.orderId = orderId;
        Total = total;
        this.rate = rate;
        this.type = type;
    }

}
