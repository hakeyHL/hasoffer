package hasoffer.core.persistence.po.urm;

import hasoffer.base.model.UrmUserGroup;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created by hs on 2016年11月15日.
 * Time 15:32
 */
@Entity
public class UrmUserRedeemGroup implements Identifiable<Long> {
    //就是用户id
    @Id
    @Column(unique = true, nullable = false)
    private Long id;
    //给用户分组,分类,当前表里的用户兑换礼品卡的比例比正常的高 10coin-1卢比
    //001
    @Enumerated(EnumType.STRING)
    private UrmUserGroup groupName;

    public UrmUserRedeemGroup() {
    }

    public UrmUserRedeemGroup(Long id, UrmUserGroup groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public UrmUserGroup getGroupName() {
        return groupName;
    }

    public void setGroupName(UrmUserGroup groupName) {
        this.groupName = groupName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
