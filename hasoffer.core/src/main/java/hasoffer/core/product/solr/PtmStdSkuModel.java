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

   /* private int Ram;
    private String Network;
    private float Screen_Size;
    private String Screen_Resolution;
    private float Secondary_Camera;
    private int  Battery_Capacity;
    private String Operating_System;
    private float Primary_Camera;
    private  int Internal_Memory;
    private String Expandable_Memory="Unavailable";*/

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

}
