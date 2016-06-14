package hasoffer.core.persistence.po.thd;


import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.enums.RelateType;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2015/12/7.
 * function : 本类是其他ThdXProduct po类 的父类，不创建数据库表，所以定义为 抽象类
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ThdProduct implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE)
    protected Long id;

    protected long ptmCateId;

    @Column(unique = true)
    protected String sourceId;
    protected String url;
    protected String imageUrl;
    protected String title;
    protected Date createTime = TimeUtils.nowDate();
    protected float price;
    @Enumerated(EnumType.STRING)
    protected Website website;
    protected long cmpSkuId;
    @Enumerated(EnumType.STRING)
    protected RelateType relateType = RelateType.NONE;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getPtmCateId() {
        return ptmCateId;
    }

    public void setPtmCateId(long ptmCateId) {
        this.ptmCateId = ptmCateId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public long getCmpSkuId() {
        return cmpSkuId;
    }

    public void setCmpSkuId(long cmpSkuId) {
        this.cmpSkuId = cmpSkuId;
    }

    public RelateType getRelateType() {
        return relateType;
    }

    public void setRelateType(RelateType relateType) {
        this.relateType = relateType;
    }
}
