package hasoffer.core.persistence.po.admin;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by hs on 2016年07月26日.
 * Time 12:51
 */
@Entity
public class AdCategory implements Identifiable<Long> {
    private static final IdWorker idWorker = IdWorker.getInstance(AdCategory.class);
    @Id
    @Column(unique = true, nullable = false)
    private Long id = idWorker.nextLong();
    private Long categgoryId;
    private Long advertisementId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(Long advertisementId) {
        this.advertisementId = advertisementId;
    }

    public Long getCateggoryId() {
        return categgoryId;
    }

    public void setCateggoryId(Long categgoryId) {
        this.categgoryId = categgoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdCategory that = (AdCategory) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (categgoryId != null ? !categgoryId.equals(that.categgoryId) : that.categgoryId != null) return false;
        return !(advertisementId != null ? !advertisementId.equals(that.advertisementId) : that.advertisementId != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (categgoryId != null ? categgoryId.hashCode() : 0);
        result = 31 * result + (advertisementId != null ? advertisementId.hashCode() : 0);
        return result;
    }
}