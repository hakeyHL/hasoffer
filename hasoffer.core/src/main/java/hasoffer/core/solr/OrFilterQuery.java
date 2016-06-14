package hasoffer.core.solr;

/**
 * Created by glx on 2015/4/21.
 */
public class OrFilterQuery extends FilterQuery {
    private FilterQuery fqs[];

    public OrFilterQuery(FilterQuery... fqs) {
        super("", "");
        this.fqs = fqs;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        if (fqs != null && fqs.length > 0) {
            for (int i = 0; i < fqs.length; i++) {
                sb.append(fqs[i].toString());
                if (i != fqs.length - 1) {
                    sb.append(" OR ");
                }
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
