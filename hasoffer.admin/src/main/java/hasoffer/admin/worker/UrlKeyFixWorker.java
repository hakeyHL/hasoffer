package hasoffer.admin.worker;

import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.task.worker.impl.ListProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class UrlKeyFixWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(UrlKeyFixWorker.class);
    private ListProcessWorkerStatus<PtmCmpSku> ws;

    public UrlKeyFixWorker(ListProcessWorkerStatus ws) {
        this.ws = ws;
    }

    @Override
    public void run() {
        while (true) {

            PtmCmpSku ptmcmpsku = ws.getSdQueue().poll();

            if (ptmcmpsku == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {

                }
                continue;
            }

            String url = ptmcmpsku.getUrl();
            String urlKey = ptmcmpsku.getUrlKey();

            //1.

        }

    }
}
