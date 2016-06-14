package hasoffer.core.solr;

import org.apache.commons.lang.StringUtils;

/**
 * Created by glx on 2015/4/21.
 */
public class Query {
	private String field;
	private String value;
	private float boost = 1f;

	public Query(String field, String value) {
		this.field = field;
		this.value = value;
	}

	public Query(String field, String value, float boost) {
		this.field = field;
		this.value = value;
		this.boost = boost;
	}

	public static void main(String[] args) {
		String temp = "1/2/3/3/4/";
		String[] catTree = temp.split("\\/");
		System.out.print(catTree);
	}

	public float getBoost() {
		return boost;
	}

	public void setBoost(float boost) {
		this.boost = boost;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		if (StringUtils.isNotBlank(field)) {
			return field + ":" + value;
		} else {
			return value;
		}
	}

}
