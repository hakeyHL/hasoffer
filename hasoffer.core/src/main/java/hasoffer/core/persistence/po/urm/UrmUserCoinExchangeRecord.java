package hasoffer.core.persistence.po.urm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by hs on 2017年01月03日.
 * Time 15:33
 */
@Entity
public class UrmUserCoinExchangeRecord implements Identifiable<Long> {
    //以用户ID作为主键
    @Id
    @Column(unique = true, nullable = false)
    private Long id;
    private Long userId;
    private Long operateTime;
    private Long coinTotal;
    private Long orderCoin;
    private Long signCoin;
    private float coin2Rupee;
    private String operator;
    private String note;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Long operateTime) {
        this.operateTime = operateTime;
    }

    public Long getCoinTotal() {
        return coinTotal;
    }

    public void setCoinTotal(Long coinTotal) {
        this.coinTotal = coinTotal;
    }

    public Long getOrderCoin() {
        return orderCoin;
    }

    public void setOrderCoin(Long orderCoin) {
        this.orderCoin = orderCoin;
    }

    public Long getSignCoin() {
        return signCoin;
    }

    public void setSignCoin(Long signCoin) {
        this.signCoin = signCoin;
    }

    public float getCoin2Rupee() {
        return coin2Rupee;
    }

    public void setCoin2Rupee(float coin2Rupee) {
        this.coin2Rupee = coin2Rupee;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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

        UrmUserCoinExchangeRecord that = (UrmUserCoinExchangeRecord) o;

        if (Float.compare(that.coin2Rupee, coin2Rupee) != 0) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (operateTime != null ? !operateTime.equals(that.operateTime) : that.operateTime != null) return false;
        if (coinTotal != null ? !coinTotal.equals(that.coinTotal) : that.coinTotal != null) return false;
        if (orderCoin != null ? !orderCoin.equals(that.orderCoin) : that.orderCoin != null) return false;
        if (signCoin != null ? !signCoin.equals(that.signCoin) : that.signCoin != null) return false;
        if (operator != null ? !operator.equals(that.operator) : that.operator != null) return false;
        return note != null ? note.equals(that.note) : that.note == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (operateTime != null ? operateTime.hashCode() : 0);
        result = 31 * result + (coinTotal != null ? coinTotal.hashCode() : 0);
        result = 31 * result + (orderCoin != null ? orderCoin.hashCode() : 0);
        result = 31 * result + (signCoin != null ? signCoin.hashCode() : 0);
        result = 31 * result + (coin2Rupee != +0.0f ? Float.floatToIntBits(coin2Rupee) : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        return result;
    }
}
