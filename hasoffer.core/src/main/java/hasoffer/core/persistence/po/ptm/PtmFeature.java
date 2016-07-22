package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by chevy on 2015/12/21.
 */
@Entity
public class PtmFeature implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(AppBanner.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private long productId;

	@Column(length = 2048)
	private String feature;

	public PtmFeature() {
	}

	public PtmFeature(long productId, String feature) {
		this.productId = productId;
		this.feature = feature;
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

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmFeature that = (PtmFeature) o;

		if (productId != that.productId) { return false; }
		if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
		return !(feature != null ? !feature.equals(that.feature) : that.feature != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (feature != null ? feature.hashCode() : 0);
		return result;
	}
}
