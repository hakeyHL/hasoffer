package hasoffer.core.app.vo;

/**
 * Created by hs on 2016年12月08日.
 * Time 19:34
 */
public class CategoryFilterReqVo {
    //String
    private String[] brand;
    private String[] netWork;
    private String[] screenResolution;
    private String[] opreatingSystem;
    private String[] expandableMemory;

    //int filter
    private String[] ram;
    private String[] batteryCapacity;
    private String[] internalMemory;

    //float
    private String[] primaryCamera;
    private String[] secondaryCamera;
    private String[] screenSize;

    public String[] getBrand() {
        return brand;
    }

    public void setBrand(String[] brand) {
        this.brand = brand;
    }

    public String[] getNetWork() {
        return netWork;
    }

    public void setNetWork(String[] netWork) {
        this.netWork = netWork;
    }

    public String[] getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String[] screenResolution) {
        this.screenResolution = screenResolution;
    }

    public String[] getOpreatingSystem() {
        return opreatingSystem;
    }

    public void setOpreatingSystem(String[] opreatingSystem) {
        this.opreatingSystem = opreatingSystem;
    }

    public String[] getExpandableMemory() {
        return expandableMemory;
    }

    public void setExpandableMemory(String[] expandableMemory) {
        this.expandableMemory = expandableMemory;
    }

    public String[] getRam() {
        return ram;
    }

    public void setRam(String[] ram) {
        this.ram = ram;
    }

    public String[] getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(String[] batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public String[] getInternalMemory() {
        return internalMemory;
    }

    public void setInternalMemory(String[] internalMemory) {
        this.internalMemory = internalMemory;
    }

    public String[] getPrimaryCamera() {
        return primaryCamera;
    }

    public void setPrimaryCamera(String[] primaryCamera) {
        this.primaryCamera = primaryCamera;
    }

    public String[] getSecondaryCamera() {
        return secondaryCamera;
    }

    public void setSecondaryCamera(String[] secondaryCamera) {
        this.secondaryCamera = secondaryCamera;
    }

    public String[] getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String[] screenSize) {
        this.screenSize = screenSize;
    }
}
