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

    private String linkUrl;
    private int discount;

    private long dealClickCount;

    public DealModel(Long id, Website website, String title, Long dealCategoryId, String linkUrl, int discount, long dealClickCount) {
        this.id = id;
        this.website = website;
        this.title = title;
        this.dealCategoryId = dealCategoryId;
        this.linkUrl = linkUrl;
        this.discount = discount;
        this.dealClickCount = dealClickCount;
    }

    public DealModel(AppDeal ad) {
        this(ad.getId(), ad.getWebsite(), ad.getTitle(), ad.getDealCategoryId(), ad.getLinkUrl(), ad.getDiscount(), ad.getDealClickCount());
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

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public long getDealClickCount() {
        return dealClickCount;
    }

    public void setDealClickCount(long dealClickCount) {
        this.dealClickCount = dealClickCount;
    }
}
