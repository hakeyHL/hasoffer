package hasoffer.core.solr;

import java.util.List;

/**
 * Created by glx on 2015/11/9.
 */
public class PivotFacetCount {
    private List<PivotFacet> pivotFacets;
    private long count;

    public PivotFacetCount(List<PivotFacet> pivotFacets, long count) {
        this.pivotFacets = pivotFacets;
        this.count = count;
    }

    public List<PivotFacet> getPivotFacets() {
        return pivotFacets;
    }

    public void setPivotFacets(List<PivotFacet> pivotFacets) {
        this.pivotFacets = pivotFacets;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
