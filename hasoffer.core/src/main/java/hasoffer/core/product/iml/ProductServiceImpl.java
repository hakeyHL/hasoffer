package hasoffer.core.product.iml;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.product.ProductBo;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.ptm.updater.PtmProductUpdater;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel;
import hasoffer.core.utils.ImageUtil;
import hasoffer.data.solr.*;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.ListProduct;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service
public class ProductServiceImpl implements IProductService {
    private static final String Q_PRODUCT =
            "SELECT t FROM PtmProduct t";
    private static final String Q_PRODUCT_BY_CATEGORY =
            "SELECT t FROM PtmProduct t WHERE t.categoryId = ?0";
    private static final String Q_PRODUCT_BY_CMPID =
            "SELECT t FROM PtmProduct t " +
                    " WHERE t.id > ?0 ";
    private static final String Q_PRODUCT_BY_CREATETIME =
            "SELECT t FROM PtmProduct t " +
                    " WHERE t.createTime > ?0 " +
                    "   AND t.sourceSite <> 'MYSMARTPRICE' " +
                    " ORDER BY t.createTime ASC ";

    private static final String Q_PTM_CMPSKU =
            "SELECT t FROM PtmCmpSku t " +
                    " WHERE t.productId = ?0   " +
                    " ORDER BY t.price ASC ";

    private static final String Q_ONSALE_PTM_CMPSKU =
            "SELECT t FROM PtmCmpSku t " +
                    " WHERE t.productId = ?0 " +
                    "   AND t.status = 'ONSALE'  " +
                    " ORDER BY t.price ASC ";

    private static final String Q_PTM_FEATURE =
            "SELECT t.feature FROM PtmFeature t " +
                    " WHERE t.productId = ?0   ";
    private static final String Q_PTM_IMAGE =
            "SELECT t FROM PtmImage t " +
                    " WHERE t.productId = ?0  ";
    private static final String Q_PTM_BASICATTRIBUTE =
            "SELECT t FROM PtmBasicAttribute t " +
                    " WHERE t.productId = ?0 ";

    private static final String Q_PTM_GETTOPPRODUCTS =
            "SELECT t from PtmProduct t where t.id in (SELECT srm.productId from SrmSearchCount srm " +
                    " where srm.ymd=?0 ORDER BY srm.count DESC)";

    private final static String CACHE_KEY = "product";
    @Resource
    ProductIndexServiceImpl productIndexService;
    @Resource
    ICategoryService categoryService;
    private Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Resource
    private IDataBaseManager dbm;
    @Resource
    private ICmpSkuService cmpSkuService;

