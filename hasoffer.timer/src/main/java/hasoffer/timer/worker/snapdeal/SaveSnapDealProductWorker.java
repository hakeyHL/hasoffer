package hasoffer.timer.worker.snapdeal;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.thd.snapdeal.ThdAProduct;
import hasoffer.core.thd.snapdeal.ISnapdealService;
import hasoffer.fetch.sites.snapdeal.SnapdealHelper;
import hasoffer.fetch.sites.snapdealDeprecated.model.SnapDealFetchProduct;

import java.util.concurrent.BlockingQueue;

/**
 * Author:menghaiquan
 * Date:2016/1/18
 */
public class SaveSnapDealProductWorker implements Runnable {
    private static boolean isFinished = false;
    private BlockingQueue<SnapDealFetchProduct> queue;
    private ISnapdealService snapdealService;

    public SaveSnapDealProductWorker(BlockingQueue<SnapDealFetchProduct> queue, ISnapdealService snapdealService) {
        this.queue = queue;
        this.snapdealService = snapdealService;
    }

    public static boolean isIsFinished() {
        return isFinished;
    }

    public static void setIsFinished(boolean isFinished) {
        SaveSnapDealProductWorker.isFinished = isFinished;
    }

    @Override
    public void run() {
        while (true) {
            if (isFinished && queue.isEmpty()) {
                break;
            }

            SnapDealFetchProduct product = null;
            try {
                product = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ThdAProduct productJob = new ThdAProduct(product.getCategoryId(),
                    product.getUrl(),
                    SnapdealHelper.getProductIdByUrl(product.getUrl()),
                    product.getImgUrl(),
                    product.getName(),
                    product.getPrice());
            productJob.setWebsite(Website.SNAPDEAL);
            snapdealService.createProduct(productJob);
        }
    }
}
