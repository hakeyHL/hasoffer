package hasoffer.core.persistence.po.urm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class UrmSignCoin {

    //以用户ID作为主键
    @Id
    @Column(unique = true, nullable = false)
    private Long userId;

    //用户的coin
    @Column(columnDefinition = "bigint default 0")
    private Long signCoin = 0L;

    //当前最大连续签到次数
    @Column(columnDefinition = "int default 0")
    private Integer conSignNum = 0;

    //最近签到时间
    private Long lastSignTime = 0L;

    //最近签到时间（北京）
    private Date signZhTime;

    //最近签到时间（印度）
    private Date signIndTime;

    //最高连续签到记录
    @Column(columnDefinition = "int default 0")
    private Integer maxConSignNum;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
        if (lastSignTime == null) {
            return 0L;
        }
        return lastSignTime;
    }

    public void setLastSignTime(Long lastSignTime) {
        this.lastSignTime = lastSignTime;
    }

    public Date getSignZhTime() {
        return signZhTime;
    }

    public void setSignZhTime(Date signZhTime) {
        this.signZhTime = signZhTime;
    }

    public Date getSignIndTime() {
        return signIndTime;
    }

    public void setSignIndTime(Date signIndTime) {
        this.signIndTime = signIndTime;
    }

    @Column(columnDefinition = "int default 0")
    public Integer getMaxConSignNum() {
        return maxConSignNum;
    }

    public void setMaxConSignNum(Integer maxConSignNum) {
        this.maxConSignNum = maxConSignNum;
    }

    @Override
    public String toString() {
        return "UrmSignCoin{" +
                "userId=" + userId +
                ", signCoin=" + signCoin +
                ", conSignNum=" + conSignNum +
                ", lastSignTime=" + lastSignTime +
                ", signIndTime=" + signIndTime +
                ", maxConSignNum=" + maxConSignNum +
                '}';
    }
}