    @Override
    public PageableResult<PtmProduct> listProductsByCreateTime(Date fromDate, int page, int size) {
        return dbm.queryPage(Q_PRODUCT_BY_CREATETIME, page, size, Arrays.asList(fromDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductBo createProductByListProducts(Map<Website, ListProduct> listProductMap) {

        long cateId = 0L;
        ProductBo pro = null;

        for (Map.Entry<Website, ListProduct> kv : listProductMap.entrySet()) {
            Website website = kv.getKey();

            ListProduct listProduct = kv.getValue();

            if (pro == null) {
                pro = createProduct(cateId, listProduct.getTitle(), listProduct.getPrice(),
                        website, listProduct.getUrl(), listProduct.getSourceId(), listProduct.getImageUrl());
            } else {
                PtmCmpSku cmpSku = createCmpsku(pro.getId(), listProduct.getPrice(),
                        listProduct.getUrl(), listProduct.getTitle(), listProduct.getImageUrl());
                pro.getCmpSkus().add(cmpSku);
            }

        }

        return pro;
    }

    @Override
    public PtmCmpSku createCmpsku(long ptmProductId, float price, String url, String title, String imageUrl, String deeplink) {
        logger.debug(String.format("create cmp sku : %s", title));

        PtmCmpSku cmpSku = new PtmCmpSku(ptmProductId, price, url, title, imageUrl, deeplink);

        cmpSkuService.createCmpSku(cmpSku);

        return cmpSku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PtmCmpSku createCmpsku(long productId, float price, String url, String title, String imageUrl) {
        logger.debug(String.format("create cmp sku : %s", title));

        PtmCmpSku cmpSku = new PtmCmpSku(productId, price, url, title, imageUrl);

        cmpSkuService.createCmpSku(cmpSku);

        return cmpSku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductCategory(PtmProduct product, Long targetCate) {

        PtmProductUpdater productUpdater = new PtmProductUpdater(product.getId());
        productUpdater.getPo().setCategoryId(targetCate);
        dbm.update(productUpdater);

        product.setCategoryId(targetCate);
        importProduct2Solr(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProductTag(String proId, String tag) {

        long proIdl = Long.valueOf(proId);

        PtmProduct product = dbm.get(PtmProduct.class, proIdl);

        if (product == null) {
            return false;
        }

        PtmProductUpdater ptmProductUpdater = new PtmProductUpdater(proIdl);
        ptmProductUpdater.getPo().setTag(tag);
        dbm.update(ptmProductUpdater);

        product.setTag(tag);
        importProduct2Solr(product);

        return true;
    }

    @Override
//    @Cacheable(value = CACHE_KEY, key = "#root.methodName + '_' + #root.args[0]")
    public PtmProduct getProduct(long proId) {
        return dbm.get(PtmProduct.class, proId);
    }

    @Override
    public List<PtmBasicAttribute> getProductBasicAttributes(long id) {
        return dbm.query(Q_PTM_BASICATTRIBUTE, Arrays.asList(id));
    }

    @Override
    public List<String> getProductFeatures(long id) {
        return dbm.query(Q_PTM_FEATURE, Arrays.asList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSku(long skuId, String url) {
        PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(skuId);

        Website website = WebsiteHelper.getWebSite(url);
        ptmCmpSkuUpdater.getPo().setWebsite(website);
        ptmCmpSkuUpdater.getPo().setUrl(url);

        dbm.update(ptmCmpSkuUpdater);
    }

    @Override
    public List<String> getProductImageUrls(Long id) {
        List<PtmImage> images = dbm.query(Q_PTM_IMAGE, Arrays.asList(id));
        List<String> imageUrls = new ArrayList<String>();

        if (ArrayUtils.hasObjs(images)) {
            // 优先从服务器取图片
            for (PtmImage image : images) {
                if (StringUtils.isEmpty(image.getPath2())) {
                    imageUrls.add(ImageUtil.getImage3rdUrl(image.getImageUrl()));
                } else {
                    imageUrls.add(ImageUtil.getImageUrl(image.getPath2()));
                }
            }
        }

        return imageUrls;
    }

    @Override
//    @Cacheable(value = CACHE_KEY, key = "#root.methodName + '_' + #root.args[0]")
    public String getProductMasterImageUrl(Long id) {
        List<String> imageUrls = getProductImageUrls(id);
        return ArrayUtils.hasObjs(imageUrls) ? imageUrls.get(0) : "";
    }

    @Override
    public List<PtmProduct> getProducts(List<Long> proIds) {

        List<PtmProduct> products = new ArrayList<PtmProduct>();
        if (ArrayUtils.isNullOrEmpty(proIds)) {
            return products;
        }

        for (Long id : proIds) {
            if (id <= 0) {
                continue;
            }
            PtmProduct product = dbm.get(PtmProduct.class, id);
            if (product != null) {
                products.add(product);
            }
        }

        return products;
    }

    @Override
    public List<PtmProduct> getTopSellingProductsByDate(String date,int page,int size) {
        return dbm.query(Q_PTM_GETTOPPRODUCTS,page,size,Arrays.asList(date));
    }

    @Override
    public List<PtmProduct> listProducts(long cateId, int page, int size) {
        return dbm.query(Q_PRODUCT_BY_CATEGORY, page, size, Arrays.asList(cateId));
    }

    @Override
    public PageableResult<PtmProduct> listPagedProducts(int page, int size) {
        return dbm.queryPage(Q_PRODUCT, page, size);
    }

    @Override
    public PageableResult<PtmProduct> listPagedProducts(long cateId, int page, int size) {
        return dbm.queryPage(Q_PRODUCT_BY_CATEGORY, page, size, Arrays.asList(cateId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PtmProduct createProduct(long categoryId, String title, float price,
                                    String description, String colors, String sizes,
                                    int rating, String website, String sourceId) {
        PtmProduct ptmProduct = new PtmProduct(categoryId,
                title,
                price,
                description,
                colors,
                sizes,
                rating,
                website,
                sourceId);
        dbm.create(ptmProduct);
        return ptmProduct;
    }

    /**
     * 删除商品操作
     * 一般情况不要使用
     * 删除商品：删除ptmimage ptmcmpsku ptmproduct ， 目前不涉及 ptmsku
     *
     * @param ptmProductId
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteProduct(long ptmProductId) {
        logger.debug("delete product : " + ptmProductId);

        List<PtmCmpSku> cmpSkus = dbm.query("select t from PtmCmpSku t where t.productId = ?0", Arrays.asList(ptmProductId));

        List<PtmImage> images = dbm.query("select t from PtmImage t where t.productId = ?0", Arrays.asList(ptmProductId));

        if (ArrayUtils.hasObjs(cmpSkus)) {
            for (PtmCmpSku cmpSku : cmpSkus) {
                dbm.delete(PtmCmpSku.class, cmpSku.getId());
            }
        }

        if (ArrayUtils.hasObjs(images)) {
            for (PtmImage image : images) {
                dbm.delete(PtmImage.class, image.getId());
            }
        }

        dbm.delete(PtmProduct.class, ptmProductId);

        productIndexService.remove(String.valueOf(ptmProductId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProductBo createProduct(long cateId, String title, float price,
                                   Website website, String sourceUrl, String sourceId, String imageUrl) {
        logger.debug(String.format("create product : %s", title));

        //创建 PtmProduct
        PtmProduct product = new PtmProduct(cateId, title, price, website.name(), sourceUrl, sourceId);
        dbm.create(product);

        //创建 图片
        PtmImage ptmImage = new PtmImage(product.getId(), imageUrl);
        dbm.create(ptmImage);

        //features
        //basic attrs

        //关联 cmp sku
//        PtmCmpSku cmpSku = new PtmCmpSku(product.getId(), price, sourceUrl, title, imageUrl);
//        cmpSku.setChecked(true);
//        cmpSkuService.createCmpSku(cmpSku);

//        List skuList = new ArrayList();
//        skuList.add(cmpSku);

        importProduct2Solr(product);

        return new ProductBo(product, new ArrayList<PtmCmpSku>(), ptmImage);
    }

    @Override
    public void reimport2Solr(boolean removeFirst) {
        if (removeFirst) {
            try {
                productIndexService.removeAll();
            } catch (SolrServerException e) {
                logger.error("{}", e);
            } catch (IOException e) {
                logger.error("{}", e);
            }
        }

        addOrUpdateSolr(Q_PRODUCT, null);
    }

    @Override
    public void importProduct2Solr(PtmProduct product) {
        List<String> features = getProductFeatures(product.getId());

//        PtmCategory category = dbm.get(PtmCategory.class, product.getCategoryId());
        List<PtmCategory> categories = categoryService.getRouterCategoryList(product.getCategoryId());

        long cate1 = 0L, cate2 = 0L, cate3 = 0L;
        String cate3name = "";

        if (ArrayUtils.hasObjs(categories) && categories.size() == 3) {
            cate1 = categories.get(0).getId();
            cate2 = categories.get(1).getId();
            PtmCategory category = categories.get(2);
            cate3 = category.getId();
        }

        ProductModel productModel = new ProductModel(product.getId(),
                product.getTitle(),
                product.getTag(),
                cate3,
                cate3name,
                product.getPrice(),
                StringUtils.arrayToString(features),
                product.getDescription(),
                product.getColor(),
                product.getSize(),
                product.getRating(),
                cate1,
                cate2,
                cate3);

        productIndexService.createOrUpdate(productModel);
    }

    private void addOrUpdateSolr(final String queryString, List params) {
        final int PAGE_SIZE = 500;
        int pageNum = 1;

        PageableResult<PtmProduct> pagedProducts = null;

        if (ArrayUtils.isNullOrEmpty(params)) {
            pagedProducts = dbm.queryPage(queryString, pageNum, PAGE_SIZE);
        } else {
            pagedProducts = dbm.queryPage(queryString, pageNum, PAGE_SIZE, params);
        }

        int pageCount = (int) pagedProducts.getTotalPage();
        List<PtmProduct> products = pagedProducts.getData();

        while (pageNum <= pageCount) {

            logger.debug(String.format("PAGE: %d / %d", pageNum, pageCount));

            if (pageNum > 1) {
                products = dbm.query(queryString, pageNum, PAGE_SIZE);
            }

            if (ArrayUtils.isNullOrEmpty(products)) {
                continue;
            } else {
                for (PtmProduct product : products) {
                    importProduct2Solr(product);
                }
            }

            pageNum++;
        }
    }

    @Override
    public void append2Solr() {
        FilterQuery[] fqs = null;
        Sort[] sorts = new Sort[]{
                new Sort("id", Order.DESC)
        };
        PivotFacet[] pivotFacets = null;

        SearchResult<Long> sr = productIndexService.search("*", fqs, sorts, pivotFacets, 1, 1);

        long maxId = sr.getResult().get(0);

        addOrUpdateSolr(Q_PRODUCT_BY_CMPID, Arrays.asList(maxId));
    }

    @Override
//    @Cacheable(value = CACHE_KEY, key = "#root.methodName + '_' + #root.args[0] + #root.args[1] + #root.args[2]")
    public PageableResult<PtmCmpSku> listPagedCmpSkus(long proId, int pageNum, int pageSize) {
        PageableResult<PtmCmpSku> pagedResult = dbm.queryPage(Q_PTM_CMPSKU, pageNum, pageSize, Arrays.asList(proId));
        return pagedResult;
    }

    @Override
    public PageableResult<PtmCmpSku> listOnsaleCmpSkus(long proId, int page, int size) {
        PageableResult<PtmCmpSku> pagedResult = dbm.queryPage(Q_ONSALE_PTM_CMPSKU, page, size, Arrays.asList(proId));
        return pagedResult;
    }
}