package hasoffer.core.persistence.po.search;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.enums.SrmSearchLogUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/3/12.
 */
@Entity
public class SrmSearchUpdateLog implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long operatorId;

    @Enumerated(EnumType.STRING)
    private SrmSearchLogUpdate srmUpdate;// UPDATELOG,NEWPRODUCT,NEWCMPSKU
    private String targetId;
    private String oldValue;
    private String newValue;
    private Date createTime = TimeUtils.nowDate();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(long operatorId) {
        this.operatorId = operatorId;
    }

    public SrmSearchLogUpdate getSrmUpdate() {
        return srmUpdate;
    }

    public void setSrmUpdate(SrmSearchLogUpdate srmUpdate) {
        this.srmUpdate = srmUpdate;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SrmSearchUpdateLog that = (SrmSearchUpdateLog) o;

        if (operatorId != that.operatorId) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (srmUpdate != that.srmUpdate) return false;
        if (targetId != null ? !targetId.equals(that.targetId) : that.targetId != null) return false;
        if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) return false;
        if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) return false;
        return !(createTime != null ? !createTime.equals(that.createTime) : that.createTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (operatorId ^ (operatorId >>> 32));
        result = 31 * result + (srmUpdate != null ? srmUpdate.hashCode() : 0);
        result = 31 * result + (targetId != null ? targetId.hashCode() : 0);
        result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
    }
}
