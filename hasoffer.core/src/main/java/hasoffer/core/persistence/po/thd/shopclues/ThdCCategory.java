package hasoffer.core.persistence.po.thd.shopclues;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.thd.ThdCategory;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ThdCCategory extends ThdCategory {

	public ThdCCategory() {
		this.website = Website.SHOPCLUES;
	}

	public ThdCCategory(long parentId, String name, String url, String imageUrl) {
		this();
		this.parentId = parentId;
		this.name = name;
		this.url = url;
		this.imageUrl = imageUrl;
	}
}
