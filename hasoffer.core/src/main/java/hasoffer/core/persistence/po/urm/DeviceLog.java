package hasoffer.core.persistence.po.urm;

public class DeviceLog {

    private String androidId;
    private String mac;
    private String imei;
    private String serial;
    private String deviceName;
    private String osVersion;
    private String appType;
    private String appVersion;
    private String gcmToken;
    private String brand;
    private String screen;
    private String screenSize;
    private String ramSize;
    private String channel;
    private Long regTime;
    private Long reqDate;
    private String reqTimes;

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getRamSize() {
        return ramSize;
    }

    public void setRamSize(String ramSize) {
        this.ramSize = ramSize;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getRegTime() {
        return regTime;
    }

    public void setRegTime(Long regTime) {
        this.regTime = regTime;
    }

    public Long getReqDate() {
        return reqDate;
    }

    public void setReqDate(Long reqDate) {
        this.reqDate = reqDate;
    }

    public String getReqTimes() {
        return reqTimes;
    }

    public void setReqTimes(String reqTimes) {
        this.reqTimes = reqTimes;
    }

    @Override
    public String toString() {
        return "DeviceLog{" +
                "androidId='" + androidId + '\'' +
                ", mac='" + mac + '\'' +
                ", imei='" + imei + '\'' +
                ", serial='" + serial + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", appType='" + appType + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", gcmToken='" + gcmToken + '\'' +
                ", brand='" + brand + '\'' +
                ", screen='" + screen + '\'' +
                ", screenSize='" + screenSize + '\'' +
                ", ramSize='" + ramSize + '\'' +
                ", channel='" + channel + '\'' +
                ", regTime=" + regTime +
                ", reqDate=" + reqDate +
                ", reqTimes='" + reqTimes + '\'' +
                '}';
    }
}