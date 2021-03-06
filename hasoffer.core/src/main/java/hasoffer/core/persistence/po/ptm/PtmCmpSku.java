package hasoffer.core.persistence.po.ptm;

import hasoffer.base.enums.IndexNeed;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceCmpSku;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PtmCmpSku implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long productId; // PtmProduct # id

    private Long categoryId;
    @ColumnDefault(value = "0")
    private Long categoryId2 = 0L;//保存flipkart的sku的三级或者末尾级别的类目

    @Enumerated(EnumType.STRING)
    private Website website;
    private String seller;

    private String skuTitle;// 带商品的color，size属性的
    private String title;

    private float price;//sku的现价
    private float oriPrice;//sku的原价

    private float cashBack = -1;

    private String rating;

    private String imagePath; // 下载后的图片路径
    private String smallImagePath;
    private String bigImagePath;
    private String oriImageUrl;// 原图片url

    @Column(columnDefinition = "text")
    private String deeplink;
    @Column(columnDefinition = "text")
    private String url;
    private String urlKey;

    @Column(columnDefinition = "text")
    private String oriUrl; //原始URL 可能带有其他网站的联盟信息

    private String color;
    private String size;

    private Date updateTime = TimeUtils.nowDate();
    private Date createTime;//该条sku记录的创建时间

    private Date titleUpdateTime;

    private boolean checked = false;//人工审核标志位
    private boolean failLoadImage = false; //下载图片是否失败

    private String sourcePid; // 源网站的商品id
    private String sourceSid; // 源网站的商品sku id

    @Enumerated(EnumType.STRING)
    private IndexNeed indexNeed = IndexNeed.NO;
    @Enumerated(EnumType.STRING)
    private SkuStatus status = SkuStatus.ONSALE;

    @ColumnDefault(value = "0")
    private long commentsNumber = 0;//评论数
    @ColumnDefault(value = "0")
    private int ratings = 0;//星级，存放百分比的整数位如 88即表示88%
    @ColumnDefault(value = "-1")
    private float shipping = -1;//邮费，默认值为-1,free shipping时值为0
    private String supportPayMethod;//支付方式  ex：COD,EMI,...,
    private String deliveryTime;//送达时间 ex: 1-3
    @ColumnDefault(value = "0")
    private int returnDays = 0;

    private String brand;//品牌
    private String model;//型号

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
        this.urlKey = HexDigestUtil.md5(url);
    }

    public String getUrlKey() {
        return urlKey;
    }

    /**
     * 该方法只是第一次修复使用的，修复后不建议使用
     * 设置urlKey的方法参见setUrl()方法
     *
     * @param urlKey
     */
    @Deprecated
    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isFailLoadImage() {
        return failLoadImage;
    }

    public void setFailLoadImage(boolean failLoadImage) {
        this.failLoadImage = failLoadImage;
    }

    public long getCommentsNumber() {
        return commentsNumber;
    }

    public void setCommentsNumber(long commentsNumber) {
        this.commentsNumber = commentsNumber;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
    }

    public int getReturnDays() {
        return returnDays;
    }

    public void setReturnDays(int returnDays) {
        this.returnDays = returnDays;
    }

    public float getShipping() {
        return shipping;
    }

    public void setShipping(float shipping) {
        this.shipping = shipping;
    }

    public String getSupportPayMethod() {
        return supportPayMethod;
    }

    public void setSupportPayMethod(String supportPayMethod) {
        this.supportPayMethod = supportPayMethod;
    }

    public float getCashBack() {
        return cashBack;
    }

    public void setCashBack(float cashBack) {
        this.cashBack = cashBack;
    }

    public Long getCategoryId2() {
        return categoryId2;
    }

    public void setCategoryId2(Long categoryId2) {
        this.categoryId2 = categoryId2;
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

    public float getOriPrice() {
        return oriPrice;
    }

    public void setOriPrice(float oriPrice) {
        this.oriPrice = oriPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmCmpSku ptmCmpSku = (PtmCmpSku) o;

        if (productId != ptmCmpSku.productId) return false;
        if (Float.compare(ptmCmpSku.price, price) != 0) return false;
        if (Float.compare(ptmCmpSku.oriPrice, oriPrice) != 0) return false;
        if (Float.compare(ptmCmpSku.cashBack, cashBack) != 0) return false;
        if (checked != ptmCmpSku.checked) return false;
        if (failLoadImage != ptmCmpSku.failLoadImage) return false;
        if (commentsNumber != ptmCmpSku.commentsNumber) return false;
        if (ratings != ptmCmpSku.ratings) return false;
        if (Float.compare(ptmCmpSku.shipping, shipping) != 0) return false;
        if (returnDays != ptmCmpSku.returnDays) return false;
        if (id != null ? !id.equals(ptmCmpSku.id) : ptmCmpSku.id != null) return false;
        if (categoryId != null ? !categoryId.equals(ptmCmpSku.categoryId) : ptmCmpSku.categoryId != null) return false;
        if (categoryId2 != null ? !categoryId2.equals(ptmCmpSku.categoryId2) : ptmCmpSku.categoryId2 != null)
            return false;
        if (website != ptmCmpSku.website) return false;
        if (seller != null ? !seller.equals(ptmCmpSku.seller) : ptmCmpSku.seller != null) return false;
        if (skuTitle != null ? !skuTitle.equals(ptmCmpSku.skuTitle) : ptmCmpSku.skuTitle != null) return false;
        if (title != null ? !title.equals(ptmCmpSku.title) : ptmCmpSku.title != null) return false;
        if (rating != null ? !rating.equals(ptmCmpSku.rating) : ptmCmpSku.rating != null) return false;
        if (imagePath != null ? !imagePath.equals(ptmCmpSku.imagePath) : ptmCmpSku.imagePath != null) return false;
        if (smallImagePath != null ? !smallImagePath.equals(ptmCmpSku.smallImagePath) : ptmCmpSku.smallImagePath != null)
            return false;
        if (bigImagePath != null ? !bigImagePath.equals(ptmCmpSku.bigImagePath) : ptmCmpSku.bigImagePath != null)
            return false;
        if (oriImageUrl != null ? !oriImageUrl.equals(ptmCmpSku.oriImageUrl) : ptmCmpSku.oriImageUrl != null)
            return false;
        if (deeplink != null ? !deeplink.equals(ptmCmpSku.deeplink) : ptmCmpSku.deeplink != null) return false;
        if (url != null ? !url.equals(ptmCmpSku.url) : ptmCmpSku.url != null) return false;
        if (urlKey != null ? !urlKey.equals(ptmCmpSku.urlKey) : ptmCmpSku.urlKey != null) return false;
        if (oriUrl != null ? !oriUrl.equals(ptmCmpSku.oriUrl) : ptmCmpSku.oriUrl != null) return false;
        if (color != null ? !color.equals(ptmCmpSku.color) : ptmCmpSku.color != null) return false;
        if (size != null ? !size.equals(ptmCmpSku.size) : ptmCmpSku.size != null) return false;
        if (updateTime != null ? !updateTime.equals(ptmCmpSku.updateTime) : ptmCmpSku.updateTime != null) return false;
        if (createTime != null ? !createTime.equals(ptmCmpSku.createTime) : ptmCmpSku.createTime != null) return false;
        if (titleUpdateTime != null ? !titleUpdateTime.equals(ptmCmpSku.titleUpdateTime) : ptmCmpSku.titleUpdateTime != null)
            return false;
        if (sourcePid != null ? !sourcePid.equals(ptmCmpSku.sourcePid) : ptmCmpSku.sourcePid != null) return false;
        if (sourceSid != null ? !sourceSid.equals(ptmCmpSku.sourceSid) : ptmCmpSku.sourceSid != null) return false;
        if (indexNeed != ptmCmpSku.indexNeed) return false;
        if (status != ptmCmpSku.status) return false;
        if (supportPayMethod != null ? !supportPayMethod.equals(ptmCmpSku.supportPayMethod) : ptmCmpSku.supportPayMethod != null)
            return false;
        if (deliveryTime != null ? !deliveryTime.equals(ptmCmpSku.deliveryTime) : ptmCmpSku.deliveryTime != null)
            return false;
        if (brand != null ? !brand.equals(ptmCmpSku.brand) : ptmCmpSku.brand != null) return false;
        return !(model != null ? !model.equals(ptmCmpSku.model) : ptmCmpSku.model != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (productId ^ (productId >>> 32));
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        result = 31 * result + (categoryId2 != null ? categoryId2.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (seller != null ? seller.hashCode() : 0);
        result = 31 * result + (skuTitle != null ? skuTitle.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + (oriPrice != +0.0f ? Float.floatToIntBits(oriPrice) : 0);
        result = 31 * result + (cashBack != +0.0f ? Float.floatToIntBits(cashBack) : 0);
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        result = 31 * result + (smallImagePath != null ? smallImagePath.hashCode() : 0);
        result = 31 * result + (bigImagePath != null ? bigImagePath.hashCode() : 0);
        result = 31 * result + (oriImageUrl != null ? oriImageUrl.hashCode() : 0);
        result = 31 * result + (deeplink != null ? deeplink.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (urlKey != null ? urlKey.hashCode() : 0);
        result = 31 * result + (oriUrl != null ? oriUrl.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (titleUpdateTime != null ? titleUpdateTime.hashCode() : 0);
        result = 31 * result + (checked ? 1 : 0);
        result = 31 * result + (failLoadImage ? 1 : 0);
        result = 31 * result + (sourcePid != null ? sourcePid.hashCode() : 0);
        result = 31 * result + (sourceSid != null ? sourceSid.hashCode() : 0);
        result = 31 * result + (indexNeed != null ? indexNeed.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (int) (commentsNumber ^ (commentsNumber >>> 32));
        result = 31 * result + ratings;
        result = 31 * result + (shipping != +0.0f ? Float.floatToIntBits(shipping) : 0);
        result = 31 * result + (supportPayMethod != null ? supportPayMethod.hashCode() : 0);
        result = 31 * result + (deliveryTime != null ? deliveryTime.hashCode() : 0);
        result = 31 * result + returnDays;
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PtmCmpSku{" +
                "id=" + id +
                ", productId=" + productId +
                ", categoryId=" + categoryId +
                ", categoryId2=" + categoryId2 +
                ", website=" + website +
                ", seller='" + seller + '\'' +
                ", skuTitle='" + skuTitle + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", oriPrice=" + oriPrice +
                ", cashBack=" + cashBack +
                ", rating='" + rating + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", smallImagePath='" + smallImagePath + '\'' +
                ", bigImagePath='" + bigImagePath + '\'' +
                ", oriImageUrl='" + oriImageUrl + '\'' +
                ", deeplink='" + deeplink + '\'' +
                ", url='" + url + '\'' +
                ", urlKey='" + urlKey + '\'' +
                ", oriUrl='" + oriUrl + '\'' +
                ", color='" + color + '\'' +
                ", size='" + size + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", titleUpdateTime=" + titleUpdateTime +
                ", checked=" + checked +
                ", failLoadImage=" + failLoadImage +
                ", sourcePid='" + sourcePid + '\'' +
                ", sourceSid='" + sourceSid + '\'' +
                ", indexNeed=" + indexNeed +
                ", status=" + status +
                ", commentsNumber=" + commentsNumber +
                ", ratings=" + ratings +
                ", shipping=" + shipping +
                ", supportPayMethod='" + supportPayMethod + '\'' +
                ", deliveryTime='" + deliveryTime + '\'' +
                ", returnDays=" + returnDays +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
