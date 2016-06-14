package hasoffer.core.solr;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by glx on 2015/11/9.
 */
public class Vector {
    private Map<String, Object> map = new HashMap<String, Object>();

    public void setPivotValue(String pivot, Object value) {
        map.put(pivot, value);
    }

    public Object setPivotValue(String pivot) {
        return map.get(pivot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        return !(map != null ? !map.equals(vector.map) : vector.map != null);

    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }
}
