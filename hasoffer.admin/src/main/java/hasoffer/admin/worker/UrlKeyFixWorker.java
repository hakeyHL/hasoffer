package hasoffer.admin.worker;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.task.worker.impl.ListProcessWorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class UrlKeyFixWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(UrlKeyFixWorker.class);
    private ListProcessWorkerStatus<PtmCmpSku> ws;
    private ICmpSkuService cmpSkuService;

    public UrlKeyFixWorker(ListProcessWorkerStatus ws, ICmpSkuService cmpSkuService) {
        this.ws = ws;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {
        while (true) {

            PtmCmpSku ptmcmpsku = ws.getSdQueue().poll();

            //用来表示是否发生改变
            boolean flag = false;

            if (ptmcmpsku == null) {
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {

                }
                continue;
            }

            long id = ptmcmpsku.getId();
            String url = ptmcmpsku.getUrl();
            if (StringUtils.isEmpty(url)) {
                continue;
            }
            String urlKey = ptmcmpsku.getUrlKey();
            Website website = ptmcmpsku.getWebsite();

            String newUrl = url.trim();
            if (!StringUtils.isEqual(url, newUrl)) {
                flag = true;
            }

            if (Website.SNAPDEAL.equals(website)) {
                if (newUrl.contains("viewAllSellers")) {
                    newUrl = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
                    flag = true;
                }
            }

            if (Website.AMAZON.equals(website)) {
                if (newUrl.contains("gp/offer-listing")) {
                    newUrl = url.replace("gp/offer-listing", "dp");
                    flag = true;
                }
            }

            String newUrlKey = HexDigestUtil.md5(newUrl);
            if (!StringUtils.isEqual(urlKey, newUrlKey)) {
                flag = true;
            }

            if (flag) {
                cmpSkuService.setUrlKey(id, newUrl, newUrlKey);
                System.out.println("update success for " + id);
                System.out.println("newUrl " + newUrl);
                System.out.println("urlKey " + newUrlKey);
            }

        }

    }
}
