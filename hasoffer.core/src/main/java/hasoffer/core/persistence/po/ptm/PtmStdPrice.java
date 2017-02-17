package hasoffer.core.persistence.po.ptm;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PtmStdPrice implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long stdSkuId; // ptm_std_sku # id

    private String title;// 带商品的color，size属性的

    private float price; // 价格
    private float oriPrice;//sku的原价

    private long stockCount; // 库存
    private float shippingFee; //运费

    @Enumerated(EnumType.STRING)
    private SkuStatus skuStatus;

    @Enumerated(EnumType.STRING)
    private Website website;
    private String url;
    private String urlKey;

    @ColumnDefault(value = "0")
    private long commentsNumber = 0;//评论数
    @ColumnDefault(value = "0")
    private int ratings = 0;//星级，存放百分比的整数位如 88即表示88%

    private Date updateTime = TimeUtils.nowDate();
    private Date createTime = TimeUtils.nowDate();//该条sku记录的创建时间

    public PtmStdPrice() {

    }

    public PtmStdPrice(long stdSkuId, String title, float price, long stockCount,
                       float shippingFee, SkuStatus skuStatus, Website website, String url) {
        this.stdSkuId = stdSkuId;
        this.title = title;
        this.price = price;
        this.stockCount = stockCount;
        this.shippingFee = shippingFee;
        this.skuStatus = skuStatus;
        this.website = website;
        this.url = url;
        this.urlKey = HexDigestUtil.md5(url);
    }

    public PtmStdPrice(long stdSkuId, float price, SkuStatus skuStatus, String url) {
        this.stdSkuId = stdSkuId;
        this.price = price;
        this.skuStatus = skuStatus;
        this.url = url;
        this.urlKey = HexDigestUtil.md5(url);
        Date currentTime = new Date();
        this.createTime = currentTime;
        this.updateTime = currentTime;

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getStdSkuId() {
        return stdSkuId;
    }

    public void setStdSkuId(long stdSkuId) {
        this.stdSkuId = stdSkuId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public SkuStatus getSkuStatus() {
        return skuStatus;
    }

    public void setSkuStatus(SkuStatus skuStatus) {
        this.skuStatus = skuStatus;
    }

    public long getStockCount() {
        return stockCount;
    }

    public void setStockCount(long stockCount) {
        this.stockCount = stockCount;
    }

    public float getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(float shippingFee) {
        this.shippingFee = shippingFee;
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

    public long getCommentsNumber() {
        return commentsNumber;
    }

    public void setCommentsNumber(long commentsNumber) {
        this.commentsNumber = commentsNumber;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
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

        PtmStdPrice that = (PtmStdPrice) o;

        if (stdSkuId != that.stdSkuId) return false;
        if (Float.compare(that.price, price) != 0) return false;
        if (Float.compare(that.oriPrice, oriPrice) != 0) return false;
        if (stockCount != that.stockCount) return false;
        if (Float.compare(that.shippingFee, shippingFee) != 0) return false;
        if (commentsNumber != that.commentsNumber) return false;
        if (ratings != that.ratings) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (skuStatus != that.skuStatus) return false;
        if (website != that.website) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (urlKey != null ? !urlKey.equals(that.urlKey) : that.urlKey != null) return false;
        if (updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null) return false;
        return !(createTime != null ? !createTime.equals(that.createTime) : that.createTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (stdSkuId ^ (stdSkuId >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + (oriPrice != +0.0f ? Float.floatToIntBits(oriPrice) : 0);
        result = 31 * result + (int) (stockCount ^ (stockCount >>> 32));
        result = 31 * result + (shippingFee != +0.0f ? Float.floatToIntBits(shippingFee) : 0);
        result = 31 * result + (skuStatus != null ? skuStatus.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (urlKey != null ? urlKey.hashCode() : 0);
        result = 31 * result + (int) (commentsNumber ^ (commentsNumber >>> 32));
        result = 31 * result + ratings;
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PtmStdPrice{" +
                "commentsNumber=" + commentsNumber +
                ", id=" + id +
                ", stdSkuId=" + stdSkuId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", oriPrice=" + oriPrice +
                ", stockCount=" + stockCount +
                ", shippingFee=" + shippingFee +
                ", skuStatus=" + skuStatus +
                ", website=" + website +
                ", url='" + url + '\'' +
                ", urlKey='" + urlKey + '\'' +
                ", ratings=" + ratings +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
