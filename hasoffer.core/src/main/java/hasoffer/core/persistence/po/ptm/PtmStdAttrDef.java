package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by chevy on 2016/8/12.
 */
@Entity
public class PtmStdAttrDef implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    private String id;

    private String stdDefName; // 规格名称

    private String unitName; // 单位,计量单位

    private PtmStdAttrDef() {
    }

    public PtmStdAttrDef(String stdDefName) {
        this();
        this.stdDefName = stdDefName;
        this.id = getId(stdDefName);
    }

    public PtmStdAttrDef(String stdDefName, String unitName) {
        this(stdDefName);
        this.unitName = unitName;
    }

    private String getId(String stdName) {
        return stdName.toLowerCase().trim();// 小写，去两端空格
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStdDefName() {
        return stdDefName;
    }

    public void setStdDefName(String stdDefName) {
        this.stdDefName = stdDefName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmStdAttrDef stdDef = (PtmStdAttrDef) o;

        if (id != null ? !id.equals(stdDef.id) : stdDef.id != null) return false;
        if (stdDefName != null ? !stdDefName.equals(stdDef.stdDefName) : stdDef.stdDefName != null) return false;
        return !(unitName != null ? !unitName.equals(stdDef.unitName) : stdDef.unitName != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (stdDefName != null ? stdDefName.hashCode() : 0);
        result = 31 * result + (unitName != null ? unitName.hashCode() : 0);
        return result;
    }
}
