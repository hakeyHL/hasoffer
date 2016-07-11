package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.enums.TopSellStatus;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2015/12/29.
 * 用于每天的topselling
 */
@Entity
public class PtmTopSelling implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long productId;
    private Long count;
    private TopSellStatus status = TopSellStatus.WAIT;
    private long lUpdateTime = TimeUtils.now();

    public PtmTopSelling(long productId, Long count) {
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

    public TopSellStatus getStatus() {
        return status;
    }

    public void setStatus(TopSellStatus status) {
        this.status = status;
    }

    public long getlUpdateTime() {
        return lUpdateTime;
    }

    public void setlUpdateTime(long lUpdateTime) {
        this.lUpdateTime = lUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmTopSelling that = (PtmTopSelling) o;

        if (productId != that.productId) return false;
        if (lUpdateTime != that.lUpdateTime) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (count != null ? !count.equals(that.count) : that.count != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (productId ^ (productId >>> 32));
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (int) (lUpdateTime ^ (lUpdateTime >>> 32));
        return result;
    }
}
