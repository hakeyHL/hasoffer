package hasoffer.core.persistence.po.admin;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "report_ordersatas")
public class OrderStatsAnalysisPO implements Identifiable<Integer> {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String affID;

    @Column(length = 20, nullable = false)
    private String webSite;


    @Column(length = 30, nullable = false)
    private String channel;

    @Column(length = 30)
    private String channelSrc;

    @Column(length = 200)
    private String affExtParam1;

    @Column(length = 200)
    private String affExtParam2;

    @Column(length = 100)
    private String userId;

    /**
     * 新用户（NEW）还是老用户（OLD）。
     */
    @Column(length = 10, nullable = false)
    private String userType;

    @Column(length = 20, nullable = false)
    private String orderId;

    /**
     * 北京时间
     */
    private Date orderTime;

    /**
     * 印度时间
     */
    private Date orderInTime;

    /**
     * 比价订单或者流量劫持订单（SHOP/REDI）
     */
    @Column(length = 10)
    private String orderType;

    @Column(length = 20)
    private String orderStatus;

    private String title;

    private String productId;

    private String category;

    private BigDecimal saleAmount;

    private BigDecimal commissionRate;

    private BigDecimal tentativeAmount;

    private Date logTime = new Date();

    private String deviceId;

    private Date deviceRegTime;

    private String version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAffID() {
        return affID;
    }

    public void setAffID(String affID) {
        this.affID = affID;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelSrc() {
        return channelSrc;
    }

    public void setChannelSrc(String channelSrc) {
        this.channelSrc = channelSrc;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Date getOrderInTime() {
        return orderInTime;
    }

    public void setOrderInTime(Date orderInTime) {
        this.orderInTime = orderInTime;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getTentativeAmount() {
        return tentativeAmount;
    }

    public void setTentativeAmount(BigDecimal tentativeAmount) {
        this.tentativeAmount = tentativeAmount;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getDeviceRegTime() {
        return deviceRegTime;
    }

    public void setDeviceRegTime(Date deviceRegTime) {
        this.deviceRegTime = deviceRegTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAffExtParam2() {
        return affExtParam2;
    }

    public void setAffExtParam2(String affExtParam2) {
        this.affExtParam2 = affExtParam2;
    }

    public String getAffExtParam1() {
        return affExtParam1;
    }

    public void setAffExtParam1(String affExtParam1) {
        this.affExtParam1 = affExtParam1;
    }

    @Override
    public String toString() {
        return "OrderStatsAnalysisPO{" +
                "id=" + id +
                ", affID='" + affID + '\'' +
                ", webSite='" + webSite + '\'' +
                ", channel='" + channel + '\'' +
                ", channelSrc='" + channelSrc + '\'' +
                ", affExtParam1='" + affExtParam1 + '\'' +
                ", affExtParam2='" + affExtParam2 + '\'' +
                ", userId='" + userId + '\'' +
                ", userType='" + userType + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderTime=" + orderTime +
                ", orderInTime=" + orderInTime +
                ", orderType='" + orderType + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", title='" + title + '\'' +
                ", productId='" + productId + '\'' +
                ", category='" + category + '\'' +
                ", saleAmount=" + saleAmount +
                ", commissionRate=" + commissionRate +
                ", tentativeAmount=" + tentativeAmount +
                ", logTime=" + logTime +
                ", deviceId='" + deviceId + '\'' +
                ", deviceRegTime=" + deviceRegTime +
                ", version='" + version + '\'' +
                '}';
    }
}
