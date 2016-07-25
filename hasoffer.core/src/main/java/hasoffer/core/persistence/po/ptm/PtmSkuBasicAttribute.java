package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * SKU销售属性表
 * Created by Chengwei Zhang on 2015年10月13日
 */
@Entity
public class PtmSkuBasicAttribute implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(PtmSkuBasicAttribute.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private long productId;
	private long skuId;
	private long attributeDefId;

	@Column(columnDefinition = "longtext")
	private String value;

	public PtmSkuBasicAttribute() {}

	public PtmSkuBasicAttribute(long productId, long skuId, long attributeDefId, String value) {
		this.productId = productId;
		this.skuId = skuId;
		this.attributeDefId = attributeDefId;
		this.value = value;
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

	public long getSkuId() {
		return skuId;
	}

	public void setSkuId(long skuId) {
		this.skuId = skuId;
	}

	public long getAttributeDefId() {
		return attributeDefId;
	}

	public void setAttributeDefId(long attributeDefId) {
		this.attributeDefId = attributeDefId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PtmSkuBasicAttribute that = (PtmSkuBasicAttribute) o;

		if (productId != that.productId) return false;
		if (skuId != that.skuId) return false;
		if (attributeDefId != that.attributeDefId) return false;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		return !(value != null ? !value.equals(that.value) : that.value != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (int) (skuId ^ (skuId >>> 32));
		result = 31 * result + (int) (attributeDefId ^ (attributeDefId >>> 32));
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "PtmSkuBasicAttribute{" +
				"id=" + id +
				", productId=" + productId +
				", skuId=" + skuId +
				", attributeDefId=" + attributeDefId +
				", value='" + value + '\'' +
				'}';
	}
}
