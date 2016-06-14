package hasoffer.core.solr;

/**
 * Created by glx on 2015/11/9.
 */
public class PivotAndValue {
    private String field;
    private Object value;

    public PivotAndValue(String field, Object value) {
        this.field = field;
        this.value = value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PivotAndValue that = (PivotAndValue) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PivotAndValue{" +
                "field='" + field + '\'' +
                ", value=" + value +
                '}';
    }
}
