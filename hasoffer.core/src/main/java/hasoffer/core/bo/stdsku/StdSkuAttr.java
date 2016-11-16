package hasoffer.core.bo.stdsku;

import hasoffer.core.persistence.po.ptm.PtmStdSkuAttr;

/**
 * Created by chevy on 2016/11/16.
 */
public class StdSkuAttr {

    private Long id;

    private String stdDefId; // ptm_std_def id

    private String stdName;

    private String stdValue;

    public StdSkuAttr(PtmStdSkuAttr stdSkuAttr) {
        this.id = stdSkuAttr.getId();
        this.stdDefId = stdSkuAttr.getStdDefId();
        this.stdName = stdSkuAttr.getStdName();
        this.stdValue = stdSkuAttr.getStdValue();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStdDefId() {
        return stdDefId;
    }

    public void setStdDefId(String stdDefId) {
        this.stdDefId = stdDefId;
    }

    public String getStdName() {
        return stdName;
    }

    public void setStdName(String stdName) {
        this.stdName = stdName;
    }

    public String getStdValue() {
        return stdValue;
    }

    public void setStdValue(String stdValue) {
        this.stdValue = stdValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StdSkuAttr that = (StdSkuAttr) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (stdDefId != null ? !stdDefId.equals(that.stdDefId) : that.stdDefId != null) return false;
        if (stdName != null ? !stdName.equals(that.stdName) : that.stdName != null) return false;
        return !(stdValue != null ? !stdValue.equals(that.stdValue) : that.stdValue != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (stdDefId != null ? stdDefId.hashCode() : 0);
        result = 31 * result + (stdName != null ? stdName.hashCode() : 0);
        result = 31 * result + (stdValue != null ? stdValue.hashCode() : 0);
        return result;
    }
}
