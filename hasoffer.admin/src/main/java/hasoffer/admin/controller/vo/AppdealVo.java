package hasoffer.admin.controller.vo;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.enums.AppdealSource;

import java.util.Date;

/**
 * Created on 2016/9/13.
 */
public class AppdealVo {

    private Long id;
    private Website website;//deal来源网站
    private AppdealSource appdealSource;

    private String title;//deal标题
    private String linkUrl;//deal跳转地址
    private int weight = 0;
    private String imageUrl;//本地图片服务器地址
    private Date createTime;//deal创建时间
    private Date expireTime;//deal失效时间

    private boolean push = false;//是否推送到banner,默认false，不推送

    private String description;//deal描述

    private String priceDescription;//deal价格描述

    private boolean display;//是否显示
    private String listPageImage;//deal列表页图片

    private String infoPageImage;//deal详情页图片

    private Long dealClickCount = 0l;//deal的点击次数

    private Long dealCategoryId = -1l;//deal的目录id

    private int discount = 50;//deal的折扣额度

    private long ptmcmpskuid;//如果是PRICE_OFF生成的deal，保留skuid

    private Float originPrice;//deal的原价

    private int expireStatus;//是否有效的状态为

    public AppdealSource getAppdealSource() {
        return appdealSource;
    }

    public void setAppdealSource(AppdealSource appdealSource) {
        this.appdealSource = appdealSource;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getDealCategoryId() {
        return dealCategoryId;
    }

    public void setDealCategoryId(Long dealCategoryId) {
        this.dealCategoryId = dealCategoryId;
    }

    public Long getDealClickCount() {
        return dealClickCount;
    }

    public void setDealClickCount(Long dealClickCount) {
        this.dealClickCount = dealClickCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public int getExpireStatus() {
        return expireStatus;
    }

    public void setExpireStatus(int expireStatus) {
        this.expireStatus = expireStatus;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getInfoPageImage() {
        return infoPageImage;
    }

    public void setInfoPageImage(String infoPageImage) {
        this.infoPageImage = infoPageImage;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getListPageImage() {
        return listPageImage;
    }

    public void setListPageImage(String listPageImage) {
        this.listPageImage = listPageImage;
    }

    public Float getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(Float originPrice) {
        this.originPrice = originPrice;
    }

    public String getPriceDescription() {
        return priceDescription;
    }

    public void setPriceDescription(String priceDescription) {
        this.priceDescription = priceDescription;
    }

    public long getPtmcmpskuid() {
        return ptmcmpskuid;
    }

    public void setPtmcmpskuid(long ptmcmpskuid) {
        this.ptmcmpskuid = ptmcmpskuid;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
