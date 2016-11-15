package hasoffer.core.persistence.po.admin;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

@Entity
public class UrmAffAccount implements Identifiable<Integer> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String nickName;

    @Column(length = 200)
    private String loginName;

    @Column(length = 200)
    private String loginPwd;

    @Column(length = 200)
    private String trackingId;

    @Column(length = 200)
    private String token;

    @Enumerated(value = EnumType.STRING)
    private Website webSite;

    @Column(length = 1)
    private String validState;

    public Integer getId() {
        return id;
    }


    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPwd() {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Website getWebSite() {
        return webSite;
    }

    public void setWebSite(Website webSite) {
        this.webSite = webSite;
    }

    public String getValidState() {
        return validState;
    }

    public void setValidState(String validState) {
        this.validState = validState;
    }

    @Override
    public String toString() {
        return "UrmAffAccount{" +
                "id=" + id +
                ", nickName='" + nickName + '\'' +
                ", loginName='" + loginName + '\'' +
                ", loginPwd='" + loginPwd + '\'' +
                ", trackingId='" + trackingId + '\'' +
                ", token='" + token + '\'' +
                ", webSite=" + webSite +
                ", validState='" + validState + '\'' +
                '}';
    }
}