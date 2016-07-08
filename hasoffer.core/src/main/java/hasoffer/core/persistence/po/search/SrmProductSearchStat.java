package hasoffer.core.persistence.po.search;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created on 2015/12/29.
 * 统计每天比价质量
 */
@Entity
public class SrmProductSearchStat implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    private String id;
    private Date createTime = TimeUtils.nowDate();

    private int skuCount0;// 没有匹配到sku的日志
    private int skuCount1;// 比价数量是1个
    private int skuCount2;// 比价数量是2个
    private int skuCount3;// 比价数量是3个
    private int skuCount4;// 比价数量是4个以上的

    public SrmProductSearchStat() {
    }

    public SrmProductSearchStat(String id, int skuCount0, int skuCount1, int skuCount2, int skuCount3, int skuCount4) {
        this.id = id;
        this.skuCount0 = skuCount0;
        this.skuCount1 = skuCount1;
        this.skuCount2 = skuCount2;
        this.skuCount3 = skuCount3;
        this.skuCount4 = skuCount4;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getSkuCount0() {
        return skuCount0;
    }

    public void setSkuCount0(int skuCount0) {
        this.skuCount0 = skuCount0;
    }

    public int getSkuCount1() {
        return skuCount1;
    }

    public void setSkuCount1(int skuCount1) {
        this.skuCount1 = skuCount1;
    }

    public int getSkuCount2() {
        return skuCount2;
    }

    public void setSkuCount2(int skuCount2) {
        this.skuCount2 = skuCount2;
    }

    public int getSkuCount3() {
        return skuCount3;
    }

    public void setSkuCount3(int skuCount3) {
        this.skuCount3 = skuCount3;
    }

    public int getSkuCount4() {
        return skuCount4;
    }

    public void setSkuCount4(int skuCount4) {
        this.skuCount4 = skuCount4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SrmProductSearchStat that = (SrmProductSearchStat) o;

        if (skuCount0 != that.skuCount0) return false;
        if (skuCount1 != that.skuCount1) return false;
        if (skuCount2 != that.skuCount2) return false;
        if (skuCount3 != that.skuCount3) return false;
        if (skuCount4 != that.skuCount4) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(createTime != null ? !createTime.equals(that.createTime) : that.createTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + skuCount0;
        result = 31 * result + skuCount1;
        result = 31 * result + skuCount2;
        result = 31 * result + skuCount3;
        result = 31 * result + skuCount4;
        return result;
    }
}
