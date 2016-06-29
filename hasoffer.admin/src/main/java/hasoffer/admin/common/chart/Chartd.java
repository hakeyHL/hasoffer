package hasoffer.admin.common.chart;

import java.util.List;

/**
 * Date : 2016/1/22
 * Function :
 */
class Chartd {
	String name;
	List<Double> data;

	public Chartd(String name, List<Double> data) {
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Double> getData() {
		return data;
	}

	public void setData(List<Double> data) {
		this.data = data;
	}
}
