package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2015/12/7.
 */
@Entity
public class PtmStdImage implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long stdProId; // ptm_std_product # 一定不为 0
    private long stdSkuId; // ptm_std_sku # 如果不能判定是哪个sku的，则为0

    private String oriImageUrl;// 原图片url

    private String oriImagePath; // 下载后的图片路径
    private String smallImagePath;
    private String bigImagePath;

    private int errTimes = 0;

    public PtmStdImage() {
        stdProId = 0;
    }

    public PtmStdImage(long stdSkuId, String oriImageUrl) {
        this();
        this.stdSkuId = stdSkuId;
        this.oriImageUrl = oriImageUrl;
    }

    public PtmStdImage(long stdSkuId, String oriImageUrl, String oriImagePath, String smallImagePath, String bigImagePath, int errTimes) {
        this();
        this.stdSkuId = stdSkuId;
        this.oriImageUrl = oriImageUrl;
        this.oriImagePath = oriImagePath;
        this.smallImagePath = smallImagePath;
        this.bigImagePath = bigImagePath;
        this.errTimes = errTimes;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getStdProId() {
        return stdProId;
    }

    public void setStdProId(long stdProId) {
        this.stdProId = stdProId;
    }

    public long getStdSkuId() {
        return stdSkuId;
    }

    public void setStdSkuId(long stdSkuId) {
        this.stdSkuId = stdSkuId;
    }

    public String getOriImagePath() {
        return oriImagePath;
    }

    public void setOriImagePath(String oriImagePath) {
        this.oriImagePath = oriImagePath;
    }

    public String getSmallImagePath() {
        return smallImagePath;
    }

    public void setSmallImagePath(String smallImagePath) {
        this.smallImagePath = smallImagePath;
    }

    public String getBigImagePath() {
        return bigImagePath;
    }

    public void setBigImagePath(String bigImagePath) {
        this.bigImagePath = bigImagePath;
    }

    public String getOriImageUrl() {
        return oriImageUrl;
    }

    public void setOriImageUrl(String oriImageUrl) {
        this.oriImageUrl = oriImageUrl;
    }

    public int getErrTimes() {
        return errTimes;
    }

    public void setErrTimes(int errTimes) {
        this.errTimes = errTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmStdImage that = (PtmStdImage) o;

        if (stdProId != that.stdProId) return false;
        if (stdSkuId != that.stdSkuId) return false;
        if (errTimes != that.errTimes) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (oriImagePath != null ? !oriImagePath.equals(that.oriImagePath) : that.oriImagePath != null) return false;
        if (smallImagePath != null ? !smallImagePath.equals(that.smallImagePath) : that.smallImagePath != null)
            return false;
        if (bigImagePath != null ? !bigImagePath.equals(that.bigImagePath) : that.bigImagePath != null) return false;
        return !(oriImageUrl != null ? !oriImageUrl.equals(that.oriImageUrl) : that.oriImageUrl != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (stdProId ^ (stdProId >>> 32));
        result = 31 * result + (int) (stdSkuId ^ (stdSkuId >>> 32));
        result = 31 * result + (oriImagePath != null ? oriImagePath.hashCode() : 0);
        result = 31 * result + (smallImagePath != null ? smallImagePath.hashCode() : 0);
        result = 31 * result + (bigImagePath != null ? bigImagePath.hashCode() : 0);
        result = 31 * result + (oriImageUrl != null ? oriImageUrl.hashCode() : 0);
        result = 31 * result + errTimes;
        return result;
    }
}
