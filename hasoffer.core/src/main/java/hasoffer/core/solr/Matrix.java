package hasoffer.core.solr;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by glx on 2015/11/9.
 */
public class Matrix {
    List<Vector> vectorList = new LinkedList<Vector>();

    private String[] pivots = new String[]{};

    public Matrix(String... pivots) {
        if (pivots != null) {
            this.pivots = pivots;
        }
    }

    public long getValue(PivotAndValue... params) {
        return 0;
    }

    public long setValue(PivotAndValue pav, long value) {
        return 0;
    }

    public Object[] listPivotValues(String pivot) {
        return null;
    }

    public final String[] listPivots() {
        return pivots;
    }
}
