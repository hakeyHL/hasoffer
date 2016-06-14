package hasoffer.base.model;

import hasoffer.base.utils.StringUtils;

import java.io.UnsupportedEncodingException;

public class HttpResponseModel {
    private static final String DEFAULT_CHARSET = "UTF-8";

    private String realUrl;
    private int statusCode;
    private byte[] bodyBytes;
    private String bodyString;
    private String contentType;
    private String charset;
    private String redirect;

    private boolean hasException = false;
    private String exception;

    public HttpResponseModel(String exception) {
        this.hasException = true;
        this.exception = exception;
    }

	/*public HttpResponseModel(int statusCode, String contentType, String charset, byte[] bodyBytes) {
        this.statusCode = statusCode;
		this.contentType = contentType;
		this.charset = StringUtils.notNullTrim(charset);
		if (this.charset.length() == 0) {
			this.charset = DEFAULT_CHARSET;
		}
		this.bodyBytes = bodyBytes;
	}*/

    public HttpResponseModel(int statusCode, String contentType,
                             String charset, byte[] bodyBytes,
                             String bodyString, String redirect) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.charset = StringUtils.notNullTrim(charset);
        if (this.charset.length() == 0) {
            this.charset = DEFAULT_CHARSET;
        }
        this.bodyBytes = bodyBytes;
        this.bodyString = bodyString;
        this.redirect = redirect;
    }

    public HttpResponseModel(int statusCode, String contentType, String s, byte[] bytes) {
        this(statusCode, contentType, s, bytes, new String(bytes), "");
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public boolean isOk() {
        return !hasException && (statusCode == 200);
    }

    public boolean isHasException() {
        return hasException;
    }

    public String getException() {
        return exception;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCharset() {
        return charset;
    }

    public String getRealUrl() {
        return realUrl;
    }

    public void setRealUrl(String realUrl) {
        this.realUrl = realUrl;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public String getBodyString() {

        if (bodyBytes == null || bodyBytes.length == 0) {
            return "";
        }
        try {
            return new String(bodyBytes, charset);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
