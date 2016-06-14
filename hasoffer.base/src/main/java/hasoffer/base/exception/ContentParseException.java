package hasoffer.base.exception;

public class ContentParseException extends Exception {

	private String url;

	public ContentParseException(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
