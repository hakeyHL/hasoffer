package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created on 2016/7/28.
 */
@Entity
public class PtmCmpSkuImage implements Identifiable<Long> {

    private static final IdWorker idWorker = IdWorker.getInstance(PtmCmpSkuImage.class);

    @Id
    @Column(unique = true, nullable = false)
    private Long id = idWorker.nextLong();

    @Column(nullable = false)
    private long ptmcmpskuId;//ptmcmpsku id

    @Column(unique = true, nullable = false)
    private String oriImageUrl;//原始图片路径

    private String imagePath;//上传图片成功返回的path

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long aLong) {

    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOriImageUrl() {
        return oriImageUrl;
    }

    public void setOriImageUrl(String oriImageUrl) {
        this.oriImageUrl = oriImageUrl;
    }

    public long getPtmcmpskuId() {
        return ptmcmpskuId;
    }

    public void setPtmcmpskuId(long ptmcmpskuId) {
        this.ptmcmpskuId = ptmcmpskuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmCmpSkuImage that = (PtmCmpSkuImage) o;

        if (ptmcmpskuId != that.ptmcmpskuId) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (oriImageUrl != null ? !oriImageUrl.equals(that.oriImageUrl) : that.oriImageUrl != null) return false;
        return !(imagePath != null ? !imagePath.equals(that.imagePath) : that.imagePath != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (ptmcmpskuId ^ (ptmcmpskuId >>> 32));
        result = 31 * result + (oriImageUrl != null ? oriImageUrl.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        return result;
    }
}
