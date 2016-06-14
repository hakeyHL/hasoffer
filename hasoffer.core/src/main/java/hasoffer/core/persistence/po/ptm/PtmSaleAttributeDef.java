package hasoffer.core.persistence.po.ptm;


import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * 商品规格定义表
 * Created by Chengwei Zhang on 2015年10月13日
 */
@Entity
public class PtmSaleAttributeDef implements Identifiable<Long> {

	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Date createTime = TimeUtils.nowDate();

	private long productId;

	private String name;// 规格名称

	private long defaultValueId;//默认值id

	public PtmSaleAttributeDef() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDefaultValueId() {
		return defaultValueId;
	}

	public void setDefaultValueId(long defaultValueId) {
		this.defaultValueId = defaultValueId;
	}

	@Override
	public String toString() {
		return "PtmSaleAttributeDef{" +
		       "id=" + id +
		       ", createTime=" + createTime +
		       ", productId=" + productId +
		       ", name='" + name + '\'' +
		       ", defaultValueId=" + defaultValueId +
		       '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmSaleAttributeDef that = (PtmSaleAttributeDef) o;

		if (productId != that.productId) { return false; }
		if (defaultValueId != that.defaultValueId) { return false; }
		if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
		if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) { return false; }
		return !(name != null ? !name.equals(that.name) : that.name != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (int) (defaultValueId ^ (defaultValueId >>> 32));
		return result;
	}
}
