package hasoffer.core.persistence.po.app;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created by hs on 2016年09月26日.
 * Time 12:28
 * 联盟信息配置
 */
@Entity
public class AffiliateConfig implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //配置佣金冻结时长
    private Long FrozenTime;
    private float backRate;
    //website
    private Website website;


    //配置返利比例

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getFrozenTime() {
        return FrozenTime;
    }

    public void setFrozenTime(Long frozenTime) {
        FrozenTime = frozenTime;
    }

    public float getBackRate() {
        return backRate;
    }

    public void setBackRate(float backRate) {
        this.backRate = backRate;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AffiliateConfig that = (AffiliateConfig) o;

        if (Float.compare(that.backRate, backRate) != 0) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (FrozenTime != null ? !FrozenTime.equals(that.FrozenTime) : that.FrozenTime != null) return false;
        return website == that.website;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (FrozenTime != null ? FrozenTime.hashCode() : 0);
        result = 31 * result + (backRate != +0.0f ? Float.floatToIntBits(backRate) : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        return result;
    }
}
