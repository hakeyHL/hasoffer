package hasoffer.core.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created on 2016/6/20.
 */
@Document(collection = "PtmCmpSkuDescription")
public class PtmCmpSkuDescription {

    @Id
    private long id;//cmpsku Id

    private String jsonDescription;

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
