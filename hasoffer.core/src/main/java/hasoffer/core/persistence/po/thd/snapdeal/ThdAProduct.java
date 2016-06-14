package hasoffer.core.persistence.po.thd.snapdeal;


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
public class ThdAProduct extends ThdProduct {

    public ThdAProduct() {
        this.website = Website.SNAPDEAL;
    }

    public ThdAProduct(long ptmCateId, String url, String sourceId,String imgUrl, String name, float price) {
        this();
        this.ptmCateId = ptmCateId;
        this.url = url;
        this.imageUrl = imgUrl;
        this.title = name;
        this.price = price;
        this.sourceId = sourceId;
    }

}
