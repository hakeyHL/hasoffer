package hasoffer.core.solr;

import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.common.util.NamedList;

import java.util.List;

/**
 * Created by glx on 2015/2/5.
 */
public class SearchResult<T> {
	NamedList<List<PivotField>> facetPivot;
	private List<T> result;
	private long totalCount = 0;
	private Matrix facetMatrix;

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public Matrix getFacetMatrix() {
		return facetMatrix;
	}

	public void setFacetMatrix(Matrix facetMatrix) {
		this.facetMatrix = facetMatrix;
	}

	public NamedList<List<PivotField>> getFacetPivot() {
		return facetPivot;
	}

	public void setFacetPivot(NamedList<List<PivotField>> facetPivot) {
		this.facetPivot = facetPivot;
	}
}
