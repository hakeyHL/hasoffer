package hasoffer.core.persistence.po.admin;

import hasoffer.base.enums.MarketChannel;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/4/11.
 */
@Entity
public class StsAlive implements Identifiable<Long>{

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    @Enumerated(EnumType.STRING)
    private MarketChannel marketChannel;

    private String Campaign;

    private String ADset;

    private Date wakeupTime;

    private Date ratioTime;//比价发生时间

    private String osVersion;

    private String deviceName;

    private String eCommerce;

    private Boolean assistIsActive;

    private Boolean assistIsFirst;

    private Boolean showIcon;

    private Boolean clickIcon;

    private Boolean clickShop;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getRatioTime() {
        return ratioTime;
    }

    public void setRatioTime(Date ratioTime) {
        this.ratioTime = ratioTime;
    }

    public Date getWakeupTime() {
        return wakeupTime;
    }

    public void setWakeupTime(Date wakeupTime) {
        this.wakeupTime = wakeupTime;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String geteCommerce() {
        return eCommerce;
    }

    public void seteCommerce(String eCommerce) {
        this.eCommerce = eCommerce;
    }

    public Boolean getAssistIsActive() {
        return assistIsActive;
    }

    public void setAssistIsActive(Boolean assistIsActive) {
        this.assistIsActive = assistIsActive;
    }

    public Boolean getAssistIsFirst() {
        return assistIsFirst;
    }

    public void setAssistIsFirst(Boolean assistIsFirst) {
        this.assistIsFirst = assistIsFirst;
    }

    public Boolean getShowIcon() {
        return showIcon;
    }

    public void setShowIcon(Boolean showIcon) {
        this.showIcon = showIcon;
    }

    public Boolean getClickIcon() {
        return clickIcon;
    }

    public void setClickIcon(Boolean clickIcon) {
        this.clickIcon = clickIcon;
    }

    public Boolean getClickShop() {
        return clickShop;
    }

    public void setClickShop(Boolean clickShop) {
        this.clickShop = clickShop;
    }

    public MarketChannel getMarketChannel() {
        return marketChannel;
    }

    public void setMarketChannel(MarketChannel marketChannel) {
        this.marketChannel = marketChannel;
    }

    public String getCampaign() {
        return Campaign;
    }

    public void setCampaign(String campaign) {
        Campaign = campaign;
    }

    public String getADset() {
        return ADset;
    }

    public void setADset(String ADset) {
        this.ADset = ADset;
    }

}
