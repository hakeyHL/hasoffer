package hasoffer.core.persistence.po.ptm;


import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/16
 */
@Entity
public class PtmModel implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(PtmModel.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private Date createTime = TimeUtils.nowDate();
	private String name;
	private String imagePath;
	private long brandId;
	private float price;
	private String accordingTo;

	public PtmModel() {}

	public PtmModel(String name) {
		this.name = name;
	}

	public PtmModel(String name, long brandId) {
		this.name = name;
		this.brandId = brandId;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getAccordingTo() {
		return accordingTo;
	}

	public void setAccordingTo(String accordingTo) {
		this.accordingTo = accordingTo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmModel model = (PtmModel) o;

		if (brandId != model.brandId) { return false; }
		if (Float.compare(model.price, price) != 0) { return false; }
		if (id != null ? !id.equals(model.id) : model.id != null) { return false; }
		if (createTime != null ? !createTime.equals(model.createTime) : model.createTime != null) { return false; }
		if (name != null ? !name.equals(model.name) : model.name != null) { return false; }
		if (imagePath != null ? !imagePath.equals(model.imagePath) : model.imagePath != null) { return false; }
		return !(accordingTo != null ? !accordingTo.equals(model.accordingTo) : model.accordingTo != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
		result = 31 * result + (int) (brandId ^ (brandId >>> 32));
		result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
		result = 31 * result + (accordingTo != null ? accordingTo.hashCode() : 0);
		return result;
	}
}
