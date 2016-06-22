package hasoffer.core.persistence.po.app;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/6/17.
 */
@Entity
public class AppDeal implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Website website;//deal来源网站

    private String title;//deal标题
    @Column(unique = true, nullable = false)
    private String linkUrl;//deal跳转地址

    @Column(columnDefinition = "text")
    private String imageUrl;//本地图片服务器地址

    @Column(nullable = false)
    private Date createTime;//deal创建时间
    @Column(nullable = false)
    private Date expireTime;//deal失效时间

    private String description;//deal描述

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppDeal appDeal = (AppDeal) o;

        if (id != null ? !id.equals(appDeal.id) : appDeal.id != null) return false;
        if (website != appDeal.website) return false;
        if (title != null ? !title.equals(appDeal.title) : appDeal.title != null) return false;
        if (linkUrl != null ? !linkUrl.equals(appDeal.linkUrl) : appDeal.linkUrl != null) return false;
        if (imageUrl != null ? !imageUrl.equals(appDeal.imageUrl) : appDeal.imageUrl != null) return false;
        if (createTime != null ? !createTime.equals(appDeal.createTime) : appDeal.createTime != null) return false;
        if (expireTime != null ? !expireTime.equals(appDeal.expireTime) : appDeal.expireTime != null) return false;
        return !(description != null ? !description.equals(appDeal.description) : appDeal.description != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (linkUrl != null ? linkUrl.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (expireTime != null ? expireTime.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
