package hasoffer.core.persistence.po.ptm;


import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * 商品类别信息表
 * Created by glx on 2014/7/25.
 */
@Entity
public class PtmCategoryBasicAttribute implements Identifiable<Long> {
	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private long categoryId;
	private long basicAttributeDefId;
	private boolean filterCondition;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public long getBasicAttributeDefId() {
		return basicAttributeDefId;
	}

	public void setBasicAttributeDefId(long basicAttributeDefId) {
		this.basicAttributeDefId = basicAttributeDefId;
	}

	public boolean isFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(boolean filterCondition) {
		this.filterCondition = filterCondition;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PtmCategoryBasicAttribute that = (PtmCategoryBasicAttribute) o;

		if (categoryId != that.categoryId) return false;
		if (basicAttributeDefId != that.basicAttributeDefId) return false;
		return !(id != null ? !id.equals(that.id) : that.id != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (categoryId ^ (categoryId >>> 32));
		result = 31 * result + (int) (basicAttributeDefId ^ (basicAttributeDefId >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "PtmCategoryBasicAttribute{" +
				"id=" + id +
				", categoryId=" + categoryId +
				", basicAttributeDefId=" + basicAttributeDefId +
				'}';
	}
}
