package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by chevy on 2015/12/7.
 */
@Entity
public class PtmBasicAttribute implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(PtmBasicAttribute.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();
	
	private long productId;

	private String name;//对应PtmBasicAttributeDef 的 id

	@Column(columnDefinition = "text")
	private String value;

	private String groupName;

	public PtmBasicAttribute(long productId, String name, String value, String group) {
		this.productId = productId;
		this.name = name;
		this.value = value;
		this.groupName = group;
	}

	public PtmBasicAttribute() {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmBasicAttribute that = (PtmBasicAttribute) o;

		if (productId != that.productId) { return false; }
		if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
		if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
		if (value != null ? !value.equals(that.value) : that.value != null) { return false; }
		return !(groupName != null ? !groupName.equals(that.groupName) : that.groupName != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
		return result;
	}
}
