package hasoffer.core.persistence.po.app;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.enums.AppdealSource;

import javax.persistence.*;
import java.util.Date;
import java.util.Random;

/**
 * Created on 2016/6/17.
 */
@Entity
public class AppDeal implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Deal的权重,初始均为0
    private int weight = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Website website;//deal来源网站

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppdealSource appdealSource = AppdealSource.MANUAL_INPUT;

    private String title;//deal标题
    @Column(columnDefinition = "text")
    private String linkUrl;//deal跳转地址

    @Column(columnDefinition = "text")
    private String imageUrl;//本地图片服务器地址

    @Column(nullable = false)
    private Date createTime;//deal创建时间
    @Column(nullable = false)
    private Date expireTime;//deal失效时间

    private boolean push = false;//是否推送到banner,默认false，不推送

    @Column(columnDefinition = "text")
    private String description;//deal描述

    @Column(length = 1024)
    private String priceDescription;//deal价格描述

    private boolean display = false;//是否显示
    @Column(columnDefinition = "text")
    private String listPageImage;//deal列表页图片

    @Column(columnDefinition = "text")
    private String infoPageImage;//deal详情页图片

    @Column(columnDefinition = "bigint default 0")
    private Long dealClickCount = 0l;//deal的点击次数
    private Long originClickCount;//只针对appdealsource=deal_site的数据，保留抓取时deal的点击次数

    private Long dealCategoryId = -1l;//deal的目录id

    private int discount = 50;//deal的折扣额度

    private long ptmcmpskuid;//如果是PRICE_OFF生成的deal，保留skuid

    private Float originPrice;//deal的原价

    private Float presentPrice;//现价
    private Integer dealThumbNumber = new Random().nextInt(10); //deal 点赞数

    private String oriLinkUrl;//deal站抓取的deal需要保存最原始的link
    private String category;//类目名称

    private Float shippingFee;//运费

    private String couponCode;//优惠码

    //保留空参构造
    public AppDeal() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
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

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getPriceDescription() {
        return priceDescription;
    }

    public void setPriceDescription(String priceDescription) {
        this.priceDescription = priceDescription;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getListPageImage() {
        return listPageImage;
    }

    public void setListPageImage(String listPageImage) {
        this.listPageImage = listPageImage;
    }

    public String getInfoPageImage() {
        return infoPageImage;
    }

    public void setInfoPageImage(String infoPageImage) {
        this.infoPageImage = infoPageImage;
    }

    public Long getDealClickCount() {
        return dealClickCount;
    }

    public void setDealClickCount(Long dealClickCount) {
        this.dealClickCount = dealClickCount;
    }

    public Long getDealCategoryId() {
        return dealCategoryId;
    }

    public void setDealCategoryId(Long dealCategoryId) {
        this.dealCategoryId = dealCategoryId;
    }

    public AppdealSource getAppdealSource() {
        return appdealSource;
    }

    public void setAppdealSource(AppdealSource appdealSource) {
        this.appdealSource = appdealSource;
    }

    public long getPtmcmpskuid() {
        return ptmcmpskuid;
    }

    public void setPtmcmpskuid(long ptmcmpskuid) {
        this.ptmcmpskuid = ptmcmpskuid;
    }

    public Float getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(Float originPrice) {
        this.originPrice = originPrice;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Float getPresentPrice() {
        return presentPrice;
    }

    public void setPresentPrice(Float presentPrice) {
        this.presentPrice = presentPrice;
    }

    public Integer getDealThumbNumber() {
        return dealThumbNumber;
    }

    public void setDealThumbNumber(Integer dealThumbNumber) {
        this.dealThumbNumber = dealThumbNumber;
    }

    public String getOriLinkUrl() {
        return oriLinkUrl;
    }

    public void setOriLinkUrl(String oriLinkUrl) {
        this.oriLinkUrl = oriLinkUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getOriginClickCount() {
        return originClickCount;
    }

    public void setOriginClickCount(Long originClickCount) {
        this.originClickCount = originClickCount;
    }

    public Float getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Float shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppDeal deal = (AppDeal) o;

        if (weight != deal.weight) return false;
        if (push != deal.push) return false;
        if (display != deal.display) return false;
        if (discount != deal.discount) return false;
        if (ptmcmpskuid != deal.ptmcmpskuid) return false;
        if (id != null ? !id.equals(deal.id) : deal.id != null) return false;
        if (website != deal.website) return false;
        if (appdealSource != deal.appdealSource) return false;
        if (title != null ? !title.equals(deal.title) : deal.title != null) return false;
        if (linkUrl != null ? !linkUrl.equals(deal.linkUrl) : deal.linkUrl != null) return false;
        if (imageUrl != null ? !imageUrl.equals(deal.imageUrl) : deal.imageUrl != null) return false;
        if (createTime != null ? !createTime.equals(deal.createTime) : deal.createTime != null) return false;
        if (expireTime != null ? !expireTime.equals(deal.expireTime) : deal.expireTime != null) return false;
        if (description != null ? !description.equals(deal.description) : deal.description != null) return false;
        if (priceDescription != null ? !priceDescription.equals(deal.priceDescription) : deal.priceDescription != null)
            return false;
        if (listPageImage != null ? !listPageImage.equals(deal.listPageImage) : deal.listPageImage != null)
            return false;
        if (infoPageImage != null ? !infoPageImage.equals(deal.infoPageImage) : deal.infoPageImage != null)
            return false;
        if (dealClickCount != null ? !dealClickCount.equals(deal.dealClickCount) : deal.dealClickCount != null)
            return false;
        if (originClickCount != null ? !originClickCount.equals(deal.originClickCount) : deal.originClickCount != null)
            return false;
        if (dealCategoryId != null ? !dealCategoryId.equals(deal.dealCategoryId) : deal.dealCategoryId != null)
            return false;
        if (originPrice != null ? !originPrice.equals(deal.originPrice) : deal.originPrice != null) return false;
        if (presentPrice != null ? !presentPrice.equals(deal.presentPrice) : deal.presentPrice != null) return false;
        if (dealThumbNumber != null ? !dealThumbNumber.equals(deal.dealThumbNumber) : deal.dealThumbNumber != null)
            return false;
        if (oriLinkUrl != null ? !oriLinkUrl.equals(deal.oriLinkUrl) : deal.oriLinkUrl != null) return false;
        if (category != null ? !category.equals(deal.category) : deal.category != null) return false;
        if (shippingFee != null ? !shippingFee.equals(deal.shippingFee) : deal.shippingFee != null) return false;
        return !(couponCode != null ? !couponCode.equals(deal.couponCode) : deal.couponCode != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + weight;
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (appdealSource != null ? appdealSource.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (linkUrl != null ? linkUrl.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (expireTime != null ? expireTime.hashCode() : 0);
        result = 31 * result + (push ? 1 : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (priceDescription != null ? priceDescription.hashCode() : 0);
        result = 31 * result + (display ? 1 : 0);
        result = 31 * result + (listPageImage != null ? listPageImage.hashCode() : 0);
        result = 31 * result + (infoPageImage != null ? infoPageImage.hashCode() : 0);
        result = 31 * result + (dealClickCount != null ? dealClickCount.hashCode() : 0);
        result = 31 * result + (originClickCount != null ? originClickCount.hashCode() : 0);
        result = 31 * result + (dealCategoryId != null ? dealCategoryId.hashCode() : 0);
        result = 31 * result + discount;
        result = 31 * result + (int) (ptmcmpskuid ^ (ptmcmpskuid >>> 32));
        result = 31 * result + (originPrice != null ? originPrice.hashCode() : 0);
        result = 31 * result + (presentPrice != null ? presentPrice.hashCode() : 0);
        result = 31 * result + (dealThumbNumber != null ? dealThumbNumber.hashCode() : 0);
        result = 31 * result + (oriLinkUrl != null ? oriLinkUrl.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (shippingFee != null ? shippingFee.hashCode() : 0);
        result = 31 * result + (couponCode != null ? couponCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AppDeal{" +
                "appdealSource=" + appdealSource +
                ", id=" + id +
                ", weight=" + weight +
                ", website=" + website +
                ", title='" + title + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", createTime=" + createTime +
                ", expireTime=" + expireTime +
                ", push=" + push +
                ", description='" + description + '\'' +
                ", priceDescription='" + priceDescription + '\'' +
                ", display=" + display +
                ", listPageImage='" + listPageImage + '\'' +
                ", infoPageImage='" + infoPageImage + '\'' +
                ", dealClickCount=" + dealClickCount +
                ", originClickCount=" + originClickCount +
                ", dealCategoryId=" + dealCategoryId +
                ", discount=" + discount +
                ", ptmcmpskuid=" + ptmcmpskuid +
                ", originPrice=" + originPrice +
                ", presentPrice=" + presentPrice +
                ", dealThumbNumber=" + dealThumbNumber +
                ", oriLinkUrl='" + oriLinkUrl + '\'' +
                ", category='" + category + '\'' +
                ", shippingFee=" + shippingFee +
                ", couponCode='" + couponCode + '\'' +
                '}';
    }
}
