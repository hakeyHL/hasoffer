package hasoffer.core.persistence.po.ptm;

/**
 * Created by chevy on 2016/11/9.
 */
public class PtmStdSkuParamNode {

    private String name;

    private String value;

    public PtmStdSkuParamNode(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
