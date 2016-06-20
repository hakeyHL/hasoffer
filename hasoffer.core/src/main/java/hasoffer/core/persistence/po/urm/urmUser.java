package hasoffer.core.persistence.po.urm;

/**
 * Created by hs on 2016/6/17.
 */

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class urmUser {
    @Id
    @Column(unique = true, nullable = false)
    private  String id;

    private  String userToken;
    private  String userName;
    private  String thirdToken;
    private  String thirdPlatform;
    private  String avatarPath;
    private Date createTime;

    @Override
         public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userToken !=null?userToken.hashCode() : 0);
        result = 31 * result + (userName !=null?userName.hashCode(): 0);
        result = 31 * result + (thirdToken !=null?thirdToken.hashCode() : 0);
        result = 31 * result + (thirdPlatform !=null?thirdPlatform.hashCode() : 0);
        result = 31 * result + (avatarPath !=null?avatarPath.hashCode() : 0);
        result = 31 * result + (createTime !=null?createTime.hashCode(): 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getThirdToken() {
        return thirdToken;
    }

    public void setThirdToken(String thirdToken) {
        this.thirdToken = thirdToken;
    }

    public String getThirdPlatform() {
        return thirdPlatform;
    }

    public void setThirdPlatform(String thirdPlatform) {
        this.thirdPlatform = thirdPlatform;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
