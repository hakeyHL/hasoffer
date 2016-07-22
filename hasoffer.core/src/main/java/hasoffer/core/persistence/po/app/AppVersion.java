package hasoffer.core.persistence.po.app;

import hasoffer.base.enums.AppType;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2015/12/30.
 */
@Entity
public class AppVersion implements Identifiable<Long> {

    private static final IdWorker idWorker = IdWorker.getInstance(AppBanner.class);

    @Id
    @Column(unique = true, nullable = false)
    private Long id = idWorker.nextLong();

    @Enumerated(EnumType.STRING)
    private AppType appType;
    private String version;

    private String url;

    private Date createTime;

    private Date publishTime;

    public AppVersion() {
    }

    public AppVersion(AppType appType, String version,
                      String url, Date createTime, Date publishTime) {
        this.appType = appType;
        this.version = version;
        this.url = url;
        this.createTime = createTime;
        this.publishTime = publishTime;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public AppType getAppType() {
        return appType;
    }

    public void setAppType(AppType appType) {
        this.appType = appType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppVersion that = (AppVersion) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (appType != that.appType) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        return !(publishTime != null ? !publishTime.equals(that.publishTime) : that.publishTime != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (appType != null ? appType.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (publishTime != null ? publishTime.hashCode() : 0);
        return result;
    }
}
