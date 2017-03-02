package hasoffer.core.persistence.po.app.mongo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by hs on 2017年03月02日.
 * Time 14:48
 */
@Document(collection = "AppMspSkuDetail")
public class AppMspSkuDetail {
    private Long id;
    private List<String> offers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getOffers() {
        return offers;
    }

    public void setOffers(List<String> offers) {
        this.offers = offers;
    }
}
