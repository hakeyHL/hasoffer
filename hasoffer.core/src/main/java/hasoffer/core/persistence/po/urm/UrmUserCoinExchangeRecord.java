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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


}
