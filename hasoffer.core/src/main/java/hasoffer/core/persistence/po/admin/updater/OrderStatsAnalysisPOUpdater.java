package hasoffer.core.persistence.po.admin.updater;

import hasoffer.core.persistence.dbm.osql.Updater;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;

/**
 * Created on 2016/10/1.
 */
public class OrderStatsAnalysisPOUpdater extends Updater<Integer, OrderStatsAnalysisPO> {
    public OrderStatsAnalysisPOUpdater(Integer integer) {
        super(OrderStatsAnalysisPO.class, integer);
    }
}
