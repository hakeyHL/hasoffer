package hasoffer.timer.msp.worker;

import hasoffer.base.model.Website;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.po.thd.msp.ThdMspProduct;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceUncmpProduct;

import java.util.concurrent.BlockingQueue;

public class SaveUncmpProductWorker implements Runnable {
    private static boolean isFinished = false;
    private BlockingQueue<MySmartPriceUncmpProduct> queue;
    private IMspService mspService;

    public SaveUncmpProductWorker(BlockingQueue<MySmartPriceUncmpProduct> queue, IMspService mspService) {
        this.queue = queue;
        this.mspService = mspService;
    }

    public static boolean isIsFinished() {
        return isFinished;
    }

    public static void setIsFinished(boolean isFinished) {
        SaveUncmpProductWorker.isFinished = isFinished;
    }

    @Override
    public void run() {
        while (true) {
            if (isFinished && queue.isEmpty()) {
                break;
            }

            MySmartPriceUncmpProduct product = null;
            try {
                product = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ThdMspProduct productJob = new ThdMspProduct(product.getCategoryId(),
                    "",
                    product.getUrl(),
                    product.getUrl(),
                    product.getImgUrl(),
                    product.getTitle(),
                    product.getSite(),
                    product.getPrice());
            productJob.setWebsite(Website.MYSMARTPRICE);
            mspService.saveUncmpProduct(productJob);
        }
    }
}
