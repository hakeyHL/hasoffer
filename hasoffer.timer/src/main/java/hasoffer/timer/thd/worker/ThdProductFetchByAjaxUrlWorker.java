package hasoffer.timer.thd.worker;

import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.helper.WebsiteProcessorFactory;
import hasoffer.fetch.model.ListProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2016/2/25.
 */
public class ThdProductFetchByAjaxUrlWorker implements Runnable {

    public static int aliveThreadCount = 0;
    public static ConcurrentLinkedQueue<HashMap<String, Long>> ajaxUrlAndCateIdQueue;
    public static ConcurrentLinkedQueue<ListProduct> productQueue;
    private static Logger logger = LoggerFactory.getLogger(ThdProductFetchByAjaxUrlWorker.class);

    public ThdProductFetchByAjaxUrlWorker() {
        aliveThreadCount++;
    }

    @Override
    public void run() {

        int scan = 0;
        int succedd = 0;
        int fail = 0;
        long time1 = 0;
        long time2 = 0;

        time1 = System.currentTimeMillis();
        while (true) {

            HashMap<String, Long> ajaxUrlAndCateId = ajaxUrlAndCateIdQueue.poll();
            if (ajaxUrlAndCateId == null) {
                ThdProductFetchByAjaxUrlWorker.aliveThreadCount--;
                break;
            }

            scan++;
            String ajaxUrl = "";
            Long cataId = null;
            //待测
            for (Map.Entry<String, Long> entry : ajaxUrlAndCateId.entrySet()) {
                ajaxUrl = entry.getKey();
                cataId = entry.getValue();
            }

            Website website = WebsiteHelper.getWebSite(ajaxUrl);
            IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(website);

            try {

                List<ListProduct> products = listProcessor.getProductByAjaxUrl(ajaxUrl, cataId);

                succedd += products.size();
                productQueue.addAll(products);
                logger.debug("fetch succedd");
            } catch (HttpFetchException e) {
                if (e.getMessage().contains("400")) {
                    ThdProductFetchByAjaxUrlWorker.aliveThreadCount--;
                    logger.debug("fetch end");
                    break;
                }
                logger.debug("fetch fail");
                ajaxUrlAndCateIdQueue.add(ajaxUrlAndCateId);
            } catch (Exception e) {
                logger.debug("fetch fail");
                ajaxUrlAndCateIdQueue.add(ajaxUrlAndCateId);
            }

        }
        time2 = System.currentTimeMillis();
        logger.debug("scan " + scan * 20 + " product,parse success " + succedd + ",fail " + fail + ",time = " + (time2 - time1) + "ms");
    }
}


