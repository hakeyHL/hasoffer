package hasoffer.job.dto;

import hasoffer.base.model.Website;

public class FetchTestTaskDTO {
	private Long id;
	private Website website;
	private String url;
	
	public FetchTestTaskDTO(Long id, Website website, String url) {
		this.id = id;
		this.website = website;
		this.url = url;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Website getWebsite() {
		return website;
	}

	public void setWebsite(Website website) {
		this.website = website;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return id + "\t" + website + "\t" + url;
	}
}
