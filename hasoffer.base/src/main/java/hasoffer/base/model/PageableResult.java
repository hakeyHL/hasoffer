package hasoffer.base.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by glx on 2015/4/10.
 */
public class PageableResult<T> implements Serializable {
	private List<T> data;
	private long numFund;
	private long totalPage;
	private long currentPage;
	private long pageSize;

	public PageableResult() {
	}

	public PageableResult(List<T> data, long numFund, long currentPage, long pageSize) {
		this.data = data;
		this.numFund = numFund;
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		totalPage = (long) Math.ceil((double) numFund / (double) pageSize);
	}

	public long getNumFund() {
		return numFund;
	}

	public List<T> getData() {
		return data;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public long getCurrentPage() {
		return currentPage;
	}

	public long getPageSize() {
		return pageSize;
	}


}
