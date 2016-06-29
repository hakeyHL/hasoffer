package hasoffer.core.search;

import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.AffiliateProduct;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.helper.WebsiteProcessorFactory;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.mysmartprice.MspListProcessor;
import hasoffer.fetch.sites.mysmartprice.NewMspSkuCompareProcessor;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceCmpSku;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/3/16
 * Function :
 */
public class SearchProductHelper {
    private static Logger logger = LoggerFactory.getLogger(SearchProductHelper.class);

    private static Website[] websites = {Website.SNAPDEAL, Website.SHOPCLUES, Website.PAYTM, Website.FLIPKART, Website.EBAY, Website.AMAZON};

    public static Map<Website, ListProduct> getProducts(SrmSearchLog searchLog) {

        String keyword = searchLog.getKeyword();
        double stdPrice = searchLog.getPrice();

        Map<Website, ListProduct> listProductMap = new HashMap<Website, ListProduct>();

        // voodoo
//        VoodooHelper.getProductsFromVoodoo(listProductMap, keyword);

        // read by affiliate api
        getProductsFromAffiliate(listProductMap, keyword, stdPrice);

        // read from html
        getProductsFromWebsite(listProductMap, keyword, stdPrice);

        // msp
        getProductsFromMSP(listProductMap, keyword, stdPrice);

        return listProductMap;
    }

    public static void getProductsFromAffiliate(Map<Website, ListProduct> listProductMap, String keyword, double stdPrice) {
        try {
            getProductFromFlipkart(listProductMap, keyword, stdPrice);
        } catch (Exception e) {
            logger.error(String.format("error : search [%s] from [%s].Info [%s]", keyword, Website.FLIPKART, e.getMessage()));
        }
    }

    public static void getProductsFromMSP(Map<Website, ListProduct> listProductMap, String keyword, double stdPrice) {
        MspListProcessor listProcessor = new MspListProcessor();
        try {
            List<ListProduct> listProducts = listProcessor.getProductSetByKeyword(keyword, 5);

            for (ListProduct listProduct : listProducts) {
                if (matched(keyword, listProduct.getTitle()) && nearPrice(stdPrice, listProduct.getPrice())) {
                    String url = listProduct.getUrl();

                    int index = url.indexOf("?");
                    if (index > 0) {
                        url = url.substring(0, index);
                    }

                    NewMspSkuCompareProcessor nmscp = new NewMspSkuCompareProcessor();
                    MySmartPriceProduct mspProduct = nmscp.parse(url);

                    List<MySmartPriceCmpSku> mspCmpSkus = mspProduct.getCmpSkus();

                    for (MySmartPriceCmpSku cmpSku : mspCmpSkus) {
                        if (listProductMap.containsKey(cmpSku.getWebsite())) {
                            continue;
                        }
                        listProductMap.put(cmpSku.getWebsite(),
                                new ListProduct(
                                        0L, "", cmpSku.getUrl(), "",
                                        listProduct.getTitle(),
                                        cmpSku.getPrice(),
                                        cmpSku.getWebsite(), ProductStatus.ONSALE
                                )
                        );
                    }

                    break;
                }
            }
        } catch (Exception e) {
            logger.error(String.format("error : search [%s] from [%s]. Info:[%s].", keyword, Website.MYSMARTPRICE, e.getMessage()));
        }
    }

    public static void getProductsFromWebsite(Map<Website, ListProduct> listProductMap, String keyword, double stdPrice) {
        //遍历websiteList，添加比较列表
        for (Website website : websites) {

            if (listProductMap.containsKey(website)) {
                continue;
            }

            IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(website);

            List<ListProduct> listProducts = null;
            try {
                listProducts = listProcessor.getProductSetByKeyword(keyword, 5);

                if (ArrayUtils.isNullOrEmpty(listProducts)) {
                    continue;
                }
            } catch (Exception e) {
                logger.error(String.format("error : search [%s] from [%s].Info : [%s]", keyword, website, e.getMessage()));
            }

            if (ArrayUtils.hasObjs(listProducts)) {
                for (ListProduct listProduct : listProducts) {
                    if (matched(keyword, listProduct.getTitle()) && nearPrice(stdPrice, listProduct.getPrice())) {
                        listProductMap.put(website, listProduct);
                        break;
                    }
                }
            }
        }
    }

    public static void getProductFromFlipkart(Map<Website, ListProduct> listProductMap, String keyword, double stdPrice) throws Exception {
        Website website = Website.FLIPKART;

        FlipkartAffiliateProductProcessor affProcessor = new FlipkartAffiliateProductProcessor();
        List<AffiliateProduct> affPros = affProcessor.getAffiliateProductByKeyword(keyword, 5);

        for (AffiliateProduct affPro : affPros) {
            if (matched(keyword, affPro.getTitle()) && nearPrice(stdPrice, affPro.getPrice())) {

                listProductMap.put(website, new ListProduct(0L,
                                affPro.getSourceId(),
                                affPro.getUrl(),
                                affPro.getImageUrl(),
                                affPro.getTitle(),
                                affPro.getPrice(),
                                affPro.getWebsite(),
                                ProductStatus.ONSALE)
                );

                break;
            }
        }
    }

    private static boolean nearPrice(double stdPrice, float price) {

        if (stdPrice == 0) {
            return true;
        }

        if (stdPrice * 0.6 < price && price < stdPrice * 1.4) {
            return true;
        }

        return false;
    }

    public static boolean matched(String s1, String s2) {

        s1 = StringUtils.toLowerCase(s1);
        s2 = StringUtils.toLowerCase(s2);

        s1 = StringUtils.filterAndTrim(s1, Arrays.asList("[", "]", ";", "%", "$", "@", "#", "(", ")"));
        s2 = StringUtils.filterAndTrim(s2, Arrays.asList("[", "]", ";", "%", "$", "@", "#", "(", ")"));

        int for1 = s1.indexOf("for");
        int for2 = s2.indexOf("for");

        // 如果两个字符串都含或都不含for关键字
        if (for1 * for2 < 0) {
            return false;
        }

        float mc = StringUtils.wordMatchD(s1, s2);

        return mc >= 0.5;
    }
}
