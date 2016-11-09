package hasoffer.core.persistence.po.ptm;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by chevy on 2016/11/9.
 */
@Document(collection = "PtmStdSkuDetail")
public class PtmStdSkuDetail {

    private long stdSkuId; // @PtmStdSku.id

    private List<PtmStdSkuParamGroup> paramGroups; // parameters

    private String desc; // description

    public PtmStdSkuDetail(long stdSkuId, List<PtmStdSkuParamGroup> paramGroups, String desc) {
        this.stdSkuId = stdSkuId;
        this.paramGroups = paramGroups;
        this.desc = desc;
    }

    public long getStdSkuId() {
        return stdSkuId;
    }

    public void setStdSkuId(long stdSkuId) {
        this.stdSkuId = stdSkuId;
    }

    public List<PtmStdSkuParamGroup> getParamGroups() {
        return paramGroups;
    }

    public void setParamGroups(List<PtmStdSkuParamGroup> paramGroups) {
        this.paramGroups = paramGroups;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
