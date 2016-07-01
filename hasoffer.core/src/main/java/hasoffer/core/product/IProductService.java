package hasoffer.core.product;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.core.bo.product.ProductBo;
import hasoffer.core.persistence.po.ptm.PtmBasicAttribute;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.fetch.model.ListProduct;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface IProductService {

    PageableResult<PtmProduct> listProductsByCreateTime(Date fromDate, int page, int size);

    @Deprecated
    PtmProduct createProduct(long categoryId, String title, float price,
                             String description, String colors, String sizes,
                             int rating, String website, String sourceId);

    ProductBo createProduct(long cateId, String title, float price, Website website,
                            String sourceUrl, String sourceId, String imageUrl);

    ProductBo createProductByListProducts(Map<Website, ListProduct> listProductMap);

    void reimport2Solr(boolean removeFirst);

    void append2Solr();

    PageableResult<PtmCmpSku> listPagedCmpSkus(long proId, int pageNum, int pageSize);

    List<PtmProduct> listProducts(long cateId, int page, int size);

    PageableResult<PtmProduct> listPagedProducts(int page, int size);

    PageableResult<PtmProduct> listPagedProducts(long cateId, int page, int size);

    List<PtmProduct> getProducts(List<Long> proIds);

    List<PtmProduct> getTopSellingProductsByDate(String date, int page, int size);

    List<String> getProductFeatures(long id);

    String getProductMasterImageUrl(Long id);

    List<String> getProductImageUrls(Long id);

    List<PtmBasicAttribute> getProductBasicAttributes(long id);

    void updateSku(long skuId, String url);

    void importProduct2Solr(PtmProduct product);

    PtmProduct getProduct(long proId);

    boolean updateProductTag(String proId, String tag);

    void updateProductCategory(PtmProduct product, Long targetCate);

    PtmCmpSku createCmpsku(long productId, float price, String url, String title, String imageUrl);

    PtmCmpSku createCmpsku(long ptmProductId, float price, String url, String title, String imageUrl, String deeplink);

    void deleteProduct(long ptmProductId);

    PageableResult<PtmCmpSku> listOnsaleCmpSkus(long proId, int page, int size);

    ProductBo getProductBo(long proId);

    PtmImage getProductMasterImage(Long id);
}