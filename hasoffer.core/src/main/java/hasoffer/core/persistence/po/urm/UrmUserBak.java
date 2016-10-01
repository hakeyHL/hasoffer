package hasoffer.core.persistence.po.urm;

/**
 * Created by hs on 2016/6/17.
 */

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

@Entity
public class UrmUserBak implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
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
    //用户的coin
    @Column(columnDefinition = "bigint default 0")
    private Long signCoin = 0l;
    //当前最大连续签到次数
    @Column(columnDefinition = "int default 0")
    private Integer conSignNum = 0;
    //上次签到时间
    private Long lastSignTime;
    //最高连续签到记录
    @Column(columnDefinition = "int default 0")
    private Long maxConSignNum;

    @Column(columnDefinition = "bigint default 0")
    public Long getMaxConSignNum() {
        return maxConSignNum;
    }

    public void setMaxConSignNum(Long maxConSignNum) {
        this.maxConSignNum = maxConSignNum;
    }

    public Long getSignCoin() {
        return signCoin;
    }

    public void setSignCoin(Long signCoin) {
        this.signCoin = signCoin;
    }

    public Integer getConSignNum() {
        return conSignNum;
    }

    public void setConSignNum(Integer conSignNum) {
        this.conSignNum = conSignNum;
    }

    public Long getLastSignTime() {
        return lastSignTime;
    }

    public void setLastSignTime(Long lastSignTime) {
        this.lastSignTime = lastSignTime;
    }

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


}
