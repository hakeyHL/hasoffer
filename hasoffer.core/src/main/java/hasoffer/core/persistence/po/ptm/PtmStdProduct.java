package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;


@Entity
public class PtmStdProduct implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date createTime = TimeUtils.nowDate();

    private long categoryId;

    private String title;// 标题

    @Column(columnDefinition = "longtext")
    private String defaultDesc; // 默认的描述信息

    public PtmStdProduct() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDefaultDesc() {
        return defaultDesc;
    }

    public void setDefaultDesc(String defaultDesc) {
        this.defaultDesc = defaultDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmStdProduct that = (PtmStdProduct) o;

        if (categoryId != that.categoryId) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return !(defaultDesc != null ? !defaultDesc.equals(that.defaultDesc) : that.defaultDesc != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (int) (categoryId ^ (categoryId >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (defaultDesc != null ? defaultDesc.hashCode() : 0);
        return result;
    }
}
