package hasoffer.core.persistence.po.ptm;

import hasoffer.base.enums.IndexNeed;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceCmpSku;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PtmCmpSku implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long productId; // PtmProduct # id

    @Enumerated(EnumType.STRING)
    private Website website;
    private String seller;

    private String skuTitle;// 带商品的color，size属性的
    private String title;
    private float price;

    private String rating;

    private String imagePath; // 下载后的图片路径
    private String smallImagePath;
    private String bigImagePath;
    private String oriImageUrl;// 原图片url

    @Column(columnDefinition = "text")
    private String deeplink;
    @Column(columnDefinition = "text")
    private String url;
    @Column(columnDefinition = "text")
    private String oriUrl; //原始URL 可能带有其他网站的联盟信息

    private String color;
    private String size;

    private Date updateTime = TimeUtils.nowDate();
    private Date createTime;//该条sku记录的创建时间

    private Date titleUpdateTime;

    private boolean checked = false;//人工审核标志位

    private String sourcePid; // 源网站的商品id
    private String sourceSid; // 源网站的商品sku id

    @Enumerated(EnumType.STRING)
    private IndexNeed indexNeed = IndexNeed.NO;
    @Enumerated(EnumType.STRING)
    private SkuStatus status = SkuStatus.ONSALE;

    public PtmCmpSku() {
    }

    public PtmCmpSku(long productId, float price, String url) {
        this.productId = productId;
        this.price = price;
        this.oriUrl = url;
        this.url = url;
        this.website = WebsiteHelper.getWebSite(url);

        this.sourcePid = WebsiteHelper.getProductIdFromUrl(website, url);
        this.sourceSid = WebsiteHelper.getSkuIdFromUrl(website, url);
    }

    public PtmCmpSku(long productId, float price, String url, String title, String imageUrl) {
        this(productId, price, url);
        this.title = title;
        this.oriImageUrl = imageUrl;
        this.skuTitle = title;
    }

    public PtmCmpSku(long productId, float price, String url, String title, String imageUrl, String deeplink) {
        this(productId, price, url, title, imageUrl);
        this.deeplink = deeplink;
    }

    public PtmCmpSku(long productId, MySmartPriceCmpSku cmpSku) {
        this.productId = productId;
        this.rating = cmpSku.getRating();
        this.price = cmpSku.getPrice();
        this.oriUrl = cmpSku.getUrl();
        this.url = WebsiteHelper.getRealUrl(oriUrl);
        this.color = cmpSku.getColor();
        this.size = cmpSku.getSize();
        this.seller = cmpSku.getSeller();
        this.website = WebsiteHelper.getWebSite(cmpSku.getUrl());
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getOriUrl() {
        return oriUrl;
    }

    public void setOriUrl(String oriUrl) {
        this.oriUrl = oriUrl;
    }

    public SkuStatus getStatus() {
        return status;
    }

    public void setStatus(SkuStatus status) {
        this.status = status;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOriImageUrl() {
        return oriImageUrl;
    }

    public void setOriImageUrl(String oriImageUrl) {
        this.oriImageUrl = oriImageUrl;
    }

    public String getSkuTitle() {
        return skuTitle;
    }

    public void setSkuTitle(String skuTitle) {
        this.skuTitle = skuTitle;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getSourcePid() {
        return sourcePid;
    }

    public void setSourcePid(String sourcePid) {
        this.sourcePid = sourcePid;
    }

    public String getSourceSid() {
        return sourceSid;
    }

    public void setSourceSid(String sourceSid) {
        this.sourceSid = sourceSid;
    }

    public Date getTitleUpdateTime() {
        return titleUpdateTime;
    }

    public void setTitleUpdateTime(Date titleUpdateTime) {
        this.titleUpdateTime = titleUpdateTime;
    }

    public IndexNeed getIndexNeed() {
        return indexNeed;
    }

    public void setIndexNeed(IndexNeed indexNeed) {
        this.indexNeed = indexNeed;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSmallImagePath() {
        return smallImagePath;
    }

    public void setSmallImagePath(String smallImagePath) {
        this.smallImagePath = smallImagePath;
    }

    public String getBigImagePath() {
        return bigImagePath;
    }

    public void setBigImagePath(String bigImagePath) {
        this.bigImagePath = bigImagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmCmpSku cmpSku = (PtmCmpSku) o;

        if (productId != cmpSku.productId) return false;
        if (Float.compare(cmpSku.price, price) != 0) return false;
        if (checked != cmpSku.checked) return false;
        if (id != null ? !id.equals(cmpSku.id) : cmpSku.id != null) return false;
        if (website != cmpSku.website) return false;
        if (seller != null ? !seller.equals(cmpSku.seller) : cmpSku.seller != null) return false;
        if (skuTitle != null ? !skuTitle.equals(cmpSku.skuTitle) : cmpSku.skuTitle != null) return false;
        if (title != null ? !title.equals(cmpSku.title) : cmpSku.title != null) return false;
        if (rating != null ? !rating.equals(cmpSku.rating) : cmpSku.rating != null) return false;
        if (imagePath != null ? !imagePath.equals(cmpSku.imagePath) : cmpSku.imagePath != null) return false;
        if (smallImagePath != null ? !smallImagePath.equals(cmpSku.smallImagePath) : cmpSku.smallImagePath != null)
            return false;
        if (bigImagePath != null ? !bigImagePath.equals(cmpSku.bigImagePath) : cmpSku.bigImagePath != null)
            return false;
        if (oriImageUrl != null ? !oriImageUrl.equals(cmpSku.oriImageUrl) : cmpSku.oriImageUrl != null) return false;
        if (deeplink != null ? !deeplink.equals(cmpSku.deeplink) : cmpSku.deeplink != null) return false;
        if (url != null ? !url.equals(cmpSku.url) : cmpSku.url != null) return false;
        if (oriUrl != null ? !oriUrl.equals(cmpSku.oriUrl) : cmpSku.oriUrl != null) return false;
        if (color != null ? !color.equals(cmpSku.color) : cmpSku.color != null) return false;
        if (size != null ? !size.equals(cmpSku.size) : cmpSku.size != null) return false;
        if (updateTime != null ? !updateTime.equals(cmpSku.updateTime) : cmpSku.updateTime != null) return false;
        if (createTime != null ? !createTime.equals(cmpSku.createTime) : cmpSku.createTime != null) return false;
        if (titleUpdateTime != null ? !titleUpdateTime.equals(cmpSku.titleUpdateTime) : cmpSku.titleUpdateTime != null)
            return false;
        if (sourcePid != null ? !sourcePid.equals(cmpSku.sourcePid) : cmpSku.sourcePid != null) return false;
        if (sourceSid != null ? !sourceSid.equals(cmpSku.sourceSid) : cmpSku.sourceSid != null) return false;
        if (indexNeed != cmpSku.indexNeed) return false;
        return status == cmpSku.status;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (productId ^ (productId >>> 32));
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (seller != null ? seller.hashCode() : 0);
        result = 31 * result + (skuTitle != null ? skuTitle.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        result = 31 * result + (smallImagePath != null ? smallImagePath.hashCode() : 0);
        result = 31 * result + (bigImagePath != null ? bigImagePath.hashCode() : 0);
        result = 31 * result + (oriImageUrl != null ? oriImageUrl.hashCode() : 0);
        result = 31 * result + (deeplink != null ? deeplink.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (oriUrl != null ? oriUrl.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (titleUpdateTime != null ? titleUpdateTime.hashCode() : 0);
        result = 31 * result + (checked ? 1 : 0);
        result = 31 * result + (sourcePid != null ? sourcePid.hashCode() : 0);
        result = 31 * result + (sourceSid != null ? sourceSid.hashCode() : 0);
        result = 31 * result + (indexNeed != null ? indexNeed.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
