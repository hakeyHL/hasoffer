package hasoffer.core.persistence.mongo;

import hasoffer.spider.model.FetchedProductReview;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created on 2016/6/20.
 * updateTime：2016-06-29 14:25
 * id更新为ptmproductId
 * 更新逻辑，更新为ptmcmpsku中website为flipkart的关联的ptmproduct的id
 * id更换为PtmCmpSkuId
 */
@Document(collection = "PtmCmpSkuDescription")
public class PtmCmpSkuDescription {

    @Id
    private long id;//cmpsku Id

    private String jsonParam;//参数

    private String jsonDescription;

    private String offers;

    private List<FetchedProductReview> fetchedProductReviewList;

    @PersistenceConstructor
    public PtmCmpSkuDescription() {
    }

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

    public String getJsonDescription() {
        return jsonDescription;
    }

    public void setJsonDescription(String jsonDescription) {
        this.jsonDescription = jsonDescription;
    }

    public String getOffers() {
        return offers;
    }

    public void setOffers(String offers) {
        this.offers = offers;
    }

    public List<FetchedProductReview> getFetchedProductReviewList() {
        return fetchedProductReviewList;
    }

    public void setFetchedProductReviewList(List<FetchedProductReview> fetchedProductReviewList) {
        this.fetchedProductReviewList = fetchedProductReviewList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmCmpSkuDescription that = (PtmCmpSkuDescription) o;

        if (id != that.id) return false;
        if (jsonParam != null ? !jsonParam.equals(that.jsonParam) : that.jsonParam != null) return false;
        if (jsonDescription != null ? !jsonDescription.equals(that.jsonDescription) : that.jsonDescription != null)
            return false;
        if (offers != null ? !offers.equals(that.offers) : that.offers != null) return false;
        return !(fetchedProductReviewList != null ? !fetchedProductReviewList.equals(that.fetchedProductReviewList) : that.fetchedProductReviewList != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (jsonParam != null ? jsonParam.hashCode() : 0);
        result = 31 * result + (jsonDescription != null ? jsonDescription.hashCode() : 0);
        result = 31 * result + (offers != null ? offers.hashCode() : 0);
        result = 31 * result + (fetchedProductReviewList != null ? fetchedProductReviewList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PtmCmpSkuDescription{" +
                "fetchedProductReviewList=" + fetchedProductReviewList +
                ", id=" + id +
                ", jsonParam='" + jsonParam + '\'' +
                ", jsonDescription='" + jsonDescription + '\'' +
                ", offers='" + offers + '\'' +
                '}';
    }
}
