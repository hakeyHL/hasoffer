package hasoffer.core.persistence.po.urm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by hs on 2016年11月15日.
 * Time 16:57
 */
@Entity
public class UrmUserExchangeRecord implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    private Long id;
    private Long userId;//用户id
    private String userAvatar;//用户头像
    private String userName;//用户名
    private String logicCoin;//应使用coin
    private String actCoin;//实际coin
    private String redeemTitle;//兑换礼品名称
    private String redeemNum;//兑换数量
    private String redeemTime;//兑换时间
    private String operatorName;//操作者
    private String note;//备注

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLogicCoin() {
        return logicCoin;
    }

    public void setLogicCoin(String logicCoin) {
        this.logicCoin = logicCoin;
    }

    public String getActCoin() {
        return actCoin;
    }

    public void setActCoin(String actCoin) {
        this.actCoin = actCoin;
    }

    public String getRedeemTitle() {
        return redeemTitle;
    }

    public void setRedeemTitle(String redeemTitle) {
        this.redeemTitle = redeemTitle;
    }

    public String getRedeemNum() {
        return redeemNum;
    }

    public void setRedeemNum(String redeemNum) {
        this.redeemNum = redeemNum;
    }

    public String getRedeemTime() {
        return redeemTime;
    }

    public void setRedeemTime(String redeemTime) {
        this.redeemTime = redeemTime;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrmUserExchangeRecord that = (UrmUserExchangeRecord) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (userAvatar != null ? !userAvatar.equals(that.userAvatar) : that.userAvatar != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (logicCoin != null ? !logicCoin.equals(that.logicCoin) : that.logicCoin != null) return false;
        if (actCoin != null ? !actCoin.equals(that.actCoin) : that.actCoin != null) return false;
        if (redeemTitle != null ? !redeemTitle.equals(that.redeemTitle) : that.redeemTitle != null) return false;
        if (redeemNum != null ? !redeemNum.equals(that.redeemNum) : that.redeemNum != null) return false;
        if (redeemTime != null ? !redeemTime.equals(that.redeemTime) : that.redeemTime != null) return false;
        if (operatorName != null ? !operatorName.equals(that.operatorName) : that.operatorName != null) return false;
        return !(note != null ? !note.equals(that.note) : that.note != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (userAvatar != null ? userAvatar.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (logicCoin != null ? logicCoin.hashCode() : 0);
        result = 31 * result + (actCoin != null ? actCoin.hashCode() : 0);
        result = 31 * result + (redeemTitle != null ? redeemTitle.hashCode() : 0);
        result = 31 * result + (redeemNum != null ? redeemNum.hashCode() : 0);
        result = 31 * result + (redeemTime != null ? redeemTime.hashCode() : 0);
        result = 31 * result + (operatorName != null ? operatorName.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        return result;
    }
}
