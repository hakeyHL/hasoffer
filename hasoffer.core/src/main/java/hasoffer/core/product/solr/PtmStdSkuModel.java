package hasoffer.core.product.solr;

import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.data.solr.IIdentifiable;

import java.util.Date;

/**
 * Created by hs on 2016年11月28日.
 * Time 18:36
 */
public class PtmStdSkuModel implements IIdentifiable<Long> {
    private Long id;
    private String title;

    private long cate1;
    private long cate2;
    private long cate3;

    private String cate1Name;
    private String cate2Name;
    private String cate3Name;

    private float minPrice;
    private float maxPrice;

    private int rating = 0;// 评分
    private int review = 0;// 评论
    private int storeCount = 0;// site 数量

    private long searchCount = 0; // 搜索次数，表示商品热度


    private Date Launch_Date;//发布日期
    private String Brand;//品牌
    private String Model;//型号
    private String Operating_System;//Operating System Android v5.1 (Lollipop) 仅显示


    private String SIM_Slot;
    private String SIM_Size;
    private String Network_Support;//4G: Available (supports Indian bands) 3G: Available, 2G: Available    只显示2 3 4G的支持即可,判断用contains
    private String Fingerprint_Sensor;

    private int Weight;


    private String Screen_Resolution;//HD (720 x 1280 pixels)  仅显示


    private float Screen_Size;//5.5 inches (13.97 cm) 筛选: 3.5-5 inch less than  &More
    private String queryScreenSize;
    private int Pixel_Density;
    private String Touch_Screen;
    private float Screen_to_Body_Ratio;


    private String Processor;
    private String Graphics;
    private int RAM;//3 GB  筛选类型:1GB-2GB 4GB&More less than 512MB 基本单位是MB GB都*1024
    private String queryRam;


    private int Internal_Memory;//32 GB 筛选:1GB-2GB less than &More
    private String queryInternalMemory;
    private int Expandable_Memory = 0;//Up to 128 GB : Up to和       0就是 Unavailable

    private float Secondary_Camera;//1. 叫Resolution 13 MP Primary Camera  2. 叫Resolution  5 MP Front Camera 筛选:0-1.9MP less than &More
    private String querySecondaryCamera;
    private float Primary_Camera;//1. 叫Resolution 13 MP Primary Camera  2. 叫Resolution  5 MP Front Camera  筛选:0-1.9MP、2-2.9MP、3-4.9MP、5-7.9MP、8MP&Above float
    private String queryPrimaryCamera;
    private String Sensor;
    private String Autofocus;
    private String secondaryAutofocus;
    private String Aperture;
    private String Flash;
    private String SecondaryFlash;
    private String Image_Resolution;
    private String Camera_Features;
    private String Video_Recording;


    private int Battery_Capacity;//1. 叫Capacity 3150 mAh    筛选:1000-1999mAh less than &More ,单位确定mAh 用整型
    private String queryBatteryCapacity;

    private String Type;
    private String User_Replaceable;
    private String Quick_Charging;


    private String VoLTE;
    private String WiFi;
    private String Bluetooth;
    private String GPS;
    private String NFC;


    private String FM_Radio;
    private String Loudspeaker;
    private String Audio_Jack;


    private String Other_Sensors;


    public PtmStdSkuModel() {

    }

    public PtmStdSkuModel(PtmStdSku ptmStdSku) {
        this.id = ptmStdSku.getId();
        this.title = ptmStdSku.getTitle();
        this.Brand = ptmStdSku.getBrand();
        this.Model = ptmStdSku.getModel();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCate1() {
        return cate1;
    }

    public void setCate1(long cate1) {
        this.cate1 = cate1;
    }

    public long getCate2() {
        return cate2;
    }

    public void setCate2(long cate2) {
        this.cate2 = cate2;
    }

    public long getCate3() {
        return cate3;
    }

    public void setCate3(long cate3) {
        this.cate3 = cate3;
    }

    public String getCate1Name() {
        return cate1Name;
    }

    public void setCate1Name(String cate1Name) {
        this.cate1Name = cate1Name;
    }

    public String getCate2Name() {
        return cate2Name;
    }

    public void setCate2Name(String cate2Name) {
        this.cate2Name = cate2Name;
    }

    public String getCate3Name() {
        return cate3Name;
    }

    public void setCate3Name(String cate3Name) {
        this.cate3Name = cate3Name;
    }

    public float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }

