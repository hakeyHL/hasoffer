package hasoffer.core.product;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.core.bo.product.ProductBo;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.ptm.updater.PtmProductUpdater;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.fetch.model.ListProduct;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface IProductService {

    //暂时使用，回头91mobile抓完删掉
    PtmProduct createPtmproduct(PtmProduct ptmproduct);

    PtmCmpSku createPtmcmpsku(PtmCmpSku ptmCmpsku);

    PageableResult<PtmProduct> listProductsByCreateTime(Date fromDate, int page, int size);

    @Deprecated
    PtmProduct createProduct(long categoryId, String title, float price,
                             String description, String colors, String sizes,
                             int rating, String website, String sourceId);

    ProductBo createProduct(long cateId, String title, float price, Website website,
                            String sourceUrl, String sourceId, String imageUrl);

    ProductBo createProductByListProducts(Map<Website, ListProduct> listProductMap);

    PageableResult<PtmCmpSku> listPagedCmpSkus(long proId, int pageNum, int pageSize);

    List<PtmProduct> listProducts(long cateId, int page, int size);

    PageableResult<PtmProduct> listPagedProducts(int page, int size);

    PageableResult<PtmProduct> listPagedProducts(long cateId, int page, int size);

    List<PtmProduct> getProducts(List<Long> proIds);

    List<PtmTopSelling> getTopSellings(Long yesterdayStart, Long todayStart, int page, int size);

    String getProductMasterImageUrl(Long id);

    List<String> getProductImageUrls(Long id);

    PtmProduct getProduct(long proId);

    boolean updateProductTag(String proId, String tag);

    void updateProductCategory(PtmProduct product, Long targetCate);

    void deleteProduct(long ptmProductId);

    ProductBo getProductBo(long proId);

    PtmImage getProductMasterImage(Long id);

    void expTopSellingsFromSearchCount(String ymd);

    void updateProductImage2(Long id, String oriImageUrl);

    /**
     * 该方法用来更新product的价格信息
     *
     * @param id
     * @return 商品价格被更新了返回true，其他情况false
     */
    void updatePtmProductPrice(long id);

    void updatePtmProdcutWebsite(long id, Website website);

    void updateProductStd(Long proId, boolean std);

    void updateProductBrand(long proId, String productBrand);

    void updateProductBrandModel(long proId, String productBrand, String modelName);

    void updateProduct(PtmProductUpdater ptmProductUpdater);

    // sku todo 迁移到 ICmpSkuService
    PageableResult<PtmCmpSku> listNotOffSaleCmpSkus(long proId, int page, int size);

    PageableResult<PtmCmpSku> listOnsaleCmpSkus(long proId, int page, int size);

    void updateSku(long skuId, String url);

    PtmCmpSku createCmpsku(long productId, float price, String url, String title, String imageUrl);

    PtmCmpSku createCmpsku(long ptmProductId, float price, String url, String title, String imageUrl, String deeplink);

    // solr
    ProductModel2 getProductModel2(PtmProduct product);

    void importProduct2Solr2(long proId);

    void importProduct2Solr2(PtmProduct o, List<PtmCmpSku> cmpSkus);

    void importProduct2Solr2(PtmProduct product);

    void importProduct2SolrByCategory(long cateId);

    List<String> spellcheck(String text);

    void saveImage222(PtmImage2 image2);
}