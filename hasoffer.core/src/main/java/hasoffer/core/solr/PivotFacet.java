package hasoffer.core.solr;

/**
 * Created by glx on 2015/11/9.
 */
public class PivotFacet {
    private String field;
    private Object value;
    private long count;

    public PivotFacet(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
