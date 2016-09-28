package hasoffer.core.persistence.po.urm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created by hs on 2016年09月28日.
 * Time 15:08
 * 用户签到奖励配置表
 */
@Entity
public class UrmSignAwdCfg implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    //累计登录次数
    private Integer count;
    //奖励的coin
    private Integer awardCoin;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getAwardCoin() {
        return awardCoin;
    }

    public void setAwardCoin(Integer awardCoin) {
        this.awardCoin = awardCoin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrmSignAwdCfg that = (UrmSignAwdCfg) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (count != null ? !count.equals(that.count) : that.count != null) return false;
        return !(awardCoin != null ? !awardCoin.equals(that.awardCoin) : that.awardCoin != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (awardCoin != null ? awardCoin.hashCode() : 0);
        return result;
    }
}
