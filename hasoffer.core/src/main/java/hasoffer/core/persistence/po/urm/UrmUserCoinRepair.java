package hasoffer.core.persistence.po.urm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by hs on 2016年12月28日.
 * Time 18:01
 */
@Entity
public class UrmUserCoinRepair implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    private Long id;

    private Long createTime;

    private String note;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
