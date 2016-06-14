package hasoffer.core.test;

import hasoffer.affiliate.affs.amazon.AmazonAffiliateProductProcessor;
import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.affs.snapdeal.SnapdealProductProcessor;
import hasoffer.affiliate.exception.AffiliateAPIException;
import hasoffer.affiliate.model.AffiliateProduct;
import hasoffer.core.product.IFetchService;
import hasoffer.core.product.iml.FetchServiceImpl;
import hasoffer.fetch.model.Product;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.fetch.sites.snapdeal.SnapdealHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created on 2016/2/24.
 */
public class AffiliateListProcessorTest {


    @Test
    public void testFlipkartListProcessor() {
        String url1 = "http://www.flipkart.com/signature-vm-44-stereo-bass-headphone-wired-headphones/p/itmegx4vhk4g2fv9?pid=ACCEGX4VCRSKVUN4";
        String url2 = "http://www.amazon.in/gp/product/B00JJIDBIC/ref=s9_simh_gw_p364_d6_i4?pf_rd_m=A1VBAL9TL5WCBF&pf_rd_s=desktop-top&pf_rd_r=1NTTH3K93MMV2CEJD28Q&pf_rd_t=36701&pf_rd_p=821056287&pf_rd_i=desktop";
        String url3 = "http://www.flipkart.com/sandisk-ultra-16-gb-sdhc-class-10-48-mb-s-memory-card/p/itmdgf8fbv6jjgpg?pid=ACCD6KQ3PRMMPJPT&al=1U5pK53iATvP1fv6EAnc78ldugMWZuE7sHPMhtl4IOqUz9KLRd4HdQtfopWe02DplgKW7xqvIfE%3D&ref=L%3A-8655159012366530478&srno=b_6&findingMethod=ch_vn_tablet_filter";
        IFetchService fetchService = new FetchServiceImpl();
        Product product = fetchService.fetchByUrl(url1);
        System.out.println(product);
    }

    @Test
    public void testFlipkartAffiliateProduct() {
        String url = "http://www.flipkart.com/samsung-galaxy-s7-edge/p/itmegmkzvcpxxqhz?pid=MOBEGFZPCHJHAZVU&ref=L%3A7915091748451274242&srno=p_1&query=Samsung+Galaxy+S7+Edge&otracker=from-search";
        String sourceId = FlipkartHelper.getSkuIdByUrl(url);
        FlipkartAffiliateProductProcessor flipkartAffilicateProductProcessor = new FlipkartAffiliateProductProcessor();
        AffiliateProduct flipkartProduct = null;
        try {
            flipkartProduct = flipkartAffilicateProductProcessor.getAffiliateProductBySourceId("LSDE8MXGZ92WH3GC");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSnapdealAffiliateProduct() {
        String url = "https://www.snapdeal.com/product/samsung-j5-8gb-espresso-brown/659785514599";
        String sourceId = SnapdealHelper.getProductIdByUrl(url);
        SnapdealProductProcessor snapdealAffilicateProductProcessor = new SnapdealProductProcessor();
        AffiliateProduct snapdealProduct = null;
        try {
            snapdealProduct = snapdealAffilicateProductProcessor.getAffiliateProductBySourceId(sourceId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(snapdealProduct);
    }

    @Test
    public void testAmazonAffiliateProduct() {

        String sourceId = "B00SJ8L2VQ";

        AmazonAffiliateProductProcessor amazonAffiliateProductProcessor = new AmazonAffiliateProductProcessor();

        AffiliateProduct affiliateProduct = null;
        try {
            affiliateProduct = amazonAffiliateProductProcessor.getAffiliateProductBySourceId(sourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }

       System.out.print(affiliateProduct);

    }

    @Test
    public void testFlipkartApiSearchByKeyword() throws AffiliateAPIException, IOException {

        String keyword = "Apple iPhone 6";

        FlipkartAffiliateProductProcessor processor = new FlipkartAffiliateProductProcessor();

        List<AffiliateProduct> productList = processor.getAffiliateProductByKeyword(keyword, 15);

        System.out.println(productList);
    }


    @Test
    public void testShopcluesProductApi(){



    }

}
