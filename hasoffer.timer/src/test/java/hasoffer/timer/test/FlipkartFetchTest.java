package hasoffer.timer.test;


import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.thd.ThdFetchTask;
import hasoffer.core.thd.IThdService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.timer.thd.worker.SaveThdProductWorker;
import hasoffer.timer.thd.worker.ThdProductFetchByAjaxUrlWorker;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Date:2016/1/14 2016/1/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class FlipkartFetchTest {

    private static Logger logger = LoggerFactory.getLogger(FlipkartFetchTest.class);

    @Resource
    IThdService thdService;
    @Resource
    IDataBaseManager dbm;


    @Test
    public void testFetchFlipkartProductByAjaxUrl() throws XPatherException {

        ConcurrentLinkedQueue<HashMap<String, Long>> ajaxUrlAndCateIdQueue = new ConcurrentLinkedQueue<HashMap<String, Long>>();
        ConcurrentLinkedQueue<ListProduct> productQueue = new ConcurrentLinkedQueue<ListProduct>();

        ThdFetchTask thdFetchTask = dbm.get(ThdFetchTask.class, 131073L);

        String urlTemplate = thdFetchTask.getUrlTemplate();
        int start = thdFetchTask.getStart();
        int size = thdFetchTask.getSize();
        long ptmCateId = thdFetchTask.getPtmCateId();
        int pageNum = (size + 19) / 20;

        for (int i = 0; i < pageNum; i++) {
            if (i == 0) {
                start = 0;
            } else {
                start = i * 20 + 1;
            }

            String[] subStrs1 = urlTemplate.split("start=");
            String ajaxUrl = subStrs1[0] + "start=" + start;

            HashMap<String, Long> map = new HashMap<String, Long>();
            map.put(ajaxUrl, ptmCateId);
            ajaxUrlAndCateIdQueue.add(map);
        }

        ExecutorService es = Executors.newCachedThreadPool();

        ThdProductFetchByAjaxUrlWorker.ajaxUrlAndCateIdQueue = ajaxUrlAndCateIdQueue;
        ThdProductFetchByAjaxUrlWorker.productQueue = productQueue;

        es.execute(new ThdProductFetchByAjaxUrlWorker());
// 		es.execute(new FlipkartProductFetchByAjaxUrlWorker());

        es.execute(new SaveThdProductWorker(thdService));

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(10);
                if (ThdProductFetchByAjaxUrlWorker.aliveThreadCount == 0 && SaveThdProductWorker.aliveThreadCount == 0) {

                    logger.debug("--flipkartFetchTest--end");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        es.shutdown();
    }
}
