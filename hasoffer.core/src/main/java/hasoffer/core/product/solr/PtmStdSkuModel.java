package hasoffer.core.product.solr;

import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.data.solr.IIdentifiable;

/**
 * Created by hs on 2016年11月28日.
 * Time 18:36
 */
public class PtmStdSkuModel implements IIdentifiable<Long> {
    private Long id;
    private String title;
    private String brand;// 品牌
    private String model;// 型号

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

    private int Ram;//3 GB  筛选类型:1GB-2GB 4GB&More less than 512MB 基本单位是MB GB都*1024
    private String queryRam;
    private String Network;//4G: Available (supports Indian bands) 3G: Available, 2G: Available    只显示2 3 4G的支持即可,判断用contains
    private String Network3G;
    private String Network4G;
    private float Screen_Size;//5.5 inches (13.97 cm) 筛选: 3.5-5 inch less than  &More
    private String queryScreenSize;
    private String Screen_Resolution;//HD (720 x 1280 pixels)  仅显示
    private float Secondary_Camera;//1. 叫Resolution 13 MP Primary Camera  2. 叫Resolution  5 MP Front Camera 筛选:0-1.9MP less than &More
    private String querySecondaryCamera;
    private int Battery_Capacity;//1. 叫Capacity 3150 mAh    筛选:1000-1999mAh less than &More ,单位确定mAh 用整型
    private String queryBatteryCapacity;
    private String Operating_System;//Operating System Android v5.1 (Lollipop) 仅显示
    private float Primary_Camera;//1. 叫Resolution 13 MP Primary Camera  2. 叫Resolution  5 MP Front Camera  筛选:0-1.9MP、2-2.9MP、3-4.9MP、5-7.9MP、8MP&Above float
    private String queryPrimaryCamera;
    private int Internal_Memory;//32 GB 筛选:1GB-2GB less than &More
    private String queryInternalMemory;
    private int Expandable_Memory = 0;//Up to 128 GB : Up to和       0就是 Unavailable

    public PtmStdSkuModel() {

    }

    public PtmStdSkuModel(PtmStdSku ptmStdSku) {
        this.id = ptmStdSku.getId();
        this.title = ptmStdSku.getTitle();
        this.brand = ptmStdSku.getBrand();
        this.model = ptmStdSku.getModel();
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    public int getRam() {
        return Ram;
    }

    public void setRam(int ram) {
        Ram = ram;
    }

    public String getNetwork() {
        return Network;
    }

    public void setNetwork(String network) {
        Network = network;
    }

    public float getScreen_Size() {
        return Screen_Size;
    }

    public void setScreen_Size(float screen_Size) {
        Screen_Size = screen_Size;
    }

    public String getScreen_Resolution() {
        return Screen_Resolution;
    }

    public void setScreen_Resolution(String screen_Resolution) {
        Screen_Resolution = screen_Resolution;
    }

    public float getSecondary_Camera() {
        return Secondary_Camera;
    }

    public void setSecondary_Camera(float secondary_Camera) {
        Secondary_Camera = secondary_Camera;
    }

    public int getBattery_Capacity() {
        return Battery_Capacity;
    }

    public void setBattery_Capacity(int battery_Capacity) {
        Battery_Capacity = battery_Capacity;
    }

    public String getOperating_System() {
        return Operating_System;
    }

    public void setOperating_System(String operating_System) {
        Operating_System = operating_System;
    }

    public float getPrimary_Camera() {
        return Primary_Camera;
    }

    public void setPrimary_Camera(float primary_Camera) {
        Primary_Camera = primary_Camera;
    }

    public int getInternal_Memory() {
        return Internal_Memory;
    }

    public void setInternal_Memory(int internal_Memory) {
        Internal_Memory = internal_Memory;
    }

    public int getExpandable_Memory() {
        return Expandable_Memory;
    }

    public void setExpandable_Memory(int expandable_Memory) {
        Expandable_Memory = expandable_Memory;
    }

    public String getNetwork3G() {
        return Network3G;
    }

    public void setNetwork3G(String network3G) {
        Network3G = network3G;
    }

    public String getNetwork4G() {
        return Network4G;
    }

    public void setNetwork4G(String network4G) {
        Network4G = network4G;
    }

    public String getQueryRam() {
        return queryRam;
    }

    public void setQueryRam(String queryRam) {
        this.queryRam = queryRam;
    }

    public String getQueryScreenSize() {
        return queryScreenSize;
    }

    public void setQueryScreenSize(String queryScreenSize) {
        this.queryScreenSize = queryScreenSize;
    }

    public String getQuerySecondaryCamera() {
        return querySecondaryCamera;
    }

    public void setQuerySecondaryCamera(String querySecondaryCamera) {
        this.querySecondaryCamera = querySecondaryCamera;
    }

    public String getQueryBatteryCapacity() {
        return queryBatteryCapacity;
    }

    public void setQueryBatteryCapacity(String queryBatteryCapacity) {
        this.queryBatteryCapacity = queryBatteryCapacity;
    }

    public String getQueryPrimaryCamera() {
        return queryPrimaryCamera;
    }

    public void setQueryPrimaryCamera(String queryPrimaryCamera) {
        this.queryPrimaryCamera = queryPrimaryCamera;
    }

    public String getQueryInternalMemory() {
        return queryInternalMemory;
    }

    public void setQueryInternalMemory(String queryInternalMemory) {
        this.queryInternalMemory = queryInternalMemory;
    }
}
