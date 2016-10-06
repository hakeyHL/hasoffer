package hasoffer.core.persistence.mongo;

import hasoffer.core.persistence.po.urm.UrmUser;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by hs on 2016年09月29日.
 * Time 12:31
 */
@Document(collection = "UserSignLog")
public class UserSignLog {
    @Id
    private String id;

    private Long userId;
    private Long signDate;

    private String signDateStr;

    @PersistenceConstructor
    public UserSignLog() {
    }

    public UserSignLog(UrmUser urmUser) {
        this.userId = urmUser.getId();
        this.signDate = urmUser.getLastSignTime();
        this.signDateStr = DateFormatUtils.format(urmUser.getLastSignTime(), "yyyy-MM-dd hh:mm:ss");
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSignDate() {
        return signDate;
    }

    public void setSignDate(Long signDate) {
        this.signDate = signDate;
    }

    public String getSignDateStr() {
        return signDateStr;
    }

    public void setSignDateStr(String signDateStr) {
        this.signDateStr = signDateStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserSignLog that = (UserSignLog) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        return !(signDate != null ? !signDate.equals(that.signDate) : that.signDate != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (signDate != null ? signDate.hashCode() : 0);
        return result;
    }
}
