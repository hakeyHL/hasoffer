package hasoffer.core.persistence.mongo;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.fetch.helper.WebsiteHelper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created on 2016/1/4.
 */
@Document(collection = "PtmCmpSkuIndex")
public class PtmCmpSkuIndex {

    @Id
    private long id;

    private long productId;

    private Website website;
    private String sourcePid;
    private String sourceSid;

    private String title;
    private String skuTitle;

    private String skuTitleIndex;
    private String siteSkuTitleIndex;
    private String skuUrlIndex;

    private float price;

    private String url;

    @PersistenceConstructor
    public PtmCmpSkuIndex() {
    }

    public PtmCmpSkuIndex(long id, long productId, Website website, String sourcePid, String sourceSid,
                          String title, String skuTitle, float price, String url) {
        this.id = id;
        this.productId = productId;
        this.website = website;
        this.sourcePid = sourcePid;
        this.sourceSid = sourceSid;
        this.title = title;
        this.skuTitle = skuTitle;
        if (StringUtils.isEmpty(skuTitle)) {
            this.skuTitle = title;
        }
        this.skuTitleIndex = HexDigestUtil.md5(StringUtils.getCleanChars(this.skuTitle));
        this.siteSkuTitleIndex = HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(this.skuTitle));
        this.price = price;
        this.url = url;
        this.skuUrlIndex = HexDigestUtil.md5(WebsiteHelper.getCleanUrl(website, url));
    }

    public PtmCmpSkuIndex(PtmCmpSku cmpSku) {
        this(cmpSku.getId(), cmpSku.getProductId(), cmpSku.getWebsite(), cmpSku.getSourcePid(), cmpSku.getSourceSid(),
                cmpSku.getTitle(), cmpSku.getSkuTitle(), cmpSku.getPrice(), cmpSku.getUrl());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSkuTitle() {
        return skuTitle;
    }

    public void setSkuTitle(String skuTitle) {
        this.skuTitle = skuTitle;
    }

    public String getSkuTitleIndex() {
        return skuTitleIndex;
    }

    public void setSkuTitleIndex(String skuTitleIndex) {
        this.skuTitleIndex = skuTitleIndex;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getSourcePid() {
        return sourcePid;
    }

    public void setSourcePid(String sourcePid) {
        this.sourcePid = sourcePid;
    }

    public String getSourceSid() {
        return sourceSid;
    }

    public void setSourceSid(String sourceSid) {
        this.sourceSid = sourceSid;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getSkuUrlIndex() {
        return skuUrlIndex;
    }

    public void setSkuUrlIndex(String skuUrlIndex) {
        this.skuUrlIndex = skuUrlIndex;
    }

    public String getSiteSkuTitleIndex() {
        return siteSkuTitleIndex;
    }

    public void setSiteSkuTitleIndex(String siteSkuTitleIndex) {
        this.siteSkuTitleIndex = siteSkuTitleIndex;
    }

    @Override
    public String toString() {
        return "PtmCmpSkuIndex{" +
                "id=" + id +
                ", productId=" + productId +
                ", website=" + website +
                ", sourcePid='" + sourcePid + '\'' +
                ", sourceSid='" + sourceSid + '\'' +
                ", title='" + title + '\'' +
                ", skuTitle='" + skuTitle + '\'' +
                ", skuTitleIndex='" + skuTitleIndex + '\'' +
                ", skuUrlIndex='" + skuUrlIndex + '\'' +
                ", price=" + price +
                ", url='" + url + '\'' +
                '}';
    }
}
