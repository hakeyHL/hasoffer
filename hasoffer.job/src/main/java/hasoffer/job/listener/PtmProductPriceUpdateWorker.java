package hasoffer.job.listener;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created on 2016/8/16.
 */
public class PtmProductPriceUpdateWorker implements Runnable {

    private IDataBaseManager dbm;

    public PtmProductPriceUpdateWorker(IDataBaseManager dbm) {
        this.dbm = dbm;
    }

    @Override
    public void run() {

        //可以写，记得加索引
        Date t1 = TimeUtils.addDay(TimeUtils.nowDate(), -1);
        Date t2 = TimeUtils.add(t1, TimeUtils.MILLISECONDS_OF_1_MINUTE * 10);


        while (true) {

            List<PtmCmpSku> skuList = dbm.query("SELECT distinct t.productId FROM PtmCmpSku t WHERE t.updateTime > ?0 and t.updateTime < ?1", Arrays.asList(t1, t2));


        }

    }
}
