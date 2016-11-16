package hasoffer.core.bo.stdsku;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;

import java.util.Date;

/**
 * Created by chevy on 2016/11/16.
 */
public class StdSkuPrice {

    private Long id;

    private long stdSkuId; // ptm_std_sku # id

    private String title;// 带商品的color，size属性的

    private float price; // 价格
    private long stockCount; // 库存
    private float shippingFee; //运费

    private SkuStatus skuStatus;

    private Website website;
    private String url;

    private Date updateTime = TimeUtils.nowDate();
    private Date createTime = TimeUtils.nowDate();//该条sku记录的创建时间

    public StdSkuPrice(Long id, long stdSkuId, String title, float price, long stockCount, float shippingFee,
                       SkuStatus skuStatus, Website website, String url, Date updateTime, Date createTime) {
        this.id = id;
        this.stdSkuId = stdSkuId;
        this.title = title;
        this.price = price;
        this.stockCount = stockCount;
        this.shippingFee = shippingFee;
        this.skuStatus = skuStatus;
        this.website = website;
        this.url = url;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }

    public StdSkuPrice(PtmStdPrice stdPrice) {
        this(stdPrice.getId(), stdPrice.getStdSkuId(), stdPrice.getTitle(), stdPrice.getPrice(), stdPrice.getStockCount(), stdPrice.getShippingFee(),
                stdPrice.getSkuStatus(), stdPrice.getWebsite(), stdPrice.getUrl(), stdPrice.getUpdateTime(), stdPrice.getCreateTime());
    }

    public Long getId() {
        return id;
    }

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

    public SkuStatus getSkuStatus() {
        return skuStatus;
    }

    public void setSkuStatus(SkuStatus skuStatus) {
        this.skuStatus = skuStatus;
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
}
