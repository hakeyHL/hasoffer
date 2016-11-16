package hasoffer.core.bo.stdsku;

import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.persistence.po.ptm.PtmStdSkuDetail;
import hasoffer.core.persistence.po.ptm.PtmStdSkuParamGroup;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chevy on 2016/11/16.
 */
public class StdSkuBo {

    private long id;

    private long stdProId; // PtmStdProduct # id

    private String title;// (可能)带商品的color，size属性的
    private String brand; // 品牌
    private String model; // 型号 (品牌+型号不允许有重复)

    private long categoryId;
    private float refPrice; // 参考价格

    private Date createTime;//该条sku记录的创建时间

    private String sourceId; // sourceId
    private String sourceUrl; // source url

    private Map<String, StdSkuAttr> skuAttrs;

    private List<StdSkuPrice> skuPrices;

    private List<StdSkuImage> skuImages;

    private List<PtmStdSkuParamGroup> paramGroups;

    private String desc;

    public StdSkuBo(PtmStdSku stdSku, Map<String, StdSkuAttr> skuAttrs, List<StdSkuPrice> skuPrices, List<StdSkuImage> skuImages, PtmStdSkuDetail skuDetail) {
        this.id = stdSku.getId();
        this.stdProId = stdSku.getStdProId();
        this.title = stdSku.getTitle();
        this.brand = stdSku.getBrand();
        this.model = stdSku.getModel();
        this.categoryId = stdSku.getCategoryId();
        this.refPrice = stdSku.getRefPrice();
        this.createTime = stdSku.getCreateTime();
        this.sourceId = stdSku.getSourceId();
        this.sourceUrl = stdSku.getSourceUrl();

        this.skuAttrs = skuAttrs;
        this.skuPrices = skuPrices;
        this.skuImages = skuImages;

        this.paramGroups = skuDetail.getParamGroups();
        this.desc = skuDetail.getDesc();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStdProId() {
        return stdProId;
    }

    public void setStdProId(long stdProId) {
        this.stdProId = stdProId;
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

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public float getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(float refPrice) {
        this.refPrice = refPrice;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Map<String, StdSkuAttr> getSkuAttrs() {
        return skuAttrs;
    }

    public void setSkuAttrs(Map<String, StdSkuAttr> skuAttrs) {
        this.skuAttrs = skuAttrs;
    }

    public List<StdSkuPrice> getSkuPrices() {
        return skuPrices;
    }

    public void setSkuPrices(List<StdSkuPrice> skuPrices) {
        this.skuPrices = skuPrices;
    }

    public List<StdSkuImage> getSkuImages() {
        return skuImages;
    }

    public void setSkuImages(List<StdSkuImage> skuImages) {
        this.skuImages = skuImages;
    }

    public List<PtmStdSkuParamGroup> getParamGroups() {
        return paramGroups;
    }

    public void setParamGroups(List<PtmStdSkuParamGroup> paramGroups) {
        this.paramGroups = paramGroups;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
