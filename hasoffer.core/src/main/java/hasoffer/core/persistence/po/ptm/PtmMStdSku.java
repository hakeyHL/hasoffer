package hasoffer.core.persistence.po.ptm;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by hs on 2017年03月02日.
 * Time 12:05
 */
@Entity
public class PtmMStdSku implements Identifiable<Long> {
    //id,sourceId,price,title,originPrice,shippingFee,skuStatus,website,url
    //sourceUrl,ratings,returnDays,productId,supportPays,matchTitle,offers,color,size
    //deliveryTime,createTime,updateTime
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceId;
    @Column(columnDefinition = "float default 0")
    private Float price = 0f;
    private String title;
    private Float originPrice;
    private Integer shippingFee = -01;
    @Enumerated(EnumType.STRING)
    private SkuStatus skuStatus = SkuStatus.ONSALE;
    @Enumerated(EnumType.STRING)
    private Website website;
    private String url;
    private String sourceUrl;
    @Column(columnDefinition = "int default 0")
    private Integer ratings = 0;
    private String returnDays = "";
    private Long productId = -1l;
    private String supportPays;
    private String matchTitle;
    //    private String offers; 应该放到mongo中
    private String color;
    private String size;
    private String deliverTime;
    private Date createTime;
    private Date updateTime;

    @Transient
    private List<String> offerList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
        if (this.originPrice == null) {
            this.originPrice = price;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Float getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(Float originPrice) {
        this.originPrice = originPrice;
    }

    public Integer getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Integer shippingFee) {
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

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Integer getRatings() {
        return ratings;
    }

    public void setRatings(Integer ratings) {
        this.ratings = ratings;
    }

    public String getReturnDays() {
        return returnDays;
    }

    public void setReturnDays(String returnDays) {
        this.returnDays = returnDays;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSupportPays() {
        return supportPays;
    }

    public void setSupportPays(String supportPays) {
        if (StringUtils.isEmpty(supportPays)) {
            return;
        } else {
            if (StringUtils.isNotEmpty(this.supportPays)) {
                //如果之前就有且不相同
                if (!this.supportPays.equals(supportPays)) {
                    this.supportPays = this.supportPays + "," + supportPays;
                } else {
                    //相同就跳过
                    this.supportPays = supportPays;
                }
            } else {
                this.supportPays = supportPays;
            }
        }
    }

    public String getMatchTitle() {
        return matchTitle;
    }

    public void setMatchTitle(String matchTitle) {
        this.matchTitle = matchTitle;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String colr) {
        this.color = colr;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(String deliverTime) {
        this.deliverTime = deliverTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<String> getOfferList() {
        return offerList;
    }

    public void setOfferList(List<String> offerList) {
        this.offerList = offerList;
    }
}
