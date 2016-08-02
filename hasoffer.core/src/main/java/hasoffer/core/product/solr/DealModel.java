package hasoffer.core.product.solr;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.data.solr.IIdentifiable;

/**
 * Created on 2016/6/17.
 */
public class DealModel implements IIdentifiable<Long> {
    private Long id;
    private Website website;//deal来源网站

    private String title;//deal标题

    private Long dealCategoryId = -1l;

    public DealModel(Long id, Website website, String title, Long dealCategoryId) {
        this.id = id;
        this.website = website;
        this.title = title;
        this.dealCategoryId = dealCategoryId;
    }

    public DealModel(AppDeal ad) {
        this(ad.getId(), ad.getWebsite(), ad.getTitle(), ad.getDealCategoryId());
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getDealCategoryId() {
        return dealCategoryId;
    }

    public void setDealCategoryId(Long dealCategoryId) {
        this.dealCategoryId = dealCategoryId;
    }
}
