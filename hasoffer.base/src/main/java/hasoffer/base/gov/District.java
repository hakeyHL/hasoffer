package hasoffer.base.gov;

/**
 * Created by glx on 2015/6/11.
 */
public class District {
	private int code;
	private String name;
	private City city;

	public District(int code, String name, City city) {
		this.code = code;
		this.name = name;
		this.city = city;
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

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}
}
