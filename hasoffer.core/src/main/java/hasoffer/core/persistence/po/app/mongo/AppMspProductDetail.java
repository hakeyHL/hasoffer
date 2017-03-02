package hasoffer.core.persistence.po.app.mongo;

import hasoffer.core.persistence.po.ptm.PtmStdSkuParamGroup;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by hs on 2017年03月02日.
 * Time 14:48
 */
@Document(collection = "AppMspProductDetail")
public class AppMspProductDetail {
    private Long id;
    private String description;
    private List<PtmStdSkuParamGroup> paramGroups; // parameters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PtmStdSkuParamGroup> getParamGroups() {
        return paramGroups;
    }

    public void setParamGroups(List<PtmStdSkuParamGroup> paramGroups) {
        this.paramGroups = paramGroups;
    }
}
