package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.*;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.search.ISearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by chevy on 2015/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ProductTest {

    private static final String Q_PRODUCT_BY_CATEGORY =
            "SELECT t FROM PtmProduct t WHERE t.categoryId = ?0";
    private static final String Q_PRODUCT_ID =
            "SELECT COUNT(t.id),t.productId FROM PtmCmpSku t " +
                    "WHERE t.productId > 0 " +
                    "GROUP BY t.productId " +
                    "HAVING COUNT(t.id)>50 " +
                    "ORDER BY COUNT(t.id) DESC";
    @Resource
    ProductIndexServiceImpl productIndexService;
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;
    @Resource
    IProductService productService;
    @Resource
    IImageService imageService;
    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    ISearchService searchService;
    @Resource
    IFetchService fetchService;
    private Pattern PATTERN_IN_WORD = Pattern.compile("[^0-9a-zA-Z\\-]");

    private Logger logger = LoggerFactory.getLogger(ProductTest.class);

    @Test
    public void testCmpskuSolr() {
        PageableResult<CmpSkuModel> pagedResults = cmpskuIndexService.searchSku("Nike Kobe Mamba", 1, 5);

        List<CmpSkuModel> cmpSkuModels = pagedResults.getData();
        for (CmpSkuModel cmpSkuModel : cmpSkuModels) {
            System.out.println(cmpSkuModel.getTitle());
        }
    }

    @Test
    public void downloadskuimage() {
        PtmCmpSku sku = dbm.get(PtmCmpSku.class, 1L);
        cmpSkuService.downloadImage(sku);
    }

    @Test
    public void stat() {
        long cateId = 459;

        int page = 1, PAGE_SIZE = 500;

        PageableResult<PtmProduct> pagedProducts = productService.listPagedProducts(cateId, 1, 500);

        List<PtmProduct> products = pagedProducts.getData();
        long totalPage = pagedProducts.getTotalPage();
//        totalPage = 5;
        Map<String, Long> statMap = new HashMap<String, Long>();

        long productCount = 0;
        while (page <= totalPage) {
            if (page > 1) {
                products = dbm.query(Q_PRODUCT_BY_CATEGORY, page, PAGE_SIZE, Arrays.asList(cateId));
            }
            if (ArrayUtils.hasObjs(products)) {
                for (PtmProduct product : products) {
                    analysis(statMap, product.getTitle());
                    productCount++;
                }
            }
            page++;
        }

        double t = productCount * 0.1;
        for (Map.Entry<String, Long> kv : statMap.entrySet()) {
            if (kv.getValue() <= t) {
                continue;
            }
            System.out.println(kv.getKey() + "\t\t" + kv.getValue());
        }

        System.out.println("products : " + productCount + ",total found products : " + pagedProducts.getNumFund());
    }

    @Test
    public void createCateIndex() {
        List<PtmCategory> cates = categoryService.listSubCategories(0L);
        for (PtmCategory cate : cates) {
            System.out.println(cate.toString());

            List<PtmCategory> subCates = categoryService.listSubCategories(cate.getId());

            for (PtmCategory subCate : subCates) {
                System.out.println("----" + subCate.toString());
                String keys = statForSolr(subCate.getId(), subCate.getLevel());
                if (!StringUtils.isEmpty(keys)) {
                    categoryService.updateCategoryIndex(subCate.getId(), keys);
                }
            }
        }
    }

    @Test
    public void countCate() {
        List<PtmCategory> cates = categoryService.listSubCategories(0L);
        int count1 = 0, count2 = 0;
        for (PtmCategory cate : cates) {
            List<PtmCategory> subCates = categoryService.listSubCategories(cate.getId());

            for (PtmCategory subCate : subCates) {
                if (StringUtils.isEmpty(subCate.getKeyword())) {
                    System.out.println(subCate);
                    count2++;
                } else {
                    count1++;
                }

            }
        }

        System.out.println(count1 + "\t" + count2);
    }

    @Test
    public void statForSolr() {
        statForSolr(39, 2);
    }

    public String statForSolr(long cateId, int level) {

        int page = 1, PAGE_SIZE = 500;
        PageableResult<Long> pagedIds = productIndexService.searchPro(cateId, level, null, page, PAGE_SIZE);
        List<PtmProduct> products = productService.getProducts(pagedIds.getData());

        long totalPage = pagedIds.getTotalPage();

        Map<String, Long> statMap = new HashMap<String, Long>();

        long productCount = 0;
        while (page <= totalPage) {
            if (page > 1) {
                pagedIds = productIndexService.searchPro(cateId, level, null, page, PAGE_SIZE);
                products = productService.getProducts(pagedIds.getData());
            }
            if (ArrayUtils.hasObjs(products)) {
                for (PtmProduct product : products) {
                    analysis(statMap, product.getTitle());
                    productCount++;
                }
            }
            page++;
        }

        double t = productCount * 0.05;
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Long> kv : statMap.entrySet()) {
            if (kv.getValue() <= t) {
                continue;
            }
            sb.append(kv.getKey()).append(" ");
            System.out.println(kv.getKey() + "\t\t" + kv.getValue());
        }

        System.out.println("category: " + cateId + ", products : " + productCount + ",total found products : " + pagedIds.getNumFund());
        return sb.toString();
    }

    private void analysis(Map<String, Long> statMap, String title) {
        String[] words = title.split(" ");
        for (String w : words) {
            w = w.toLowerCase().trim();
            if (PATTERN_IN_WORD.matcher(w).find()) {
//                System.out.println(w);
                continue;
            }
            Long count = statMap.get(w);
            if (count == null) {
                count = new Long(1);
                statMap.put(w, count);
            } else {
                count++;
                statMap.put(w, count);
            }
        }
    }

    @Test
    public void solr() {
        productService.reimport2Solr(true);
    }

    @Test
    public void image() {
        PtmImage image = dbm.get(PtmImage.class, 20L);

        boolean t = imageService.downloadImage(image);

        t = false;
    }

    @Test
    public void f() {
        String title = "LG 43LF6300 10cm(43) Full HD Smart LED Television";
        title = title.replaceAll("\\s+", " ");
        String key0 = title;
        String key1 = "";
        if (!StringUtils.isEmpty(title)) {
            String[] ts = title.split(" ");
            if (ts.length > 3) {
                int index = title.indexOf(ts[2]) + ts[2].length();
                key0 = title.substring(0, index);
                key1 = title.substring(index + 1);
            }
        }
        System.out.println(key0);
        System.out.println(key1);
    }

    @Test
    public void getImage() {
        String url = "https://d1nfvnlhmjw5uh.cloudfront.net/8936-2-desktop-normal.jpg";
        PtmImage image = new PtmImage();
        image.setImageUrl(url);
        image.setId(123l);
        imageService.downloadImage(image);
    }
}
