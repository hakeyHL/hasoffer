package hasoffer.timer.msp.worker;

import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.po.thd.msp.ThdMspProduct;
import hasoffer.fetch.sites.mysmartprice.MspList2Processor;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceUncmpProduct;
import hasoffer.timer.msp.vo.MspUnCmpModel;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2016/2/19.
 */
public class ParseTagNodeToProductWorker implements Runnable {

    private static ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue;
    private static Logger logger = LoggerFactory.getLogger(ParseTagNodeToProductWorker.class);
    private IMspService mspService;
    public static int aliveThreadCount = 0;
    public static long pareseProductCount = 0;

    public ParseTagNodeToProductWorker(ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue, IMspService mspService) {
        this.mspUnCmpModelQueue = mspUnCmpModelQueue;
        this.mspService = mspService;
        aliveThreadCount++;
    }

    @Override
    public void run() {

        while (true) {
            MspUnCmpModel mspUnCmpModel = mspUnCmpModelQueue.poll();
            //队列元素为空，且生产者线程个数为0
            if (mspUnCmpModel == null && FetchProductTagNodesWorker.aliveThreadCount == 0) {
                aliveThreadCount--;
                break;
            }

            if (mspUnCmpModel != null) {
                TagNode tagNode = mspUnCmpModel.getTagNode();
                Long categoryId = mspUnCmpModel.getCategoryId();
                try {
                    MySmartPriceUncmpProduct mySmartPriceUncmpProduct = MspList2Processor.parseUncmpProductByTagNode(tagNode, categoryId);
                    ThdMspProduct product = new ThdMspProduct(categoryId, mspUnCmpModel.getSourceId(), mySmartPriceUncmpProduct.getOfferUrl(), mySmartPriceUncmpProduct.getUrl(), mySmartPriceUncmpProduct.getImgUrl(), mySmartPriceUncmpProduct.getTitle(), mySmartPriceUncmpProduct.getSite(), mySmartPriceUncmpProduct.getPrice());

                    mspService.saveUncmpProduct(product);
                    logger.debug( "------------------parse MspUnCmpProduct success------------------------"+pareseProductCount++);
                } catch (Exception e) {
                    mspUnCmpModelQueue.add(mspUnCmpModel);
                    logger.debug( "------------------parse MspUnCmpProduct fail---------------------categoryId="+categoryId);
                }
            }
        }
    }
}
