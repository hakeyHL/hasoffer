package hasoffer.core.product.solr;

import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.data.solr.IIdentifiable;

/**
 * Created by hs on 2016年12月01日.
 * Time 09:58
 */
public class PtmStdPriceModel implements IIdentifiable<Long> {
    private Long id;
    private long stdSkuId;
    private String title;
    private String site;
    private String skuUrl;
    private String skuStatus;

    public PtmStdPriceModel() {
    }

    public PtmStdPriceModel(PtmStdPrice ptmStdPrice) {
        this.id = ptmStdPrice.getId();
        this.stdSkuId = ptmStdPrice.getStdSkuId();
        this.title = ptmStdPrice.getTitle();
        this.site = ptmStdPrice.getWebsite().name();
        this.skuUrl = ptmStdPrice.getUrl();
        this.skuStatus = ptmStdPrice.getSkuStatus().name();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStdSkuId() {
        return stdSkuId;
    }

    public void setStdSkuId(long stdSkuId) {
        this.stdSkuId = stdSkuId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getSkuUrl() {
        return skuUrl;
    }

    public void setSkuUrl(String skuUrl) {
        this.skuUrl = skuUrl;
    }

    public String getSkuStatus() {
        return skuStatus;
    }

    public void setSkuStatus(String skuStatus) {
        this.skuStatus = skuStatus;
    }
}
