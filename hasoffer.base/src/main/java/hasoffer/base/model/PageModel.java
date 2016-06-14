package hasoffer.base.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/11/4
 */
public class PageModel {

	private long recordCount;
	private long pageCount;
	private long pageNum;
	private long pageSize;

	private Map<String, String> pageParams = new HashMap<String, String>();

	private String requestUrl;

	public PageModel(long recordCount, long pageCount, long pageNum, long pageSize) {
		this.recordCount = recordCount;
		this.pageCount = pageCount;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.requestUrl = "";
	}

	public PageModel() {

	}

	public PageModel(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public void set(PageableResult result) {
		this.pageNum = result.getCurrentPage();
		this.pageSize = result.getPageSize();
		this.recordCount = result.getNumFund();
		this.pageCount = result.getTotalPage();
	}

	public long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(long recordCount) {
		this.recordCount = recordCount;
	}

	public long getPageCount() {
		return pageCount;
	}

	public void setPageCount(long pageCount) {
		this.pageCount = pageCount;
	}

	public long getPageNum() {
		return pageNum;
	}

	public void setPageNum(long pageNum) {
		this.pageNum = pageNum;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public Map<String, String> getPageParams() {
		return pageParams;
	}

	public void setPageParams(Map<String, String> pageParams) {
		this.pageParams = pageParams;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
}
