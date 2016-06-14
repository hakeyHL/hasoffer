package hasoffer.timer.test;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.sites.shopclues.ShopCluesSummaryProductProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/4/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ShopcluesOffsaleTest {


    private static Logger logger = LoggerFactory.getLogger(ShopcluesOffsaleTest.class);
    private static final String Q_SHOPCLUES_OFFSALE =
            "SELECT t FROM PtmCmpSku t " +
                    "WHERE t.website = 'SHOPCLUES' " +
                    "AND t.status = 'OFFSALE' " +
                    "ORDER BY t.id";

    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;

    @Test
    public void testShopcluesOffsale() throws HttpFetchException, ContentParseException {

        PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_SHOPCLUES_OFFSALE, 1, Integer.MAX_VALUE);

        List<PtmCmpSku> skus = pageableResult.getData();

        for (PtmCmpSku sku : skus) {

            String url = sku.getUrl();

            //判断是否为offsale
            ShopCluesSummaryProductProcessor processor = new ShopCluesSummaryProductProcessor();
            FetchedProduct fetchedProduct = null;
            try {
                fetchedProduct = processor.getSummaryProductByUrl(url);
                if (!ProductStatus.OFFSALE.equals(fetchedProduct.getProductStatus())) {
                    continue;
                }
            } catch (HttpFetchException e) {
                logger.debug("httpFetchException exception [" + sku.getId() + "]");
            } catch (ContentParseException e) {
                logger.debug("content parse exception [" + sku.getId() + "]");
            }


            //进过一系列判断，得到一个要访问的urlList集合
            List<String> urlList = getUrlList(url);

            for (String urlString : urlList) {

                FetchedProduct product = null;

                try {
                    fetchedProduct = processor.getSummaryProductByUrl(url);
                } catch (HttpFetchException e) {
                    logger.debug("httpFetchException exception [" + sku.getId() + "]");
                    continue;
                } catch (ContentParseException e) {
                    logger.debug("content parse exception [" + sku.getId() + "]");
                    continue;
                }

                if (!ProductStatus.OFFSALE.equals(product.getProductStatus())) {

                    PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

                    updater.getPo().setUrl(urlString);

                    dbm.update(updater);

                    logger.debug("update [" + sku.getId() + "]" + "[" + sku.getTitle() + "]");
                    break;
                }
            }

            logger.debug("no update for [" + sku.getId() + "]");
        }

    }


    private List<String> getUrlList(String url) {

        List<String> urlList = new ArrayList<String>();
        //判断url中是否是以-X结尾的
        //如果是，替换x的值，1-10，排除x
        //如果不是，从1-10

        int lastLine = url.lastIndexOf("-");
        int lastPoint = url.lastIndexOf('.');

        if (lastLine == -1 || lastPoint == -1) {
            return urlList;
        }
        String subStr = url.substring(lastLine + 1, lastPoint);

        int num = 0;

        if (subStr.length() == 1) {
            char[] chars = subStr.toCharArray();
            char ch = chars[0];
            if (ch >= '1' && ch <= '9') {
                num = Integer.valueOf(ch + "");
            }
        }

        if (num == 0) {//ex:http://www.shopclues.com/new-intex-mobile-aqua-speed-hd-with-manufacturer-warranty.html

            for (int i = 1; i < 11; i++) {
                String newUrl = url.replace(".html", "-" + i + ".html");
                urlList.add(newUrl);
            }

        } else {//ex:http://www.shopclues.com/zen-x4-3.html

            for (int i = 1; i <= 10; i++) {
                if (i == num) {
                    continue;
                } else {
                    String newUrl = url.replace("-" + num + ".html", "-" + i + ".html");
                    urlList.add(newUrl);
                }
            }
        }
        return urlList;
    }
}
