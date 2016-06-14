package hasoffer.base.gov;

import java.util.List;

/**
 * Created by glx on 2015/6/11.
 */
public class City {
	private int code;
	private String name;
	private List<District> districts;
	private Province province;

	public City(int code, String name, List<District> districts, Province province) {
		this.code = code;
		this.name = name;
		this.districts = districts;
		this.province = province;
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

	public List<District> getDistricts() {
		return districts;
	}

	public void setDistricts(List<District> districts) {
		this.districts = districts;
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}
}
