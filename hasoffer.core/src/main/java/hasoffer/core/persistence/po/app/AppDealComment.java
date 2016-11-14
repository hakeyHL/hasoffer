package hasoffer.core.persistence.po.app;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created by hs on 2016年11月14日.
 * Time 11:51
 */
@Entity
public class AppDealComment implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long createTime;//评论时间
    private Long userId;//用户id
    private String content;//评论内容
    private int isAnonymous = 0; //是否匿名 0不匿名 1  匿名
    private Long dealId;

    public AppDealComment() {
    }

    public AppDealComment(Long createTime, Long userId, String content, int isAnonymous, Long dealId) {
        this.createTime = createTime;
        this.userId = userId;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.dealId = dealId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(int isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Long getDealId() {
        return dealId;
    }

    public void setDealId(Long dealId) {
        this.dealId = dealId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppDealComment that = (AppDealComment) o;

        if (isAnonymous != that.isAnonymous) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        return !(dealId != null ? !dealId.equals(that.dealId) : that.dealId != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + isAnonymous;
        result = 31 * result + (dealId != null ? dealId.hashCode() : 0);
        return result;
    }
}
