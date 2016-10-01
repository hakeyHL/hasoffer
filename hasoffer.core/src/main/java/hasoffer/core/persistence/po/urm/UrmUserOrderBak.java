package hasoffer.core.persistence.po.urm;

/**
 * Created by hs on 2016/6/17.
 */

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

@Entity
public class UrmUserOrderBak implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderIdStrig;//用来记录用户的订单数据

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderIdStrig() {
        return orderIdStrig;
    }

    public void setOrderIdStrig(String orderIdStrig) {
        this.orderIdStrig = orderIdStrig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrmUserOrderBak that = (UrmUserOrderBak) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(orderIdStrig != null ? !orderIdStrig.equals(that.orderIdStrig) : that.orderIdStrig != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (orderIdStrig != null ? orderIdStrig.hashCode() : 0);
        return result;
    }
}
