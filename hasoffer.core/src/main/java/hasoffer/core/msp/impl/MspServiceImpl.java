package hasoffer.core.msp.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.*;
import hasoffer.core.persistence.po.msp.updater.MspCategoryUpdater;
import hasoffer.core.persistence.po.msp.updater.MspProductJobUpdater;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.thd.msp.ThdMspProduct;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.fetch.model.ProductJob;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceCmpSku;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by chevy on 2015/12/7.
 */
@Service
public class MspServiceImpl implements IMspService {

    private static final String Q_PRODUCT_JOB_BY_SOURCEID =
            "SELECT t FROM MspProductJob t " +
                    " WHERE t.sourceId = ?0 ";
    private static final String Q_PRODUCT_JOB_BY_PTMPRODUCT_ID =
            "SELECT t FROM MspProductJob t " +
                    " WHERE t.ptmProductId = ?0 ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    ProductIndexServiceImpl mspIndexService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IProductService productService;
    private Logger logger = LoggerFactory.getLogger(MspServiceImpl.class);

    @Override
    public MspProductJob findJobByPtmProductId(long ptmProductId) {
        return dbm.querySingle(Q_PRODUCT_JOB_BY_PTMPRODUCT_ID, Arrays.asList(ptmProductId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobProcessTime(long jobId, Date time) {
        MspProductJobUpdater mspProductJobUpdater = new MspProductJobUpdater(jobId);
        mspProductJobUpdater.getPo().setProcessTime(time);
        dbm.update(mspProductJobUpdater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductByMspProduct(long ptmProductId, MySmartPriceProduct mspp) {
        // 查询图片是否存在，如果没存在，则创建image记录
        List<String> imageUrls = productService.getProductImageUrls(ptmProductId);
        if (ArrayUtils.isNullOrEmpty(imageUrls)) {
            if (ArrayUtils.hasObjs(mspp.getImageUrls())) {
                for (String imageUrl : mspp.getImageUrls()) {
                    PtmImage ptmImage = new PtmImage(ptmProductId, imageUrl);
                    dbm.create(ptmImage);
                }
            }
        }

        PageableResult<PtmCmpSku> pageableResult = productService.listPagedCmpSkus(ptmProductId, 1, Integer.MAX_VALUE);

        Map<Website, PtmCmpSku> ptmCmpSkuMap = new HashMap<Website, PtmCmpSku>();

        List<PtmCmpSku> ptmCmpSkus = pageableResult.getData();
        if (ArrayUtils.hasObjs(ptmCmpSkus)) {
            for (PtmCmpSku ptmCmpSku : ptmCmpSkus) {
                ptmCmpSkuMap.put(ptmCmpSku.getWebsite(), ptmCmpSku);
            }
        }

        // 更新相关的比价信息
        List<MySmartPriceCmpSku> cmpSkus = mspp.getCmpSkus();
        if (ArrayUtils.hasObjs(cmpSkus)) {
            for (MySmartPriceCmpSku mspCmpsku : cmpSkus) {
                if(mspCmpsku.getWebsite()==null){
                    continue;
                }
                PtmCmpSku ptmCmpSku = ptmCmpSkuMap.get(mspCmpsku.getWebsite());
                if (ptmCmpSku == null) {
                    PtmCmpSku sku = new PtmCmpSku(ptmProductId, mspCmpsku);
                    cmpSkuService.createCmpSku(sku);
                } else {
                    // 更新相关sku
                    if (StringUtils.isEmpty(ptmCmpSku.getOriUrl())) {
                        PtmCmpSkuUpdater cmpSkuUpdater = new PtmCmpSkuUpdater(ptmCmpSku.getId());
                        cmpSkuUpdater.getPo().setOriUrl(mspCmpsku.getUrl());
                        dbm.update(cmpSkuUpdater);
                    }

                    // 如果存在则查看价格是否一致
                    if (ptmCmpSku.getPrice() != mspCmpsku.getPrice()) {
                        //先忽略
                        /*try {
                            // try update sku
							String url = ptmCmpSku.getUrl();
							Website website = WebsiteHelper.getWebSite(url);

							IPriceProcessor priceProcessor = WebsitePriceProcessorFactory.getProcessor(website);

							if (priceProcessor == null) {
								return;
							}

							Price newPrice = priceProcessor.getPirce(url);
							productService.updateSku(ptmCmpSku.getId(), newPrice.getPrice(), website);
						} catch (Exception e) {
							logger.error(e.toString());
						}*/
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void relateCategory(Long id, PtmCategory ptmCategory) {
        MspCategoryUpdater mspCategoryUpdater = new MspCategoryUpdater(id);
        mspCategoryUpdater.getPo().setPtmCategoryId(ptmCategory.getId());
        dbm.update(mspCategoryUpdater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(long id, int proCount) {
        MspCategoryUpdater updater = new MspCategoryUpdater(id);
        updater.getPo().setProCount(proCount);
        dbm.update(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCategory(MspCategory mspCategory) {
        dbm.create(mspCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MspCategory saveCategory(long parentId, String name, String url, String imageUrl, String groupName) {
        MspCategory mspCategory = new MspCategory(parentId, name, url, imageUrl, groupName);
        saveCategory(mspCategory);
        return mspCategory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProductJobs(Set<ProductJob> productJobs) {
        for (ProductJob proJob : productJobs) {
            //判断该job是否已经存在了
            List<ProductJob> pjobs = dbm.query(Q_PRODUCT_JOB_BY_SOURCEID, Arrays.asList(proJob.getSourceId()));
            if (ArrayUtils.hasObjs(pjobs)) {
                logger.info("Job exists.");
                return;
            }
            MspProductJob mpj = new MspProductJob(proJob.getCategoryId(), proJob.getSourceId(), proJob.getSourceUrl());
            dbm.create(mpj);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(long jobId, long ptmCategoryId, MySmartPriceProduct mySmartPriceProduct) {
        MspCategory mspCategory = dbm.get(MspCategory.class, ptmCategoryId);

        PtmProduct ptmProduct = productService.createProduct(mspCategory.getPtmCategoryId(),
                mySmartPriceProduct.getTitle(),
                mySmartPriceProduct.getPrice(),
                mySmartPriceProduct.getDescription(),
                StringUtils.arrayToString(mySmartPriceProduct.getColors()),
                StringUtils.arrayToString(mySmartPriceProduct.getSizes()),
                mySmartPriceProduct.getRating(),
                "MYSMARTPRICE",
                mySmartPriceProduct.getMspId());

        long ptmProId = ptmProduct.getId();

        List<String> imageUrls = mySmartPriceProduct.getImageUrls();
        if (ArrayUtils.hasObjs(imageUrls)) {
            for (String imageUrl : imageUrls) {
                PtmImage ptmImage = new PtmImage(ptmProId, imageUrl);
                dbm.create(ptmImage);
            }
        }

        List<String> features = mySmartPriceProduct.getFeatures();
        if (ArrayUtils.hasObjs(features)) {
            for (String feature : features) {
                PtmFeature ptmFeature = new PtmFeature(ptmProId, feature);
                dbm.create(ptmFeature);
            }
        }

        Map<String, Map<String, String>> baseAttrs = mySmartPriceProduct.getBaseAttrs();
        if (baseAttrs != null) {
            for (Map.Entry<String, Map<String, String>> kv : baseAttrs.entrySet()) {
                String group = kv.getKey();
                Map<String, String> vals = kv.getValue();
                for (Map.Entry<String, String> valkv : vals.entrySet()) {
                    String key = valkv.getKey();
                    String val = valkv.getValue();
                    PtmBasicAttribute pba = new PtmBasicAttribute(ptmProId, key, val, group);
                    dbm.create(pba);
                }
            }
        }

        List<MySmartPriceCmpSku> cmpSkus = mySmartPriceProduct.getCmpSkus();
        if (!ArrayUtils.isNullOrEmpty(cmpSkus)) {
            for (MySmartPriceCmpSku cmpSku : cmpSkus) {
                PtmCmpSku sku = new PtmCmpSku(ptmProId, cmpSku);
                cmpSkuService.createCmpSku(sku);
            }
        }

        MspProductJobUpdater mspProductJobUpdater = new MspProductJobUpdater(jobId);
        mspProductJobUpdater.getPo().setPtmProductId(ptmProId);
        dbm.update(mspProductJobUpdater);

        productService.importProduct2Solr(ptmProduct);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveMspProduct(long jobId, long categoryId, MySmartPriceProduct mySmartPriceProduct) {
        //long jobId, String title, float ptm, String color, String size, int rating, String url

        MspProduct product = new MspProduct(jobId, categoryId,
                mySmartPriceProduct.getTitle(),
                mySmartPriceProduct.getPrice(),
                StringUtils.arrayToString(mySmartPriceProduct.getColors()),
                StringUtils.arrayToString(mySmartPriceProduct.getSizes()),
                mySmartPriceProduct.getRating(),
                mySmartPriceProduct.getUrl(),
                mySmartPriceProduct.getDescription(),
                StringUtils.arrayToString(mySmartPriceProduct.getFeatures(), "<br>"));

        dbm.create(product);

        long productId = product.getId();

        List<String> imageUrls = mySmartPriceProduct.getImageUrls();
        if (!ArrayUtils.isNullOrEmpty(imageUrls)) {
            for (String imageUrl : imageUrls) {
                MspImage mspImage = new MspImage(productId, imageUrl);
                dbm.create(mspImage);
            }
        }

//		mspProduct.getBaseAttrs()
        Map<String, Map<String, String>> baseAttrs = mySmartPriceProduct.getBaseAttrs();
        if (baseAttrs != null) {
            for (Map.Entry<String, Map<String, String>> kv : baseAttrs.entrySet()) {
                String group = kv.getKey();
                Map<String, String> vals = kv.getValue();
                for (Map.Entry<String, String> valkv : vals.entrySet()) {
                    String key = valkv.getKey();
                    String val = valkv.getValue();
                    MspProductAttribute mpa = new MspProductAttribute(productId, key, val, group);
                    dbm.create(mpa);
                }
            }
        }

        List<MySmartPriceCmpSku> cmpSkus = mySmartPriceProduct.getCmpSkus();
        if (!ArrayUtils.isNullOrEmpty(cmpSkus)) {
            for (MySmartPriceCmpSku cmpSku : cmpSkus) {
                MspCmpSku sku = new MspCmpSku(productId, cmpSku);
                dbm.create(sku);
            }
        }
    }

    @Override
    public void saveUncmpProduct(ThdMspProduct productJob) {
        this.dbm.create(productJob);
    }
}
