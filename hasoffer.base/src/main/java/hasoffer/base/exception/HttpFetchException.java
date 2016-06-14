package hasoffer.base.exception;

/**
 * Author: Wesley Wu
 * Date: 14/12/4 17:02
 */
public class HttpFetchException extends Exception {
	public HttpFetchException() {
		super();
	}

	public HttpFetchException(String message) {
		super(message);
	}

	public HttpFetchException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpFetchException(Throwable cause) {
		super(cause);
	}

	protected HttpFetchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
