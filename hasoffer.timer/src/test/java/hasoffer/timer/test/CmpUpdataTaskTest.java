package hasoffer.timer.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IFetchService;
import hasoffer.core.system.ITimerService;
import hasoffer.fetch.exception.amazon.AmazonRobotCheckException;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.timer.product.worker.CmpSkuListWorker;
import hasoffer.timer.product.worker.CmpSkuUpdateWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 2016/3/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class CmpUpdataTaskTest {

    private static Logger logger = LoggerFactory.getLogger(CmpUpdataTaskTest.class);

    private static final String Q_PTM_CMPSKU = "SELECT t FROM PtmCmpSku t ORDER BY t.id ";
    private static final String Q_PTM_CMPSKUTEST = "SELECT t FROM PtmCmpSku t WHERE website = 'AMAZON' ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    ITimerService timerService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IFetchService fetchService;

    @Test
    public void testCmpUpdateTask() {

        SysTimerTaskLog log = timerService.createTaskLog("CmpUpdateTask");

        logger.debug("------------------------------------CmpUpdateTask-START------------------------------------");
        final ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        final AtomicBoolean listTaskFinished = new AtomicBoolean(false);

        Runnable listTask = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    r();
                }//                listTaskFinished.set(true);
            }

            private void r() {
                int pageNum = 1, PAGE_SIZE = 500;
                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_PTM_CMPSKUTEST, pageNum, PAGE_SIZE);

                int pageCount = (int) pageableResult.getTotalPage();

                List<PtmCmpSku> cmpSkus = pageableResult.getData();
                while (pageNum <= pageCount) {

                    if (skuQueue.size() > 600) {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                            continue;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    logger.info(String.format("update sku : %d/%d .", pageNum, pageCount));

                    if (pageNum > 1) {
                        cmpSkus = dbm.query(Q_PTM_CMPSKUTEST, pageNum, PAGE_SIZE);
                    }

                    skuQueue.addAll(cmpSkus);
                    pageNum++;
                }
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(listTask);

        for (int i = 0; i < 10; i++) {
            es.execute(new CmpSkuUpdateWorker(skuQueue, cmpSkuService, fetchService));
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
                if (listTaskFinished.get() && skuQueue.size() == 0) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        es.shutdown();

        logger.debug("------------------------------------CmpUpdateTask-END------------------------------------");
        timerService.updateTaskLog(log.getId(), "");

    }

    @Test
    public void testCmpUpdate() {

        logger.debug("------------------------------------CmpUpdateTask-START------------------------------------");
        final ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        final AtomicBoolean listTaskFinished = new AtomicBoolean(false);

        Runnable listTask = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    r();
                }//                listTaskFinished.set(true);
            }

            private void r() {
                int pageNum = 1, PAGE_SIZE = 500;
                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_PTM_CMPSKUTEST, pageNum, PAGE_SIZE);

                int pageCount = (int) pageableResult.getTotalPage();

                List<PtmCmpSku> cmpSkus = pageableResult.getData();
                while (pageNum <= pageCount) {

                    if (skuQueue.size() > 600) {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                            continue;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    logger.info(String.format("update sku : %d/%d .", pageNum, pageCount));

                    if (pageNum > 1) {
                        cmpSkus = dbm.query(Q_PTM_CMPSKUTEST, pageNum, PAGE_SIZE);
                    }

                    skuQueue.addAll(cmpSkus);
                    pageNum++;
                }
            }
        };

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(listTask);

        for (int i = 0; i < 10; i++) {
            es.execute(new CmpSkuUpdateWorker(skuQueue, cmpSkuService, fetchService));
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
                if (listTaskFinished.get() && skuQueue.size() == 0) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        es.shutdown();

        logger.debug("------------------------------------CmpUpdateTask-END------------------------------------");
    }

    @Test
    public void testUpdateSku() {

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        CmpSkuListWorker cmpSkuListWorker = new CmpSkuListWorker(dbm, cmpSkuService, skuQueue);

        es.execute(cmpSkuListWorker);
        for (int i = 0; i < 10; i++) {
            es.execute(new CmpSkuUpdateWorker(skuQueue, cmpSkuService, fetchService));
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
                if (skuQueue.size() == 0) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    @Test
    public void testSingleUrlUpdate() {

        long id = 806752;
        String url = "http://www.amazon.in/dp/B015U2DAUY";

        url = URLDecoder.decode(url);

        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            logger.debug(url + " parse website get null");
        }

        OriFetchedProduct oriFetchedProduct = null;
        try {
            oriFetchedProduct = fetchService.fetchSummaryProductByUrl(url);
        } catch (Exception e) {

            String message = e.getMessage();
            if (message != null) {
                if (message.contains("302") || message.contains("404")) {
                    oriFetchedProduct = new OriFetchedProduct();
                    oriFetchedProduct.setTitle("url expire");
                    oriFetchedProduct.setProductStatus(ProductStatus.OFFSALE);
                    oriFetchedProduct.setWebsite(website);
                    oriFetchedProduct.setUrl(url);
                } else {
                    logger.error(e.toString());
                }
            } else {
                logger.error(e.toString());
            }

            if(Website.AMAZON.equals(website)){
                if(e instanceof NullPointerException||e instanceof AmazonRobotCheckException){
                   System.out.println(website);
                }
            }

        }

        cmpSkuService.updateCmpSkuByOriFetchedProduct(id, oriFetchedProduct);
    }
}
