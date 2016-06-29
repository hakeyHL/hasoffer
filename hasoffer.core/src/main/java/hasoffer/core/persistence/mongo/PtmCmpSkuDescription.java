package hasoffer.core.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created on 2016/6/20.
 * updateTime：2016-06-29 14:25
 * id更新为ptmproductId
 * 更新逻辑，更新为ptmcmpsku中website为flipkart的关联的ptmproduct的id
 */
@Document(collection = "PtmCmpSkuDescription")
public class PtmCmpSkuDescription {

    @Id
    private long id;//cmpsku Id

    private String jsonDescription;//描述

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJsonDescription() {
        return jsonDescription;
    }

    public void setJsonDescription(String jsonDescription) {
        this.jsonDescription = jsonDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmCmpSkuDescription that = (PtmCmpSkuDescription) o;

        if (id != that.id) return false;
        return !(jsonDescription != null ? !jsonDescription.equals(that.jsonDescription) : that.jsonDescription != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (jsonDescription != null ? jsonDescription.hashCode() : 0);
        return result;
    }
}
