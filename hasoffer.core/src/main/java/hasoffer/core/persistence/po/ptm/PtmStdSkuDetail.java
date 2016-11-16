package hasoffer.core.persistence.po.ptm;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by chevy on 2016/11/9.
 */
@Document(collection = "PtmStdSkuDetail")
public class PtmStdSkuDetail {

    @Id
    private long id;

    private List<PtmStdSkuParamGroup> paramGroups; // parameters

    private String desc; // description

    public PtmStdSkuDetail() {
    }

    public PtmStdSkuDetail(long stdSkuId, List<PtmStdSkuParamGroup> paramGroups, String desc) {
        this.id = stdSkuId;
        this.paramGroups = paramGroups;
        this.desc = desc;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
