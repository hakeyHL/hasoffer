package hasoffer.core.persistence.po.thd.msp;


import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.thd.ThdProduct;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Created on 2015/12/7.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ThdMspProduct extends ThdProduct{

    private String offerUrl;//Msp的url

    public ThdMspProduct() {
        this.website = Website.MYSMARTPRICE;
    }

    public ThdMspProduct(long categoryId, String sourceId, String offerUrl,
                         String url, String imageUrl, String title,
                         Website website, float price) {
        this();
        this.ptmCateId = categoryId;
        this.sourceId = sourceId;
        this.offerUrl = offerUrl;
        this.url = url;
        this.imageUrl = imageUrl;
        this.title = title;
        this.website = website;
        this.price = price;
    }

    public String getOfferUrl() {
        return offerUrl;
    }

    public void setOfferUrl(String offerUrl) {
        this.offerUrl = offerUrl;
    }
}
