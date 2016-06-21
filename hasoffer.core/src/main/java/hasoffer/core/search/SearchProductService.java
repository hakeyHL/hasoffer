package hasoffer.core.search;

import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.AffiliateProduct;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.product.SearchedSku;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Date : 2016/3/16
 * Function :
 */
@Service
public class SearchProductService {
    private static Logger logger = LoggerFactory.getLogger(SearchProductService.class);
    private static List<Website> websites = Arrays.asList(Website.FLIPKART, Website.SNAPDEAL, Website.SHOPCLUES, Website.PAYTM, Website.EBAY, Website.AMAZON);
    @Resource
    IMongoDbManager mdm;

    public static void getProductsFromAffiliate(Map<Website, List<ListProduct>> listProductMap, String keyword) {
        try {
            getProductFromFlipkart(listProductMap, keyword);
        } catch (Exception e) {
            logger.error(String.format("error : search [%s] from [%s].Info [%s]", keyword, Website.FLIPKART, e.getMessage()));
        }
    }

    public static void getProductsFromMSP(Map<Website, List<ListProduct>> listProductMap, String keyword) {
        MspListProcessor listProcessor = new MspListProcessor();
        try {
            List<ListProduct> listProducts = listProcessor.getProductSetByKeyword(keyword, 5);

            for (ListProduct listProduct : listProducts) {

                if (stringMatch(keyword, listProduct.getTitle()) > 0) {

                    String url = listProduct.getUrl();

                    int index = url.indexOf("?");
                    if (index > 0) {
                        url = url.substring(0, index);
                    }

                    NewMspSkuCompareProcessor nmscp = new NewMspSkuCompareProcessor();
                    MySmartPriceProduct mspProduct = nmscp.parse(url);

                    List<MySmartPriceCmpSku> mspCmpSkus = mspProduct.getCmpSkus();

                    for (MySmartPriceCmpSku cmpSku : mspCmpSkus) {
                        Website website = cmpSku.getWebsite();
                        if (!websites.contains(website)) {
                            continue;
                        }

                        List<ListProduct> listProducts1 = listProductMap.get(website);
                        if (listProducts1 == null) {
                            listProducts1 = new ArrayList<ListProduct>();
                            listProductMap.put(website, listProducts1);
                        }

                        listProducts1.add(
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

    public static void getProductsFromWebsite(Map<Website, List<ListProduct>> listProductMap, String keyword) {
        keyword = StringUtils.getCleanWordString(keyword);
        //遍历websiteList，添加比较列表
        for (Website website : websites) {
            //todo shopclues反爬，先不跳过
            if (website == Website.SHOPCLUES) {
                continue;
            }

            List<ListProduct> listProducts = listProductMap.get(website);
            if (listProducts == null) {
                listProducts = new ArrayList<ListProduct>();
                listProductMap.put(website, listProducts);
            }

            IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(website);

            try {
                List<ListProduct> listProducts2 = listProcessor.getProductSetByKeyword(keyword, 10);

                logger.debug(String.format("found [%d] products. search[%s] from [%s].", listProducts2.size(), keyword, website.name()));

                if (ArrayUtils.hasObjs(listProducts2)) {
                    listProducts.addAll(listProducts2);
                }
            } catch (Exception e) {
                logger.error(String.format("error : search [%s] from [%s].Info : [%s]", keyword, website, e.getMessage()));
                continue;
            }
        }
    }

    public static void getProductFromFlipkart(Map<Website, List<ListProduct>> listProductMap, String keyword) throws Exception {
        Website website = Website.FLIPKART;

        List<ListProduct> listProducts = listProductMap.get(website);
        if (listProducts == null) {
            listProducts = new ArrayList<ListProduct>();
            listProductMap.put(website, listProducts);
        }

        FlipkartAffiliateProductProcessor affProcessor = new FlipkartAffiliateProductProcessor();
        List<AffiliateProduct> affPros = affProcessor.getAffiliateProductByKeyword(keyword, 10);

        for (AffiliateProduct affPro : affPros) {

            listProducts.add(
                    new ListProduct(0L,
                            affPro.getSourceId(),
                            affPro.getUrl(),
                            affPro.getImageUrl(),
                            affPro.getTitle(),
                            affPro.getPrice(),
                            affPro.getWebsite(),
                            ProductStatus.ONSALE)
            );
        }
    }

    private static boolean nearPrice(float stdPrice, float price) {

        if (stdPrice == 0) {
            return true;
        }

        if (stdPrice * 0.5 < price && price < stdPrice * 1.5) {
            return true;
        }

        return false;
    }

    public static float stringMatch(String s1, String s2) {

        s1 = StringUtils.toLowerCase(s1);
        s2 = StringUtils.toLowerCase(s2);

        String[] ss1 = StringUtils.getCleanWords(s1);
        String[] ss2 = StringUtils.getCleanWords(s2);

        return StringUtils.wordsMatchD(ss1, ss2);
    }

    public void cleanProducts(SrmAutoSearchResult searchResult) {
        Map<Website, List<SearchedSku>> searchedSkusMap = new LinkedHashMap<Website, List<SearchedSku>>();

        Comparator comparator = new Comparator<SearchedSku>() {
            @Override
            public int compare(SearchedSku p1, SearchedSku p2) {
                float score1 = p1.getTitleScore();
                float score2 = p2.getTitleScore();

                if (score1 > score2) {
                    return -1;
                } else if (score1 < score2) {
                    return 1;
                } else if (score1 == score2) {
                    float priceScore1 = p1.getPriceScore();
                    float priceScore2 = p2.getPriceScore();

                    if (priceScore1 == 0 && priceScore2 == 0) {
                        return 0;
                    } else {
                        if (priceScore1 > priceScore2) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
                return 0;
            }
        };

        Map<Website, List<ListProduct>> listProductMap = searchResult.getSitePros();

        String keyword = searchResult.getTitle();
        Website logSite = Website.valueOf(searchResult.getFromWebsite());

        float stdPrice = 0;//searchResult.getPrice();
        float maxTitleScore = 0;

        List<ListProduct> logPros = listProductMap.get(logSite);
        if (ArrayUtils.hasObjs(logPros)) {
            for (ListProduct lp : logPros) {
                float titleScore = stringMatch(lp.getTitle(), keyword);
                if (maxTitleScore < titleScore) {
                    maxTitleScore = titleScore;
                    stdPrice = lp.getPrice();
                }
            }
        }

        if (maxTitleScore != 1) {
            // 源网站如果没找到完全匹配的，不抓
            searchResult.setFinalSkus(searchedSkusMap);
            mdm.save(searchResult);
            return;
        }

        for (Map.Entry<Website, List<ListProduct>> kv : listProductMap.entrySet()) {
            Website website = kv.getKey();
            List<ListProduct> products = kv.getValue();
            List<SearchedSku> searchedSkus = new ArrayList<SearchedSku>();

            for (ListProduct lp : products) {
                float titleScore = stringMatch(lp.getTitle(), keyword);
                float priceScore = 0.0f;
                if (stdPrice > 0) {
                    priceScore = Math.abs(stdPrice - lp.getPrice()) / stdPrice;
                }

//                if (titleScore < 0.5 || priceScore > 0.5) {
//                    logger.debug(String.format("title/price:[%s/%f].titleScore/priceScore:[%f/%f]", lp.getTitle(), lp.getPrice(), titleScore, priceScore));
//                    continue;
//                }

                searchedSkus.add(
                        new SearchedSku(lp.getWebsite(), lp.getTitle(),
                                titleScore, lp.getPrice(), priceScore,
                                lp.getSourceId(), lp.getUrl(),
                                lp.getImageUrl(), lp.getStatus())
                );
            }

            if (ArrayUtils.hasObjs(searchedSkus)) {
                logger.debug(String.format("Get [%d] skus from [%s]", searchedSkus.size(), website.name()));
                Collections.sort(searchedSkus, comparator);
                searchedSkusMap.put(website, searchedSkus);
            }
        }

        searchResult.setFinalSkus(searchedSkusMap);

        mdm.save(searchResult);
    }

    public void searchProductsFromSites(SrmAutoSearchResult searchResult) {

        //String keyword = searchResult.getTitle();
        //Website logSite = Website.valueOf(searchResult.getFromWebsite());

        //Map<Website, List<ListProduct>> listProductMap = new HashMap<Website, List<ListProduct>>();

        // voodoo - fuck voodoo - return durex
//        VoodooHelper.getProductsFromVoodoo(listProductMap, keyword);

        // read by affiliate api - size always null
//        getProductsFromAffiliate(listProductMap, keyword);

        // read from html
        //getProductsFromWebsite(listProductMap, keyword);

        // msp
//        getProductsFromMSP(listProductMap, keyword);

        //searchResult.setSitePros(listProductMap);
        logger.info("job result info ：{}",searchResult.toString());
        mdm.save(searchResult);
    }
}
