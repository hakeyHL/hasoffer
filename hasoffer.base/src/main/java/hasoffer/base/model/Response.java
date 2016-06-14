package hasoffer.base.model;

/**
 * Created by glx on 2015/6/1.
 */
public class Response<T> {
	private T data;
	private int errorCode = 0;
	private String error="";
	private String message="";

	public Response() {
	}

	public Response(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Response{" +
				"data=" + data +
				", errorCode=" + errorCode +
				", error='" + error + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
