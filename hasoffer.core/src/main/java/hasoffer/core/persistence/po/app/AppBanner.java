package hasoffer.core.persistence.po.app;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.enums.BannerFrom;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/6/20.
 */
@Entity
public class AppBanner implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;//图片路径
    private String linkUrl;//跳转路径

    private Date deadline;//最大的有效期 默认值：创建时间+7天

    private BannerFrom from;//banner来源

    private int rank;//用于手工调整该条banner的优先级  此处感觉设计不合理，还没想到比较好的办法

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public BannerFrom getFrom() {
        return from;
    }

    public void setFrom(BannerFrom from) {
        this.from = from;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppBanner appBanner = (AppBanner) o;

        if (rank != appBanner.rank) return false;
        if (id != null ? !id.equals(appBanner.id) : appBanner.id != null) return false;
        if (imageUrl != null ? !imageUrl.equals(appBanner.imageUrl) : appBanner.imageUrl != null) return false;
        if (linkUrl != null ? !linkUrl.equals(appBanner.linkUrl) : appBanner.linkUrl != null) return false;
        if (deadline != null ? !deadline.equals(appBanner.deadline) : appBanner.deadline != null) return false;
        return from == appBanner.from;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (linkUrl != null ? linkUrl.hashCode() : 0);
        result = 31 * result + (deadline != null ? deadline.hashCode() : 0);
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + rank;
        return result;
    }
}
