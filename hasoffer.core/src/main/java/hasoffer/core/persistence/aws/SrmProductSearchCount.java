package hasoffer.core.persistence.aws;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Created on 2015/12/29.
 * 1 记录每天被搜索到的商品
 * 2 统计匹配比价数量
 * 3 保存每天被搜索次数最多的20个商品 - top selling
 */
@DynamoDBTable(tableName = "SrmProductSearchCount")
public class SrmProductSearchCount {

    @DynamoDBHashKey(attributeName = "proId")
    private long proId;

    private String ymd;//日期

    private Long count;
    private int skuCount;//sku 的数量

    public SrmProductSearchCount() {
    }

    public SrmProductSearchCount(long productId, String ymd, Long count, int skuCount) {
        this.proId = productId;
        this.ymd = ymd;
        this.count = count;
        this.skuCount = skuCount;
    }

    public long getProId() {
        return proId;
    }

    public void setProId(long proId) {
        this.proId = proId;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SrmProductSearchCount that = (SrmProductSearchCount) o;

        if (proId != that.proId) return false;
        if (skuCount != that.skuCount) return false;
        if (ymd != null ? !ymd.equals(that.ymd) : that.ymd != null) return false;
        return !(count != null ? !count.equals(that.count) : that.count != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (proId ^ (proId >>> 32));
        result = 31 * result + (ymd != null ? ymd.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + skuCount;
        return result;
    }
}
