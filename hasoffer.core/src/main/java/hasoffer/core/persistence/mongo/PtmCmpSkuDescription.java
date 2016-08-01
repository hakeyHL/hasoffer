package hasoffer.core.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created on 2016/6/20.
 * updateTime：2016-06-29 14:25
 * id更新为ptmproductId
 * 更新逻辑，更新为ptmcmpsku中website为flipkart的关联的ptmproduct的id
 * id更换为PtmCmpSkuId,将初始数据迁移到PtmProductDescription
 */
@Document(collection = "PtmCmpSkuDescription")
public class PtmCmpSkuDescription {

    @Id
    private long id;//cmpsku Id

    private String jsonParam;//参数

    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJsonParam() {
        return jsonParam;
    }

    public void setJsonParam(String jsonParam) {
        this.jsonParam = jsonParam;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmCmpSkuDescription that = (PtmCmpSkuDescription) o;

        if (id != that.id) return false;
        if (jsonParam != null ? !jsonParam.equals(that.jsonParam) : that.jsonParam != null) return false;
        return !(description != null ? !description.equals(that.description) : that.description != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (jsonParam != null ? jsonParam.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
