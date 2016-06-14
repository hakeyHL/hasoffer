package hasoffer.admin.controller.vo;

import hasoffer.core.persistence.po.ptm.PtmBasicAttribute;

/**
 * Created on 2015/12/22.
 */
public class ProductBasicAttributeVo {

	private String groupName;

	private long id;

	private String name;

	private String value;



	public ProductBasicAttributeVo(PtmBasicAttribute basicAttribute) {
		this.groupName = basicAttribute.getGroupName();
		this.id = basicAttribute.getId();
		this.name = basicAttribute.getName();
		this.value = basicAttribute.getValue();
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
}
