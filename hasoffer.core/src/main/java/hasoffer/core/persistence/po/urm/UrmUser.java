package hasoffer.core.persistence.po.urm;

/**
 * Created by hs on 2016/6/17.
 */

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

@Entity
public class UrmUser implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String thirdId;
    private String userToken;
    private String userName;
    private String thirdToken;
    private String thirdPlatform;
    private String avatarPath;
    private Date createTime;
    private String telephone;
    private String gcmToken;
    ////用户的coin
    //@Column(columnDefinition = "bigint default 0")
    //private Long signCoin = 0L;
    ////当前最大连续签到次数
    //@Column(columnDefinition = "int default 0")
    //private Integer conSignNum = 0;
    ////上次签到时间
    //private Long lastSignTime;
    ////最高连续签到记录
    //@Column(columnDefinition = "int default 0")
    //private Integer maxConSignNum;

    //@Column(columnDefinition = "int default 0")
    //public Integer getMaxConSignNum() {
    //    return maxConSignNum;
    //}
    //
    //public void setMaxConSignNum(Integer maxConSignNum) {
    //    this.maxConSignNum = maxConSignNum;
    //}
    //
    //public Long getSignCoin() {
    //    return signCoin;
    //}
    //
    //public void setSignCoin(Long signCoin) {
    //    this.signCoin = signCoin;
    //}
    //
    //public Integer getConSignNum() {
    //    return conSignNum;
    //}
    //
    //public void setConSignNum(Integer conSignNum) {
    //    this.conSignNum = conSignNum;
    //}
    //
    //public Long getLastSignTime() {
    //    return lastSignTime;
    //}
    //
    //public void setLastSignTime(Long lastSignTime) {
    //    this.lastSignTime = lastSignTime;
    //}

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        telephone = telephone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thridId) {
        this.thirdId = thridId;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrmUser urmUser = (UrmUser) o;

        if (id != null ? !id.equals(urmUser.id) : urmUser.id != null) return false;
        if (thirdId != null ? !thirdId.equals(urmUser.thirdId) : urmUser.thirdId != null) return false;
        if (userToken != null ? !userToken.equals(urmUser.userToken) : urmUser.userToken != null) return false;
        if (userName != null ? !userName.equals(urmUser.userName) : urmUser.userName != null) return false;
        if (thirdToken != null ? !thirdToken.equals(urmUser.thirdToken) : urmUser.thirdToken != null) return false;
        if (thirdPlatform != null ? !thirdPlatform.equals(urmUser.thirdPlatform) : urmUser.thirdPlatform != null)
            return false;
        if (avatarPath != null ? !avatarPath.equals(urmUser.avatarPath) : urmUser.avatarPath != null) return false;
        if (createTime != null ? !createTime.equals(urmUser.createTime) : urmUser.createTime != null) return false;
        if (telephone != null ? !telephone.equals(urmUser.telephone) : urmUser.telephone != null) return false;
        return gcmToken != null ? gcmToken.equals(urmUser.gcmToken) : urmUser.gcmToken == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (thirdId != null ? thirdId.hashCode() : 0);
        result = 31 * result + (userToken != null ? userToken.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (thirdToken != null ? thirdToken.hashCode() : 0);
        result = 31 * result + (thirdPlatform != null ? thirdPlatform.hashCode() : 0);
        result = 31 * result + (avatarPath != null ? avatarPath.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (telephone != null ? telephone.hashCode() : 0);
        result = 31 * result + (gcmToken != null ? gcmToken.hashCode() : 0);
        return result;
    }
}
