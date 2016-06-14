package hasoffer.core.persistence.po.thd.flipkart;


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
public class ThdBProduct extends ThdProduct {

    public ThdBProduct() {
        this.website = Website.FLIPKART;
    }

    public ThdBProduct(long ptmCateId, String url, String imgUrl, String name, float price) {
        this();
        this.ptmCateId = ptmCateId;
        this.url = url;
        this.imageUrl = imgUrl;
        this.title = name;
        this.price = price;
    }

}
