package hasoffer.core.persistence.po.thd.shopclues;


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
public class ThdCProduct extends ThdProduct {

    public ThdCProduct() {
        this.website = Website.SHOPCLUES;
    }

    public ThdCProduct(long categoryId, String url, String imgUrl, String name, float price) {
        this();
        this.ptmCateId = categoryId;
        this.url = url;
        this.imageUrl = imgUrl;
        this.title = name;
        this.price = price;
    }

}
