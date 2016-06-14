package hasoffer.timer.thd.worker;

import hasoffer.core.persistence.po.thd.ThdProduct;
import hasoffer.core.thd.IThdService;
import hasoffer.core.thd.ThdHelper;
import hasoffer.fetch.model.ListProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/2/25.
 */
public class SaveThdProductWorker implements Runnable {

    public static int aliveThreadCount = 0;
    private static Logger logger = LoggerFactory.getLogger(SaveThdProductWorker.class);
    public IThdService thdService;

    public SaveThdProductWorker(IThdService thdService) {
        this.thdService = thdService;
        aliveThreadCount++;
    }

    @Override
    public void run() {

        int scan  = 0;
        int succedd = 0;
        int duplicate = 0;
        long time1 =0;
        long time2 =0;

        time1 = System.currentTimeMillis();
        while (true) {
            //如果获取的数据为空切活着的抓取线程数字为0，break
            ListProduct product = ThdProductFetchByAjaxUrlWorker.productQueue.poll();
            if (product == null && ThdProductFetchByAjaxUrlWorker.aliveThreadCount == 0) {
                SaveThdProductWorker.aliveThreadCount--;
                break;
            }
            if (product != null) {
                scan++;
                ThdProduct thdProduct = ThdHelper.newThdProduct(product.getWebsite());

                thdProduct.setSourceId(product.getSourceId());
                thdProduct.setImageUrl(product.getImageUrl());
                thdProduct.setPtmCateId(product.getCategoryId());
                thdProduct.setPrice(product.getPrice());
                thdProduct.setTitle(product.getTitle());
                thdProduct.setUrl(product.getUrl());

                try{
                    thdService.createProduct(thdProduct);
                    succedd++;
                    logger.debug(thdProduct.getSourceId()+" save succedd ");
                }catch (Exception e){
                    logger.debug(thdProduct.getSourceId()+" save fail ");
                    if(e.getMessage().contains("execute statement")){
                        duplicate++;
                    }
                }
            } else {
                try {
                    logger.debug("--save--waiting--");
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }
            }
            logger.debug("--save--end--");
        }
        time2 = System.currentTimeMillis();
        logger.debug("scan "+scan+" product,exist "+duplicate+",add "+succedd+",time="+(time2-time1)+"ms");
    }
}
