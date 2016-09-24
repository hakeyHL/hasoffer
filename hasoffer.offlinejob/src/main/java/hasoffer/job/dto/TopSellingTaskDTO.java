package hasoffer.job.dto;

import hasoffer.base.model.Website;

import java.util.Date;

public class TopSellingTaskDTO {
    private Long id;
    private Long productId;
    private Website website;
    private String url;
    private Date updateTime;

    public TopSellingTaskDTO(Long id, Long productId, Website website, String url, Date updateTime) {
        this.id = id;
        this.productId = productId;
        this.website = website;
        this.url = url;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return id + "\t" + productId + "\t" + website + "\t" + url + "\t" + updateTime;
    }


}
