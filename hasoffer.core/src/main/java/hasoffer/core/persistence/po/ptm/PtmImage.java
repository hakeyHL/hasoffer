package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2015/12/7.
 */
@Entity
public class PtmImage implements Identifiable<Long> {
	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private long productId; // #

	private String imageUrl;
	private String path;

	private int errTimes;

	public PtmImage() {}

	public PtmImage(long productId, String imageUrl) {
		this.productId = productId;
		this.imageUrl = imageUrl;
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

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

		PtmImage image = (PtmImage) o;

		if (productId != image.productId) return false;
		if (errTimes != image.errTimes) return false;
		if (id != null ? !id.equals(image.id) : image.id != null) return false;
		if (imageUrl != null ? !imageUrl.equals(image.imageUrl) : image.imageUrl != null) return false;
		return !(path != null ? !path.equals(image.path) : image.path != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
		result = 31 * result + (path != null ? path.hashCode() : 0);
		result = 31 * result + errTimes;
		return result;
	}
}
