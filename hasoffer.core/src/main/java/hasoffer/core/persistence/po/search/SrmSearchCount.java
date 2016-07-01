package hasoffer.core.persistence.po.search;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2015/12/29.
 */
@Entity
public class SrmSearchCount implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ymd;
    private long productId;
    private Long count;

    public SrmSearchCount() {
    }

    public SrmSearchCount(String ymd, long productId, Long count) {
        this.ymd = ymd;
        this.productId = productId;
        this.count = count;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SrmSearchCount that = (SrmSearchCount) o;

        if (productId != that.productId) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (ymd != null ? !ymd.equals(that.ymd) : that.ymd != null) return false;
        return !(count != null ? !count.equals(that.count) : that.count != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ymd != null ? ymd.hashCode() : 0);
        result = 31 * result + (int) (productId ^ (productId >>> 32));
        result = 31 * result + (count != null ? count.hashCode() : 0);
        return result;
    }
}