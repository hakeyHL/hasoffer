package hasoffer.core.persistence.po.stat;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/5/30.
 */
@Entity
public class StatPtmCmpSkuUpdate implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    private String id;//ex:HexDigestUtil.md5(website_20160530)

    @Enumerated(EnumType.STRING)
    private Website website;
    private Date date;//记录该条记录对应的时间
    private Date updateTime;

    private long onSaleAmount;//onsale数量
    private long soldOutAmount;//sold out数量
    private long offsaleAmount;//offsale数量
    private long allAmount;//所有sku数量
    private long updateSuccessAmount;//当日更新成功数量
    private long alwaysFailAmount;//默认3日以上未更新的数量
    private long newSkuAmount;//当日新增sku数量
    private long indexAmount;//该网站所有索引数量
    private long newIndexAmount;//该网站当日新增索引数量

    public StatPtmCmpSkuUpdate() {
    }

    public StatPtmCmpSkuUpdate(String id, long allAmount, long alwaysFailAmount, Date date, long indexAmount, long newIndexAmount, long newSkuAmount, long offsaleAmount, long onSaleAmount, long soldOutAmount, long updateSuccessAmount, Date updateTime, Website website) {
        this.id = id;
        this.allAmount = allAmount;
        this.alwaysFailAmount = alwaysFailAmount;
        this.date = date;
        this.indexAmount = indexAmount;
        this.newIndexAmount = newIndexAmount;
        this.newSkuAmount = newSkuAmount;
        this.offsaleAmount = offsaleAmount;
        this.onSaleAmount = onSaleAmount;
        this.soldOutAmount = soldOutAmount;
        this.updateSuccessAmount = updateSuccessAmount;
        this.updateTime = updateTime;
        this.website = website;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setId(String s) {

    }

    public long getAllAmount() {
        return allAmount;
    }

    public void setAllAmount(long allAmount) {
        this.allAmount = allAmount;
    }

    public long getAlwaysFailAmount() {
        return alwaysFailAmount;
    }

    public void setAlwaysFailAmount(long alwaysFailAmount) {
        this.alwaysFailAmount = alwaysFailAmount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getIndexAmount() {
        return indexAmount;
    }

    public void setIndexAmount(long indexAmount) {
        this.indexAmount = indexAmount;
    }

    public long getNewIndexAmount() {
        return newIndexAmount;
    }

    public void setNewIndexAmount(long newIndexAmount) {
        this.newIndexAmount = newIndexAmount;
    }

    public long getNewSkuAmount() {
        return newSkuAmount;
    }

    public void setNewSkuAmount(long newSkuAmount) {
        this.newSkuAmount = newSkuAmount;
    }

    public long getOffsaleAmount() {
        return offsaleAmount;
    }

    public void setOffsaleAmount(long offsaleAmount) {
        this.offsaleAmount = offsaleAmount;
    }

    public long getOnSaleAmount() {
        return onSaleAmount;
    }

    public void setOnSaleAmount(long onSaleAmount) {
        this.onSaleAmount = onSaleAmount;
    }

    public long getSoldOutAmount() {
        return soldOutAmount;
    }

    public void setSoldOutAmount(long soldOutAmount) {
        this.soldOutAmount = soldOutAmount;
    }

    public long getUpdateSuccessAmount() {
        return updateSuccessAmount;
    }

    public void setUpdateSuccessAmount(long updateSuccessAmount) {
        this.updateSuccessAmount = updateSuccessAmount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatPtmCmpSkuUpdate that = (StatPtmCmpSkuUpdate) o;

        if (onSaleAmount != that.onSaleAmount) return false;
        if (soldOutAmount != that.soldOutAmount) return false;
        if (offsaleAmount != that.offsaleAmount) return false;
        if (allAmount != that.allAmount) return false;
        if (updateSuccessAmount != that.updateSuccessAmount) return false;
        if (alwaysFailAmount != that.alwaysFailAmount) return false;
        if (newSkuAmount != that.newSkuAmount) return false;
        if (indexAmount != that.indexAmount) return false;
        if (newIndexAmount != that.newIndexAmount) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (website != that.website) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        return !(updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (int) (onSaleAmount ^ (onSaleAmount >>> 32));
        result = 31 * result + (int) (soldOutAmount ^ (soldOutAmount >>> 32));
        result = 31 * result + (int) (offsaleAmount ^ (offsaleAmount >>> 32));
        result = 31 * result + (int) (allAmount ^ (allAmount >>> 32));
        result = 31 * result + (int) (updateSuccessAmount ^ (updateSuccessAmount >>> 32));
        result = 31 * result + (int) (alwaysFailAmount ^ (alwaysFailAmount >>> 32));
        result = 31 * result + (int) (newSkuAmount ^ (newSkuAmount >>> 32));
        result = 31 * result + (int) (indexAmount ^ (indexAmount >>> 32));
        result = 31 * result + (int) (newIndexAmount ^ (newIndexAmount >>> 32));
        return result;
    }
}
