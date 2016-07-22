package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * SKU销售属性表
 * Created by Chengwei Zhang on 2015年10月13日
 */
@Entity
public class PtmSkuSaleAttribute implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(AppBanner.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private long productId;
	private long skuId;
	private long attributeDefId;
	private long valueId;

	public PtmSkuSaleAttribute() {}

	public PtmSkuSaleAttribute(long productId, long skuId, long attributeDefId, long valueId) {
		this.productId = productId;
		this.skuId = skuId;
		this.attributeDefId = attributeDefId;
		this.valueId = valueId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public long getValueId() {
		return valueId;
	}

	public void setValueId(long valueId) {
		this.valueId = valueId;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PtmSkuSaleAttribute that = (PtmSkuSaleAttribute) o;

		if (productId != that.productId) return false;
		if (skuId != that.skuId) return false;
		if (attributeDefId != that.attributeDefId) return false;
		if (valueId != that.valueId) return false;
		return !(id != null ? !id.equals(that.id) : that.id != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (int) (skuId ^ (skuId >>> 32));
		result = 31 * result + (int) (attributeDefId ^ (attributeDefId >>> 32));
		result = 31 * result + (int) (valueId ^ (valueId >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "PtmSkuSaleAttribute{" +
				"id=" + id +
				", productId=" + productId +
				", skuId=" + skuId +
				", attributeDefId=" + attributeDefId +
				", valueId=" + valueId +
				'}';
	}
}
