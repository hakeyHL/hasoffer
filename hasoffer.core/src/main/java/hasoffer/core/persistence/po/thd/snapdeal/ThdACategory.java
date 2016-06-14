package hasoffer.core.persistence.po.thd.snapdeal;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.thd.ThdCategory;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Created on 2015/12/7.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ThdACategory extends ThdCategory {

    public ThdACategory() {
        this.website = Website.SNAPDEAL;
    }

    public ThdACategory(long parentId, String name, String url, String imageUrl) {
        this();
        this.parentId = parentId;
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }
}
