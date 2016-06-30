package hasoffer.admin.worker;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.updater.PtmProductUpdater;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2016/6/30.
 */
public class FixPtmProductCategoryUpdateWorker implements Runnable {

    private ConcurrentLinkedQueue<PtmCmpSku> quene;
    private IDataBaseManager dbm;

    public  FixPtmProductCategoryUpdateWorker(ConcurrentLinkedQueue<PtmCmpSku> quene,IDataBaseManager dbm){
        this.quene = quene;
        this.dbm = dbm;
    }


    @Override
    public void run() {
        while (true) {

            PtmCmpSku skuQ = quene.poll();

            if (skuQ == null) {
                continue;
            }

            PtmProductUpdater updater = new PtmProductUpdater(skuQ.getProductId());
            updater.getPo().setCategoryId(skuQ.getCategoryId());
            dbm.update(updater);
        }
    }
}
