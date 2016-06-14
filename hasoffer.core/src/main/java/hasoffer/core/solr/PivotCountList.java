package hasoffer.core.solr;

import java.util.HashMap;

/**
 * Created by glx on 2015/11/9.
 */
public class PivotCountList extends HashMap<String, PivotCountList> {
    private String pivot;
    private long count;

    public String getPivot() {
        return pivot;
    }

    public void setPivot(String pivot) {
        this.pivot = pivot;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "PivotCountList{" +
                "pivot='" + pivot + '\'' +
                ", count=" + count +
                '}';
    }
}
