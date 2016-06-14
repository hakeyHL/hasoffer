package hasoffer.core.persistence.po.log;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/6/12.
 */
@Entity
public class SkuUpdateLog implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    private Website website;

    private long needUpdateAmount;
    private long updateSuccessAmount;

    private Date createTime;
    private Date updateTime;

    public SkuUpdateLog() {
    }

    public SkuUpdateLog(String id, long needUpdateAmount, long updateSuccessAmount, Website website) {
        this.id = id;
        this.needUpdateAmount = needUpdateAmount;
        this.updateSuccessAmount = updateSuccessAmount;
        this.website = website;
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

    public long getNeedUpdateAmount() {
        return needUpdateAmount;
    }

    public void setNeedUpdateAmount(long needUpdateAmount) {
        this.needUpdateAmount = needUpdateAmount;
    }

    public long getUpdateSuccessAmount() {
        return updateSuccessAmount;
    }

    public void setUpdateSuccessAmount(long updateSuccessAmount) {
        this.updateSuccessAmount = updateSuccessAmount;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setId(String s) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SkuUpdateLog that = (SkuUpdateLog) o;

        if (needUpdateAmount != that.needUpdateAmount) return false;
        if (updateSuccessAmount != that.updateSuccessAmount) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return website == that.website;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (int) (needUpdateAmount ^ (needUpdateAmount >>> 32));
        result = 31 * result + (int) (updateSuccessAmount ^ (updateSuccessAmount >>> 32));
        return result;
    }
}
