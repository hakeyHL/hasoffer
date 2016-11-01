package hasoffer.core.persistence.po.search;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2015/12/29.
 * 1 记录每天被搜索到的商品
 * 2 统计匹配比价数量
 * 3 保存每天被搜索次数最多的20个商品 - top selling
 */
@Entity
public class SrmProductSearchCountByHour implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long productId;

    private String ymdHour;// ymd_hour

    private Long count;
    private int skuCount;//sku 的数量

    public SrmProductSearchCountByHour() {
    }

    public SrmProductSearchCountByHour(String ymdHour, long productId, Long count, int skuCount) {
        this.productId = productId;
        this.ymdHour = ymdHour;
        this.count = count;
        this.skuCount = skuCount;
    }

    public Long getId() {
        return id;
    }

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

    public int getSkuCount() {
        return skuCount;
    }

    public void setSkuCount(int skuCount) {
        this.skuCount = skuCount;
    }

    public String getYmdHour() {
        return ymdHour;
    }

    public void setYmdHour(String ymdHour) {
        this.ymdHour = ymdHour;
    }
}
