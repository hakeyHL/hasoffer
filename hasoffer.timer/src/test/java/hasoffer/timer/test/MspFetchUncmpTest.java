package hasoffer.timer.test;

import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.msp.updater.MspCategoryUpdater;
import hasoffer.core.persistence.po.thd.msp.ThdMspProduct;
import hasoffer.core.product.ICategoryService;
import hasoffer.fetch.sites.mysmartprice.MspHelper;
import hasoffer.fetch.sites.mysmartprice.MspList2Processor;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceUncmpProduct;
import hasoffer.timer.msp.vo.MspUnCmpModel;
import hasoffer.timer.msp.worker.FetchProductTagNodesWorker;
import hasoffer.timer.msp.worker.ParseTagNodeToProductWorker;
import hasoffer.timer.msp.worker.SaveUncmpProductWorker;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by chevy on 2015/12/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class MspFetchUncmpTest {
    private final static String Q_CATEGORY_UNCMP_FIXCOUNT = "SELECT t FROM MspCategory t where t.proCount = 0 and t.parentId > 0";
    private final static String Q_CATEGORY_UNCMPS = "SELECT t FROM MspCategory t where t.parentId > 0 and t.compared = 0";
    private final static String Q_THDMSPPRODUCT_SOURCEID = "SElECT t.sourceId FROM ThdMspProduct t where t.sourceId is not null";
    @Resource
    IMspService mspService;

    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(MspFetchUncmpTest.class);

    @Test
    public void fixUncomCateProductCount() {
        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMP_FIXCOUNT);
        if (categories != null && categories.size() > 0) {
            for (MspCategory category : categories) {
                int count = MspList2Processor.getProductCount(category.getUrl());
                MspCategoryUpdater updater = new MspCategoryUpdater(category.getId());
                updater.getPo().setProCount(count);
                dbm.update(updater);
            }
        }
    }

    @Test
    public void fetchUncmpProducts() {
        BlockingQueue<MySmartPriceUncmpProduct> queue = new ArrayBlockingQueue<MySmartPriceUncmpProduct>(1024);
        ExecutorService service = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            service.execute(new SaveUncmpProductWorker(queue, mspService));
        }

        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMPS);
        if (categories != null && categories.size() > 0) {
            for (MspCategory category : categories) {
                String[] subStrs = category.getUrl().trim().split("/");
                String cateIdentify = subStrs[subStrs.length - 1];
                MspList2Processor.fetchProductsByCate(category.getId(), cateIdentify, category.getProCount(), queue);
            }
        }

        SaveUncmpProductWorker.setIsFinished(true);
    }

    /**
     * 单线程测试抓取MySmartPrice商品详情
     *
     * @throws HttpFetchException
     * @throws XPatherException
     */
    @Test
    public void fetchMySmartPriceUncmpProductsSingleThread() throws HttpFetchException, XPatherException {

        String AJAX_PRODUCTS_QUERY = "http://www.mysmartprice.com/fashion/filters/filter_get_revamp?recent=0&q=filter%2F&subcategory=$cateIdentifier&start=$start&rows=$rowCount&page_name=";

        String PRODUCT_SECTION = "//div[@class='grid-item product']";

        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMPS);

        if (categories != null && categories.size() > 0) {
            for (MspCategory category : categories) {

                //获取cate
                String[] subStrs = category.getUrl().trim().split("/");
                String cateIdentify = subStrs[subStrs.length - 1];

                //通过分页的方式获取
                String url = AJAX_PRODUCTS_QUERY.replace("$cateIdentifier", cateIdentify).replace("$start", 0 + "").replace("$rowCount", category.getProCount() + "");
                System.out.println(url);
                TagNode root = HtmlUtils.getUrlRootTagNode(url);

                List<TagNode> secNodes = HtmlUtils.getSubNodesByXPath(root, PRODUCT_SECTION);
                if (secNodes != null && secNodes.size() > 0) {
                    for (TagNode node : secNodes) {
                        MySmartPriceUncmpProduct mySmartPriceUncmpProduct = MspList2Processor.parseUncmpProductByTagNode(node, category.getId());
                        //todo 从url中获取sourceId，第二个字段
                        ThdMspProduct productJob = new ThdMspProduct(mySmartPriceUncmpProduct.getCategoryId(), mySmartPriceUncmpProduct.getUrl() + "", mySmartPriceUncmpProduct.getOfferUrl(), mySmartPriceUncmpProduct.getUrl(), mySmartPriceUncmpProduct.getImgUrl(), mySmartPriceUncmpProduct.getTitle(), Website.MYSMARTPRICE, mySmartPriceUncmpProduct.getPrice());
                        mspService.saveUncmpProduct(productJob);
                    }
                }
            }
        }

    }


    /**
     * 单线程测试抓取MySmartPrice商品详情2
     *
     * @throws HttpFetchException
     * @throws XPatherException
     */
    @Test
    public void fetchMySmartPriceUncmpProductsSingleThread2() throws HttpFetchException, XPatherException {

        String AJAX_PRODUCTS_QUERY = "http://www.mysmartprice.com/fashion/filters/filter_get_revamp?recent=0&q=filter%2F&subcategory=$cateIdentifier&start=$start&rows=$rowCount&page_name=";
        String PRODUCT_SECTION = "//div[@class='grid-item product']";

        //1.从数据库中拿到要解析的页面
        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMPS);
        MspCategory mspCategory = categories.get(0);
        categories.clear();
        categories.add(mspCategory);
        if (categories != null && categories.size() > 0) {
            //1.定义map存储key=sourceId，value=tagNode的产品id和tagNode
            Map<String, Map<TagNode, Long>> map = new HashMap<String, Map<TagNode, Long>>();
            for (MspCategory category : categories) {
                //获取cate
                String[] subStrs = category.getUrl().trim().split("/");
                String cateIdentify = subStrs[subStrs.length - 1];
                //通过分页的方式获取
                String url = AJAX_PRODUCTS_QUERY.replace("$cateIdentifier", cateIdentify).replace("$start", 0 + "").replace("$rowCount", category.getProCount() + "");
                String PRODUCT_NAME_PATH = "/a[@class='info']/div[@class='title']";
                System.out.println(url);
                Long time1 = System.currentTimeMillis();
                TagNode root = HtmlUtils.getUrlRootTagNode(url);
                Long time2 = System.currentTimeMillis();
                Long timeRoot = time2 - time1;
                System.out.print("getRoot" + timeRoot + "秒");
                Long time3 = System.currentTimeMillis();
                List<TagNode> secNodes = HtmlUtils.getSubNodesByXPath(root, PRODUCT_SECTION);
                Long time4 = System.currentTimeMillis();
                Long timeSubNode = time4 - time3;
                System.out.print("getSubNode" + timeSubNode + "秒");
                if (secNodes != null && secNodes.size() > 0) {
                    for (TagNode node : secNodes) {
                        String offerUrl = "";
                        TagNode nameNode = HtmlUtils.getFirstNodeByXPath(node, PRODUCT_NAME_PATH);
                        if (nameNode == null) {
                            System.out.println("name node null");
                        } else {
                            offerUrl = nameNode.getParent().getAttributeByName("href");
                            if (!offerUrl.contains("//")) {
                                offerUrl = "http://www.mysmartprice.com/" + offerUrl;
                            }
                        }
                        String sourceId = MspHelper.getProductIdByUrl(offerUrl);
                        Map<TagNode, Long> map2 = new HashMap<TagNode, Long>();
                        map2.put(node, category.getId());
                        map.put(sourceId, map2);
                    }
                }
            }
            //3.去数据库中比对，排除sourceId存在的数据，取到List<TagNode>
            List<String> sourceIdList = dbm.query(Q_THDMSPPRODUCT_SOURCEID);
            //如果集合长度不为0，遍历
            if (sourceIdList.size() != 0) {
                for (String id : sourceIdList) {
                    if (map.containsKey(id)) {
                        map.remove(id);
                    }
                }
            }


            //4.遍历map的同时，调用方法抓取抓取
            for (Map.Entry entry : map.entrySet()) {
                String sourceId = entry.getKey().toString();
                Map<TagNode, Long> productMap = (Map<TagNode, Long>) entry.getValue();
                for (Map.Entry entry2 : productMap.entrySet()) {
                    TagNode node = (TagNode) entry2.getKey();
                    Long categoryId = (Long) entry2.getValue();
                    Long time1 = System.currentTimeMillis();
                    MySmartPriceUncmpProduct mySmartPriceUncmpProduct = MspList2Processor.parseUncmpProductByTagNode(node, categoryId);
                    Long time2 = System.currentTimeMillis();
                    Long time = time2 - time1;
                    System.out.print("解析tagnode耗时" + time + "秒");
                    ThdMspProduct productJob = new ThdMspProduct(categoryId, sourceId, mySmartPriceUncmpProduct.getOfferUrl(), mySmartPriceUncmpProduct.getUrl(), mySmartPriceUncmpProduct.getImgUrl(), mySmartPriceUncmpProduct.getTitle(), mySmartPriceUncmpProduct.getSite(), mySmartPriceUncmpProduct.getPrice());
                    System.out.print(productJob);
                    mspService.saveUncmpProduct(productJob);
                }
            }
        }
    }

    /**
     * 根据offerUrl截取出商品的sourceId
     *
     * @param offerUrl
     * @return
     */
    private Long getSourceIdByOfferUrl(String offerUrl) {

        String[] subStrs = offerUrl.split("/");

        String[] subStrs2 = subStrs[subStrs.length - 1].split("-");

        char[] chs = subStrs2[subStrs2.length - 1].toCharArray();

        int count = 0;

        for (int i = 0; i < chs.length; i++) {

            if (chs[i] >= '0' || chs[i] <= '9') {
                break;
            }
            count++;
        }

        String sourceId = subStrs2[subStrs2.length - 1].substring(count);

        return Long.parseLong(sourceId);
    }


    /**
     * 测试多线程抓取
     */
    @Test
    public void testMspUnCmpFetchTask(){

        final ConcurrentLinkedQueue<MspCategory> categoryListQueue = new ConcurrentLinkedQueue<MspCategory>();
        final ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue = new ConcurrentLinkedQueue<MspUnCmpModel>();
        // 拿到要抓取的列表页url集合
        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMPS);
        if (categories != null && categories.size() > 0) {// 集合中数据不为空
            //将遍历的结果放入队列
            for (MspCategory category : categories) {
                categoryListQueue.add(category);
            }
        }
        List<String> sourceIdList = dbm.query(Q_THDMSPPRODUCT_SOURCEID);

        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new FetchProductTagNodesWorker(categoryListQueue,mspUnCmpModelQueue,sourceIdList));
        es.execute(new ParseTagNodeToProductWorker(mspUnCmpModelQueue,mspService));

        while (true) {
            logger.debug( "------------------FETCH main thread------------------------");
            logger.debug("抓取队列剩余"+FetchProductTagNodesWorker.mspCategoryQueue.size()+"个");
            logger.debug("解析队列现有"+FetchProductTagNodesWorker.mspUnCmpModelQueue.size()+"个");
            try {
                TimeUnit.SECONDS.sleep(1000);
                if (FetchProductTagNodesWorker.aliveThreadCount==0&&ParseTagNodeToProductWorker.aliveThreadCount==0){
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }

        es.shutdown();

    }

}
