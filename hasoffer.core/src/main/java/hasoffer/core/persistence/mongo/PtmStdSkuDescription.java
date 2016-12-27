package hasoffer.core.persistence.mongo;

import hasoffer.spider.model.FetchedProductReview;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created on 2016/12/27.
 */
@Document(collection = "PtmStdSkuDescription")
public class PtmStdSkuDescription {

    @Id
    private long id;//ptmStdSku id

    private String features;

    private String summary;

    private List<FetchedProductReview> fetchedProductReviewList;

    @PersistenceConstructor
    public PtmStdSkuDescription() {
    }

    public List<FetchedProductReview> getFetchedProductReviewList() {
        return fetchedProductReviewList;
    }

    public void setFetchedProductReviewList(List<FetchedProductReview> fetchedProductReviewList) {
        this.fetchedProductReviewList = fetchedProductReviewList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmStdSkuDescription that = (PtmStdSkuDescription) o;

        if (id != that.id) return false;
        if (features != null ? !features.equals(that.features) : that.features != null) return false;
        if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;
        return !(fetchedProductReviewList != null ? !fetchedProductReviewList.equals(that.fetchedProductReviewList) : that.fetchedProductReviewList != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (features != null ? features.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (fetchedProductReviewList != null ? fetchedProductReviewList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PtmStdSkuDescription{" +
                "features='" + features + '\'' +
                ", id=" + id +
                ", summary='" + summary + '\'' +
                ", fetchedProductReviewList=" + fetchedProductReviewList +
                '}';
    }
}
