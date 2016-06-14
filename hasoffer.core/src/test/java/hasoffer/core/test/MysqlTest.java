package hasoffer.core.test;

import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.AffiliateProduct;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import jodd.io.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/4/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class MysqlTest {

    private static final String Q_PTM_CMPSKU =
            "SELECT t FROM PtmCmpSku t " +
                    " ORDER BY t.id ";

    private static final String Q_PTM_CMPSKU_AMAZON =
            "SELECT t FROM PtmCmpSku t WHERE t.website = 'AMAZON' " +
                    " ORDER BY t.id ";
    private static final String Q_PTM_CMPSKU_NOTAMAZON =
            "SELECT t FROM PtmCmpSku t WHERE t.website <> 'AMAZON' " +
                    "ORDER BY t.id";

    private static final String Q_PTMCMPSKU_ERROR = "SELECT t FROM PtmCmpSku t WHERE t.productId = 999999999 ORDER BY t.id ASC ";
    private static final String Q_PTMCMPSKU_ERROR_BYID = "SELECT t FROM PtmCmpSku t WHERE t.id = ?0 ";

    private static final String Q_PTMCMPSKU_BYSID = "SELECT t FROM PtmCmpSku t WHERE t.sourceSid = ?0 ";

    private static final String Q_WEBSITE_PTMCMPSKU_ONSALE = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.status = 'ONSALE' ";

    private Logger logger = LoggerFactory.getLogger(MysqlTest.class);

    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;

    private ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

    @Test
    public void testLinkedQueue() {

        new Runnable() {
            @Override
            public void run() {
                while (true) {
                    r();
                }
            }
        }.run();

    }

    public void r() {
        int notAmazonPageNum = 1;
        int amazonPageNum = 1;
        int PAGE_SIZE = 500;

        PageableResult<PtmCmpSku> notAmazonResult = dbm.queryPage(Q_PTM_CMPSKU_NOTAMAZON, notAmazonPageNum, PAGE_SIZE);
        PageableResult<PtmCmpSku> amazonResult = dbm.queryPage(Q_PTM_CMPSKU_AMAZON, amazonPageNum, PAGE_SIZE);

        int notAmazonCount = (int) notAmazonResult.getTotalPage();
        int amazonCount = (int) amazonResult.getTotalPage();

        List<PtmCmpSku> notAmazonSkus = notAmazonResult.getData();
        List<PtmCmpSku> amazonSkus = notAmazonResult.getData();

        while (notAmazonPageNum <= notAmazonCount) {

            if (skuQueue.size() > 600) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            logger.info(String.format("update sku : %d/%d .", notAmazonPageNum, notAmazonCount));

            if (notAmazonPageNum > 1) {
                notAmazonSkus = dbm.query(Q_PTM_CMPSKU_NOTAMAZON, notAmazonPageNum, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(notAmazonSkus)) {

                for (int i = 0; i < notAmazonSkus.size(); i++) {

                    PtmCmpSku cmpSku = notAmazonSkus.get(i);

                    if (cmpSku.getUpdateTime().getTime() < TimeUtils.today()) {
                        skuQueue.add(cmpSku);
                    }
                    skuQueue.add(cmpSku);
                    if (i % 10 == 0) {
                        if (amazonSkus.get(0).getUpdateTime().getTime() < TimeUtils.today()) {
                            skuQueue.add(amazonSkus.remove(0));
                            amazonCount--;
                            if (amazonCount == 0) {
                                amazonPageNum++;
                            }
                        }
                    }
                }
            }
            notAmazonPageNum++;
        }
    }


    @Test
    public void test() {

        List<Object> object = dbm.query(Q_PTMCMPSKU_BYSID, Arrays.asList("Q_PTMCMPSKU_BYSID"));

        System.out.println();

    }

    @Test
    public void get() {

        PtmCmpSku ptmCmpSku = new PtmCmpSku();

        cmpSkuService.createCmpSku(ptmCmpSku);
        System.out.println(ptmCmpSku);
    }

    @Test
    public void test1() {

        String url = "http://www.flipkart.com/samsung-galaxy-s7-edge/p/itmegmkzvcpxxqhz?pid=MOBEGFZPCHJHAZVU&ref=L%3A7915091748451274242&srno=p_1&query=Samsung+Galaxy+S7+Edge&otracker=from-search";
        String sourceId = FlipkartHelper.getSkuIdByUrl(url);
        FlipkartAffiliateProductProcessor flipkartAffilicateProductProcessor = new FlipkartAffiliateProductProcessor();
        AffiliateProduct flipkartProduct = null;
        try {
            flipkartProduct = flipkartAffilicateProductProcessor.getAffiliateProductBySourceId("MOBECCA5Y5HBYR3Q");

        } catch (Exception e) {
            e.printStackTrace();
        }

        String imageUrl = flipkartProduct.getImageUrl();
        float price = flipkartProduct.getPrice();
        String title = flipkartProduct.getTitle();
        String productStatus = flipkartProduct.getProductStatus();
        String url1 = flipkartProduct.getUrl();

        FetchedProduct fetchedProduct = new FetchedProduct();
        fetchedProduct.setPrice(price);
        fetchedProduct.setSourceSid(sourceId);
        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url1);

        //todo 商品状态对于flipkart联盟的解析有一些问题，待改进
        if ("false".equals(productStatus)) {
            fetchedProduct.setProductStatus(ProductStatus.OUTSTOCK);
        } else if ("true".equals(productStatus)) {
            fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        } else if ("none".equals(productStatus)) {
            fetchedProduct.setProductStatus(ProductStatus.OFFSALE);
        } else {
            fetchedProduct.setProductStatus(null);
        }

        cmpSkuService.updateCmpSkuBySummaryProduct(168542, fetchedProduct);

    }


    /**
     * 修复
     * 修复价格差距太大
     * 造成的错误
     * <p>
     * 记录所有被更新的sku，然后恢复
     */
    @Test
    public void fixErrorPrice() throws IOException {

        List<PtmCmpSku> skuList = dbm.query(Q_PTMCMPSKU_ERROR);

        for (PtmCmpSku sku : skuList) {

            FileUtil.appendString("C:/Users/wing/Desktop/skuid.txt", sku.getId() + "\n");

        }
    }

    @Test
    public void fixErrorPrice1() throws IOException {

        String[] strings = FileUtil.readLines("C:/Users/wing/Desktop/skuid.txt");

        for (String str : strings) {

            PtmCmpSku sku = dbm.querySingle(Q_PTMCMPSKU_ERROR_BYID, Arrays.asList(Long.parseLong(str)));

            if (sku == null) {
                logger.debug(str + " get null");
                continue;
            }

            long productId = sku.getProductId();

            FileUtil.appendString("C:/Users/wing/Desktop/skufix.sql", "update ptmcmpsku set productId = " + productId + " where id = " + sku.getId() + ";\n");
        }

    }

    @Test
    public void testSingle() {

        long number = dbm.querySingle(Q_WEBSITE_PTMCMPSKU_ONSALE, Arrays.asList(Website.FLIPKART));

        System.out.println(number);

    }

}
