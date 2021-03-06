package hasoffer.core.app.vo;

import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;

/**
 * Date : 2016/1/15
 * Function :
 */
public class DeviceInfoVo {

    private String brand;
    private String userToken;
    private String imeiId;
    private String deviceId;
    private String serial;

    private String mac; // mac地址

    private String deviceName;

    private String osVersion;

    private Integer osVersionCode;

    private String appVersion;
    private String screen;
    private String screenSize;
    private String ramSize;


    private Website curShopApp;
    private String[] shopApp;
    private String[] otherApp;
    private int appCount; // app安装数量

    private String curNetState;

    private AppType appType;
    private MarketChannel marketChannel;

    private String gcmToken;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int appCount) {
        this.appCount = appCount;
    }

    public String getCurNetState() {
        return curNetState;
    }

    public void setCurNetState(String curNetState) {
        this.curNetState = curNetState;
    }

    public String[] getOtherApp() {
        return otherApp;
    }

    public void setOtherApp(String[] otherApp) {
        this.otherApp = otherApp;
    }

    public Website getCurShopApp() {
        return curShopApp;
    }

    public void setCurShopApp(Website curShopApp) {
        this.curShopApp = curShopApp;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImeiId() {
        return imeiId;
    }

    public void setImeiId(String imeiId) {
        this.imeiId = imeiId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public Integer getOsVersionCode() {
        return osVersionCode;
    }

    public void setOsVersionCode(Integer osVersionCode) {
        this.osVersionCode = osVersionCode;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String[] getShopApp() {
        return shopApp;
    }

    public void setShopApp(String[] shopApp) {
        this.shopApp = shopApp;
    }

    public AppType getAppType() {
        return appType;
    }

    public void setAppType(AppType appType) {
        this.appType = appType;
    }

    public MarketChannel getMarketChannel() {
        return marketChannel;
    }

    public void setMarketChannel(MarketChannel marketChannel) {
        this.marketChannel = marketChannel;
    }
}