    public float getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(float maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getReview() {
        return review;
    }

    public void setReview(int review) {
        this.review = review;
    }

    public int getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(int storeCount) {
        this.storeCount = storeCount;
    }

    public long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(long searchCount) {
        this.searchCount = searchCount;
    }

    public Date getLaunch_Date() {
        return Launch_Date;
    }

    public void setLaunch_Date(Date launch_Date) {
        Launch_Date = launch_Date;
    }

    public String getBrand() {
        return Brand;
    }

    public void setBrand(String brand) {
        Brand = brand;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getOperating_System() {
        return Operating_System;
    }

    public void setOperating_System(String operating_System) {
        Operating_System = operating_System;
    }

    public String getSIM_Slot() {
        return SIM_Slot;
    }

    public void setSIM_Slot(String SIM_Slot) {
        this.SIM_Slot = SIM_Slot;
    }

    public String getSIM_Size() {
        return SIM_Size;
    }

    public void setSIM_Size(String SIM_Size) {
        this.SIM_Size = SIM_Size;
    }

    public String getNetwork_Support() {
        return Network_Support;
    }

    public void setNetwork_Support(String network_Support) {
        Network_Support = network_Support;
    }

    public String getFingerprint_Sensor() {
        return Fingerprint_Sensor;
    }

    public void setFingerprint_Sensor(String fingerprint_Sensor) {
        Fingerprint_Sensor = fingerprint_Sensor;
    }

    public int getWeight() {
        return Weight;
    }

    public void setWeight(int weight) {
        Weight = weight;
    }

    public String getScreen_Resolution() {
        return Screen_Resolution;
    }

    public void setScreen_Resolution(String screen_Resolution) {
        Screen_Resolution = screen_Resolution;
    }

    public float getScreen_Size() {
        return Screen_Size;
    }

    public void setScreen_Size(float screen_Size) {
        Screen_Size = screen_Size;
    }

    public String getQueryScreenSize() {
        return queryScreenSize;
    }

    public void setQueryScreenSize(String queryScreenSize) {
        this.queryScreenSize = queryScreenSize;
    }

    public int getPixel_Density() {
        return Pixel_Density;
    }

    public void setPixel_Density(int pixel_Density) {
        Pixel_Density = pixel_Density;
    }

    public String getTouch_Screen() {
        return Touch_Screen;
    }

    public void setTouch_Screen(String touch_Screen) {
        Touch_Screen = touch_Screen;
    }

    public float getScreen_to_Body_Ratio() {
        return Screen_to_Body_Ratio;
    }

    public void setScreen_to_Body_Ratio(float screen_to_Body_Ratio) {
        Screen_to_Body_Ratio = screen_to_Body_Ratio;
    }

    public String getProcessor() {
        return Processor;
    }

    public void setProcessor(String processor) {
        Processor = processor;
    }

    public String getGraphics() {
        return Graphics;
    }

    public void setGraphics(String graphics) {
        Graphics = graphics;
    }

    public int getRAM() {
        return RAM;
    }

    public void setRAM(int RAM) {
        this.RAM = RAM;
    }

    public String getQueryRam() {
        return queryRam;
    }

    public void setQueryRam(String queryRam) {
        this.queryRam = queryRam;
    }

    public int getInternal_Memory() {
        return Internal_Memory;
    }

    public void setInternal_Memory(int internal_Memory) {
        Internal_Memory = internal_Memory;
    }

    public String getQueryInternalMemory() {
        return queryInternalMemory;
    }

    public void setQueryInternalMemory(String queryInternalMemory) {
        this.queryInternalMemory = queryInternalMemory;
    }

    public int getExpandable_Memory() {
        return Expandable_Memory;
    }

    public void setExpandable_Memory(int expandable_Memory) {
        Expandable_Memory = expandable_Memory;
    }

    public float getSecondary_Camera() {
        return Secondary_Camera;
    }

    public void setSecondary_Camera(float secondary_Camera) {
        Secondary_Camera = secondary_Camera;
    }

    public String getQuerySecondaryCamera() {
        return querySecondaryCamera;
    }

    public void setQuerySecondaryCamera(String querySecondaryCamera) {
        this.querySecondaryCamera = querySecondaryCamera;
    }

    public float getPrimary_Camera() {
        return Primary_Camera;
    }

    public void setPrimary_Camera(float primary_Camera) {
        Primary_Camera = primary_Camera;
    }

    public String getQueryPrimaryCamera() {
        return queryPrimaryCamera;
    }

    public void setQueryPrimaryCamera(String queryPrimaryCamera) {
        this.queryPrimaryCamera = queryPrimaryCamera;
    }

    public String getSensor() {
        return Sensor;
    }

    public void setSensor(String sensor) {
        Sensor = sensor;
    }

    public String getAutofocus() {
        return Autofocus;
    }

    public void setAutofocus(String autofocus) {
        Autofocus = autofocus;
    }

    public String getSecondaryAutofocus() {
        return secondaryAutofocus;
    }

    public void setSecondaryAutofocus(String secondaryAutofocus) {
        this.secondaryAutofocus = secondaryAutofocus;
    }

    public String getFlash() {
        return Flash;
    }

    public void setFlash(String flash) {
        Flash = flash;
    }

    public String getSecondaryFlash() {
        return SecondaryFlash;
    }

    public void setSecondaryFlash(String secondaryFlash) {
        SecondaryFlash = secondaryFlash;
    }

    public String getWiFi() {
        return WiFi;
    }

    public void setWiFi(String wiFi) {
        WiFi = wiFi;
    }

    public String getBluetooth() {
        return Bluetooth;
    }

    public void setBluetooth(String bluetooth) {
        Bluetooth = bluetooth;
    }

    public String getGPS() {
        return GPS;
    }

    public void setGPS(String GPS) {
        this.GPS = GPS;
    }

    public String getNFC() {
        return NFC;
    }

    public void setNFC(String NFC) {
        this.NFC = NFC;
    }

    public String getFM_Radio() {
        return FM_Radio;
    }

    public void setFM_Radio(String FM_Radio) {
        this.FM_Radio = FM_Radio;
    }

    public String getLoudspeaker() {
        return Loudspeaker;
    }

    public void setLoudspeaker(String loudspeaker) {
        Loudspeaker = loudspeaker;
    }

    public String getAperture() {
        return Aperture;
    }

    public void setAperture(String aperture) {
        Aperture = aperture;
    }


    public String getImage_Resolution() {
        return Image_Resolution;
    }

    public void setImage_Resolution(String image_Resolution) {
        Image_Resolution = image_Resolution;
    }

    public String getCamera_Features() {
        return Camera_Features;
    }

    public void setCamera_Features(String camera_Features) {
        Camera_Features = camera_Features;
    }

    public String getVideo_Recording() {
        return Video_Recording;
    }

    public void setVideo_Recording(String video_Recording) {
        Video_Recording = video_Recording;
    }

    public int getBattery_Capacity() {
        return Battery_Capacity;
    }

    public void setBattery_Capacity(int battery_Capacity) {
        Battery_Capacity = battery_Capacity;
    }

    public String getQueryBatteryCapacity() {
        return queryBatteryCapacity;
    }

    public void setQueryBatteryCapacity(String queryBatteryCapacity) {
        this.queryBatteryCapacity = queryBatteryCapacity;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getUser_Replaceable() {
        return User_Replaceable;
    }

    public void setUser_Replaceable(String user_Replaceable) {
        User_Replaceable = user_Replaceable;
    }

    public String getQuick_Charging() {
        return Quick_Charging;
    }

    public void setQuick_Charging(String quick_Charging) {
        Quick_Charging = quick_Charging;
    }

    public String getVoLTE() {
        return VoLTE;
    }

    public void setVoLTE(String voLTE) {
        VoLTE = voLTE;
    }


    public String getAudio_Jack() {
        return Audio_Jack;
    }

    public void setAudio_Jack(String audio_Jack) {
        Audio_Jack = audio_Jack;
    }

    public String getOther_Sensors() {
        return Other_Sensors;
    }

    public void setOther_Sensors(String other_Sensors) {
        Other_Sensors = other_Sensors;
    }
}
