package hasoffer.core.persistence.po.log;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/4/22.
 */
@Entity
public class ShopcluesFixLog implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    private Long id;//ptmcmpsku id

    private String oriUrl;
    @Enumerated(EnumType.STRING)
    private SkuStatus oriStatus;
    @Enumerated(EnumType.STRING)
    private SkuStatus newStatus;
    private String newUrl;
    private Date updateTime = TimeUtils.nowDate();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNewUrl() {
        return newUrl;
    }

    public void setNewUrl(String newUrl) {
        this.newUrl = newUrl;
    }

    public String getOriUrl() {
        return oriUrl;
    }

    public void setOriUrl(String oriUrl) {
        this.oriUrl = oriUrl;
    }

    public SkuStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(SkuStatus newStatus) {
        this.newStatus = newStatus;
    }

    public SkuStatus getOriStatus() {
        return oriStatus;
    }

    public void setOriStatus(SkuStatus oriStatus) {
        this.oriStatus = oriStatus;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopcluesFixLog that = (ShopcluesFixLog) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (oriUrl != null ? !oriUrl.equals(that.oriUrl) : that.oriUrl != null) return false;
        if (oriStatus != that.oriStatus) return false;
        if (newStatus != that.newStatus) return false;
        if (newUrl != null ? !newUrl.equals(that.newUrl) : that.newUrl != null) return false;
        return !(updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (oriUrl != null ? oriUrl.hashCode() : 0);
        result = 31 * result + (oriStatus != null ? oriStatus.hashCode() : 0);
        result = 31 * result + (newStatus != null ? newStatus.hashCode() : 0);
        result = 31 * result + (newUrl != null ? newUrl.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        return result;
    }
}
