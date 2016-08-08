package hasoffer.admin.worker;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.worker.ListAndProcessWorkerStatus;

/**
 * Created on 2016/5/3.
 */
public class FlipkartSkuCategory2GetSaveWorker implements Runnable {

    private final String Q_CATEGORY_BYNAME = "SELECT t FROM PtmCategory2 t WHERE t.name = ?0 ";
    private ListAndProcessWorkerStatus<PtmCmpSku> ws;
    private IDataBaseManager dbm;


    @Override
    public void run() {


    }

}
