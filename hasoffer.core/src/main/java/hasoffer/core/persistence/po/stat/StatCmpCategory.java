package hasoffer.core.persistence.po.stat;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created by chevy on 2016/12/1.
 */
@Entity
public class StatCmpCategory implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ymd;

    private long cateId;
    private String cateName;

    private long count;
    private long totalCount;

    public StatCmpCategory(String ymd, long cateId, String cateName, long count, long totalCount) {
        this.ymd = ymd;
        this.cateId = cateId;
        this.cateName = cateName;
        this.count = count;
        this.totalCount = totalCount;
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

    public long getCateId() {
        return cateId;
    }

    public void setCateId(long cateId) {
        this.cateId = cateId;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
