package hasoffer.core.solr;


/**
 * Created by glx on 2015/2/5.
 */
public interface IIndexService<M, T extends IIdentifiable<M>> {
	void createOrUpdate(T... ts);

	void remove(String id);

	SearchResult<M> search(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets,int pageNumber, int pageSize);

	SearchResult<M> search(Query[] qs, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets,int pageNumber, int pageSize, boolean useCache);

	SearchResult<M> search(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets,int pageNumber, int pageSize);

	SearchResult<M> search(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets,int pageNumber, int pageSize, boolean useCache);

	SearchResult<T> searchObjs(String q, FilterQuery[] fqs, Sort[] sorts, PivotFacet[] pivotFacets,int pageNumber, int pageSize, boolean useCache);
}
