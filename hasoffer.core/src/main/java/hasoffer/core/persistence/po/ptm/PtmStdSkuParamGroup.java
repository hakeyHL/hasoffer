package hasoffer.core.persistence.po.ptm;

import java.util.List;

/**
 * Created by chevy on 2016/11/9.
 */
public class PtmStdSkuParamGroup {

    private String name;

    private List<PtmStdSkuParamNode> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PtmStdSkuParamNode> getParams() {
        return params;
    }

    public void setParams(List<PtmStdSkuParamNode> params) {
        this.params = params;
    }
}
