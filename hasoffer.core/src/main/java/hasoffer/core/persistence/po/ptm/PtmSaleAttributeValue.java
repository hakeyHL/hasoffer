package hasoffer.core.persistence.po.ptm;


import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * 商品规格值表
 * Created by Chengwei Zhang on 2015年10月13日
 */
@Entity
public class PtmSaleAttributeValue implements Identifiable<Long> {

	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Date createTime = TimeUtils.nowDate();

	private long attributeDefId;

	private String strValue;
	private String imagePath;

	public PtmSaleAttributeValue() {}

	public PtmSaleAttributeValue(long attributeDefId, String strValue, String imagePath) {
		this.attributeDefId = attributeDefId;
		this.strValue = strValue;
		this.imagePath = imagePath;
	}

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

	public long getAttributeDefId() {
		return attributeDefId;
	}

	public void setAttributeDefId(long attributeDefId) {
		this.attributeDefId = attributeDefId;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	@Override
	public String toString() {
		return "PtmSaleAttributeValue{" +
		       "id=" + id +
		       ", createTime=" + createTime +
		       ", attributeDefId=" + attributeDefId +
		       ", strValue='" + strValue + '\'' +
		       ", imagePath='" + imagePath + '\'' +
		       '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmSaleAttributeValue that = (PtmSaleAttributeValue) o;

		if (attributeDefId != that.attributeDefId) { return false; }
		if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
		if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) { return false; }
		if (strValue != null ? !strValue.equals(that.strValue) : that.strValue != null) { return false; }
		return !(imagePath != null ? !imagePath.equals(that.imagePath) : that.imagePath != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
		result = 31 * result + (int) (attributeDefId ^ (attributeDefId >>> 32));
		result = 31 * result + (strValue != null ? strValue.hashCode() : 0);
		result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
		return result;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
