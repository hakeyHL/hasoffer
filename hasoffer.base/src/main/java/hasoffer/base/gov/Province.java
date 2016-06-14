package hasoffer.base.gov;

import java.util.List;

/**
 * Created by glx on 2015/6/11.
 */
public class Province {
	List<City> cities;
	private int code;
	private String name;

	public Province(int code, String name, List<City> cities) {
		this.code = code;
		this.name = name;
		this.cities = cities;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<City> getCities() {
		return cities;
	}

	public void setCities(List<City> cities) {
		this.cities = cities;
	}
}
