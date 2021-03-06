package hasoffer.core.product.impl;

import hasoffer.affiliate.model.FlipkartAttribute;
import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.base.model.ImagePath;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.stdsku.StdSkuAttr;
import hasoffer.core.bo.stdsku.StdSkuBo;
import hasoffer.core.bo.stdsku.StdSkuImage;
import hasoffer.core.bo.stdsku.StdSkuPrice;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmStdBrandCard;
import hasoffer.core.persistence.mongo.PtmStdSkuDescription;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.ptm.updater.PtmStdImageUpdater;
import hasoffer.core.product.IStdProductService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.spider.model.FetchedProductReview;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by chevy on 2016/8/12.
 */
@Service
public class StdProductServiceImpl implements IStdProductService {

    private static final String Q_ATTR_BY_NAME = "SELECT t from PtmStdAttrDef t where t.stdDefName = ?0";

    private static final String Q_SKU_PRICE = "select t from PtmStdPrice t where t.stdSkuId=?0 ";

    private static final String Q_SKU_ATTR = "select t from PtmStdSkuAttr t where t.stdSkuId=?0 ";

    private static final String Q_SKU_IMAGE = "select t from PtmStdImage t where t.stdSkuId=?0 ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;

    private PtmStdAttrDef findAttrDefByName(String name) {
        return dbm.querySingle(Q_ATTR_BY_NAME, Arrays.asList(name));
    }

    @Override
    public StdSkuBo findStdSku(long skuId) {

        PtmStdSku stdSku = dbm.get(PtmStdSku.class, skuId);

        if (stdSku == null) {
            return null;
        }

        List<StdSkuPrice> skuPrices = findSkuPriceInfo(skuId);
        Map<String, StdSkuAttr> attrs = findStdSkuAttr(skuId);
        List<StdSkuImage> stdImages = findImages(skuId);

        PtmStdSkuDetail stdSkuDetail = mdm.queryOne(PtmStdSkuDetail.class, skuId);

        return new StdSkuBo(stdSku, attrs, skuPrices, stdImages, stdSkuDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fixImage(long imageId) {

        PtmStdImageUpdater updater = new PtmStdImageUpdater(imageId);

        PtmStdImage ptmStdImage = dbm.get(PtmStdImage.class, imageId);

        String oriImageUrl = ptmStdImage.getOriImageUrl();

        String newImageUrl = oriImageUrl.replace("-thumb-", "-large-");

        System.out.println("oriUrl " + oriImageUrl);
        System.out.println("newUrl " + newImageUrl);

        updater.getPo().setOriImageUrl(newImageUrl);

        dbm.update(updater);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downLoadImage(long imageId) throws ImageDownloadOrUploadException {

        PtmStdImage ptmStdImage = dbm.get(PtmStdImage.class, imageId);

        PtmStdImageUpdater updater = new PtmStdImageUpdater(imageId);

        ImagePath imagePath = ImageUtil.downloadAndUpload2(ptmStdImage.getOriImageUrl());

        updater.getPo().setOriImagePath(imagePath.getOriginalPath());
        updater.getPo().setSmallImagePath(imagePath.getSmallPath());
        updater.getPo().setBigImagePath(imagePath.getBigPath());

        dbm.update(updater);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPtmStdSkuDescription(PtmStdSkuDescription ptmStdSkuDescription) {
        mdm.save(ptmStdSkuDescription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePtmStdSkuDescription(PtmStdSkuDescription newPtmStdSkuDescription, PtmStdSkuDescription oldPtmStdSkuDescription) {

        String newFeathers = newPtmStdSkuDescription.getFeatures();
        String newSummary = newPtmStdSkuDescription.getSummary();
        List<FetchedProductReview> newFetchedProductReviewList = newPtmStdSkuDescription.getFetchedProductReviewList();

        String oldFeathers = oldPtmStdSkuDescription.getFeatures();
        String oldSummary = oldPtmStdSkuDescription.getSummary();
        List<FetchedProductReview> oldFetchedProductReviewList = oldPtmStdSkuDescription.getFetchedProductReviewList();

        Update update = new Update();
        boolean flagFeather = false;
        boolean flagSummary = false;
        boolean flagReview = false;

        if (!StringUtils.isEqual(newFeathers, oldFeathers)) {
            update.set("features", newFeathers);
            flagFeather = true;
        }

        if (!StringUtils.isEqual(newSummary, oldSummary)) {
            update.set("summary", newSummary);
            flagSummary = true;
        }

        for (FetchedProductReview newReview : newFetchedProductReviewList) {
            if (!oldFetchedProductReviewList.contains(newReview)) {
                oldFetchedProductReviewList.add(newReview);
                flagReview = true;
            }
        }

        if (flagReview == true) {
            update.set("fetchedProductReviewList", oldFetchedProductReviewList);
        }

        if (flagFeather || flagSummary || flagReview) {
            mdm.update(PtmStdSkuDescription.class, oldPtmStdSkuDescription.getId(), update);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBrandCard(PtmStdBrandCard ptmStdBrandCard) {
        mdm.save(ptmStdBrandCard);
    }

    private List<StdSkuImage> findImages(long skuId) {
        List<StdSkuImage> stdSkuImages = new ArrayList<>();

        List<PtmStdImage> stdImages = dbm.query(Q_SKU_IMAGE, Arrays.asList(skuId));

        for (PtmStdImage stdImage : stdImages) {
            stdSkuImages.add(new StdSkuImage(stdImage));
        }

        return stdSkuImages;
    }

    private List<StdSkuPrice> findSkuPriceInfo(long skuId) {
        List<PtmStdPrice> skuPrices = dbm.query(Q_SKU_PRICE, Arrays.asList(skuId));

        List<StdSkuPrice> stdSkuPrices = new ArrayList<>();
        for (PtmStdPrice stdPrice : skuPrices) {
            stdSkuPrices.add(new StdSkuPrice(stdPrice));
        }

        return stdSkuPrices;
    }

    private Map<String, StdSkuAttr> findStdSkuAttr(long skuId) {
        Map<String, StdSkuAttr> datamap = new HashMap<>();

        List<PtmStdSkuAttr> stdSkuAttrs = dbm.query(Q_SKU_ATTR, Arrays.asList(skuId));
        for (PtmStdSkuAttr stdSkuAttr : stdSkuAttrs) {
            datamap.put(stdSkuAttr.getStdName(), new StdSkuAttr(stdSkuAttr));
        }

        return datamap;
    }

    @Override
    @Transactional
    public boolean createStdSku(StdSkuBo skuBo) {

        //查找或者创建一个PtmStdSku
        boolean flag = false;//用来标记PtmStdSku是否是新创建的
        PtmStdSku stdSku = dbm.querySingle("SELECT t FROM PtmStdSku t WHERE t.sourceId = ?0", Arrays.asList(skuBo.getSourceId()));
        if (stdSku == null) {
            stdSku = new PtmStdSku(skuBo.getTitle(), skuBo.getBrand(), skuBo.getModel(),
                    skuBo.getCategoryId(), skuBo.getRefPrice(), skuBo.getSourceId(), skuBo.getSourceUrl());
            dbm.create(stdSku);
            flag = true;
        }

        //补全PtmStdPrice列表
        List<PtmStdPrice> skuPriceList = dbm.query("SELECT t FROM PtmStdPrice t WHERE t.stdSkuId = ?0 ", Arrays.asList(stdSku.getId()));
        List<String> existsUrl = new ArrayList<>();
        if (skuPriceList != null && skuPriceList.size() != 0) {
            for (PtmStdPrice stdPrice : skuPriceList) {
                existsUrl.add(stdPrice.getUrl());
            }
        }
        // 各网站价格
        List<StdSkuPrice> skuPrices = skuBo.getSkuPrices();
        for (StdSkuPrice skuPrice : skuPrices) {
            if (existsUrl.contains(skuPrice.getUrl())) {
                continue;
            }
            PtmStdPrice stdPrice = new PtmStdPrice(stdSku.getId(), skuPrice.getTitle(), skuPrice.getPrice(), skuPrice.getStockCount(),
                    skuPrice.getShippingFee(), skuPrice.getSkuStatus(), skuPrice.getWebsite(), skuPrice.getUrl());
            dbm.create(stdPrice);
        }

        if (flag) {
            // sku属性
            Map<String, StdSkuAttr> attrs = skuBo.getSkuAttrs();
            if (attrs != null) {
                for (Map.Entry<String, StdSkuAttr> attr : attrs.entrySet()) {
                    PtmStdAttrDef attrDef = getAttrByName(attr.getKey());

                    StdSkuAttr skuAttr = attr.getValue();

                    PtmStdSkuAttr ptmStdSkuAttr = new PtmStdSkuAttr(stdSku.getId(), attrDef.getId(), attrDef.getStdDefName(), skuAttr.getStdValue());
                    dbm.create(ptmStdSkuAttr);
                }
            }

            // 图片
            List<StdSkuImage> stdImages = skuBo.getSkuImages();
            if (stdImages != null) {
                for (StdSkuImage skuImage : stdImages) {
                    PtmStdImage stdImage = new PtmStdImage(stdSku.getId(), skuImage.getOriImageUrl());
                    dbm.create(stdImage);
                }
            }

            // detail-info
            PtmStdSkuDetail stdSkuDetail = new PtmStdSkuDetail(stdSku.getId(), skuBo.getParamGroups(), skuBo.getDesc());
            mdm.save(stdSkuDetail);
        }

        return true;
    }

    private PtmStdAttrDef getAttrByName(String key) {
        PtmStdAttrDef attrDef = findAttrDefByName(key);

        if (attrDef == null) {
            attrDef = new PtmStdAttrDef(key);
            dbm.create(attrDef);
        }

        return attrDef;
    }

    private void createStdSkuValues(long stdSkuId, FlipkartAttribute fa) {

        String color = fa.getColor();
        String displaySize = fa.getDisplaySize();
        String size = fa.getSize();
        String sizeUnit = fa.getSizeUnit();
        String storage = fa.getStorage();

        setStdSkuValue(stdSkuId, "color", color);
        setStdSkuValue(stdSkuId, "displaySize", displaySize);
        setStdSkuValue(stdSkuId, "size", size);
        setStdSkuValue(stdSkuId, "sizeUnit", sizeUnit);
        setStdSkuValue(stdSkuId, "storage", storage);
    }

    private void setStdSkuValue(long stdSkuId, String stdName, String value) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(value.trim())) {
            return;
        }

        PtmStdAttrDef ptmStdAttrDef = new PtmStdAttrDef(stdName);
        dbm.createIfNoExist(ptmStdAttrDef);

        PtmStdSkuAttr stdSkuValue = new PtmStdSkuAttr(stdSkuId, ptmStdAttrDef.getId(), ptmStdAttrDef.getStdDefName(), value);
        dbm.create(stdSkuValue);
    }

   /* @Override
    public Map<String, FlipkartSkuInfo> searchSku(String keyword) throws Exception {
        FlipkartAffiliateProductProcessor fapp = new FlipkartAffiliateProductProcessor();
        List<AffiliateProduct> searchedPros = fapp.getAffiliateProductByKeyword(keyword, 10);

        if (ArrayUtils.isNullOrEmpty(searchedPros)) {
            System.out.println("no searched results.");
            return null;
        }

        FlipkartSkuInfo skuInfo = null;
        for (AffiliateProduct ap : searchedPros) {
            FlipkartSkuInfo for_skuInfo = fapp.getSkuInfo(ap.getSourceId());
            System.out.println("[title]" + for_skuInfo.getTitle() + "\t| [brand]\t" + for_skuInfo.getProductBrand() + "\t| [model]"
                    + for_skuInfo.getModelNum() + "|"
                    + for_skuInfo.getModelName() + "|"
                    + for_skuInfo.getModelId());
            if (keyword.contains(for_skuInfo.getProductBrand().toLowerCase())
                    &&
                    (keyword.contains(for_skuInfo.getModelNum().toLowerCase())
                            || keyword.contains(for_skuInfo.getModelName().toLowerCase())
                            || keyword.contains(for_skuInfo.getModelId().toLowerCase())
                    )) {
                skuInfo = for_skuInfo;
                break;
            }
        }

        if (skuInfo == null) {
            return null;
        }

        Map<String, FlipkartSkuInfo> skuInfoMap = new HashMap<>();

        String[] sourceIds = skuInfo.getProductFamily();
        skuInfoMap.put(skuInfo.getProductId(), skuInfo);

        for (String sid : sourceIds) {
            try {
                FlipkartSkuInfo skuInfo1 = fapp.getSkuInfo(sid);
                skuInfoMap.put(skuInfo1.getProductId(), skuInfo1);

                System.out.println(skuInfo1.getProductBrand() + "|\t" + skuInfo1.getModelName() + "|\t" + skuInfo1.getAttributes());
            } catch (Exception e) {
                System.out.println("error");
            }
        }

        return skuInfoMap;
    }

    public Map<String, FlipkartSkuInfo> searchSku_bak(String keyword) throws Exception {
        FlipkartAffiliateProductProcessor fapp = new FlipkartAffiliateProductProcessor();
        List<AffiliateProduct> searchedPros = fapp.getAffiliateProductByKeyword(keyword, 10);

        if (ArrayUtils.isNullOrEmpty(searchedPros)) {
            System.out.println("no searched results.");
            return null;
        }

        List<FlipkartSearchedSkuAnalysisResult> analysisResults = new ArrayList<>();

        for (AffiliateProduct ap : searchedPros) {
            float score = ProductAnalysisService.stringMatch(keyword, ap.getTitle());
            System.out.println(ap.getSourceId() + "\t" + ap.getTitle() + "\t" + score);
            analysisResults.add(new FlipkartSearchedSkuAnalysisResult(score, ap));
        }

        Collections.sort(analysisResults, new Comparator<FlipkartSearchedSkuAnalysisResult>() {
            @Override
            public int compare(FlipkartSearchedSkuAnalysisResult o1, FlipkartSearchedSkuAnalysisResult o2) {
                if (o1.getScore() > o2.getScore()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        String sourceId = analysisResults.get(0).getAp().getSourceId();
        FlipkartSkuInfo skuInfo = fapp.getSkuInfo(sourceId);

        if (StringUtils.isEmpty(skuInfo.getProductBrand()) || StringUtils.isEmpty(skuInfo.getModelName())) {
            System.out.println(skuInfo.getTitle() + "\t|\t" + skuInfo.getProductBrand() + "\t|\t" + skuInfo.getModelName());
            return null;
        }

        Map<String, FlipkartSkuInfo> skuInfoMap = new HashMap<>();

        String[] sourceIds = skuInfo.getProductFamily();
        skuInfoMap.put(sourceId, skuInfo);

        for (String sid : sourceIds) {
            try {
                FlipkartSkuInfo skuInfo1 = fapp.getSkuInfo(sid);
                skuInfoMap.put(skuInfo1.getProductId(), skuInfo1);

                System.out.println(skuInfo1.getProductBrand() + "|\t" + skuInfo1.getModelName() + "|\t" + skuInfo1.getAttributes());
            } catch (Exception e) {
                System.out.println("error");
            }
        }

        return skuInfoMap;
    }*/

    /*@Override
    @Transactional
    public PtmStdProduct createStd(Map<String, FlipkartSkuInfo> skuInfoMap) {
        if (skuInfoMap == null) {
            System.out.println("sku map is null.");
            return null;
        }
        Set<Map.Entry<String, FlipkartSkuInfo>> skuInfoSet = skuInfoMap.entrySet();
        Iterator<Map.Entry<String, FlipkartSkuInfo>> it = skuInfoSet.iterator();

        // No.1
        Map.Entry<String, FlipkartSkuInfo> kv = it.next();
        FlipkartSkuInfo skuInfo = kv.getValue();

        // build std product
        String productName = skuInfo.getTitle();
        String brandName = skuInfo.getProductBrand();
        String desc = skuInfo.getDesc();

        String modelName = skuInfo.getModelName();
        if (StringUtils.isEmpty(modelName)) {
            modelName = skuInfo.getModelNum();
            if (StringUtils.isEmpty(modelName)) {
                modelName = skuInfo.getModelId();
            }
        }

        if (StringUtils.isEmpty(brandName) || StringUtils.isEmpty(modelName)) {
            System.out.println(String.format("brand[%s].model[%s].one is empty.skipped.", brandName, modelName));
            return null;
        }

        // query product by brand and model
        long count = dbm.querySingle("select count(t.id) from PtmStdProduct t where t.brand=?0 and t.model=?1 ", Arrays.asList(brandName, modelName));
        if (count >= 1) {
            System.out.println(String.format("brand[%s].model[%s].exists.", brandName, modelName));
            return null;
        }

        PtmStdProduct stdProduct = new PtmStdProduct(productName, brandName, modelName);

        // create product
        dbm.create(stdProduct);

        do {
            createStdSku(stdProduct.getId(), skuInfo);

            if (it.hasNext()) {
                kv = it.next();
                skuInfo = kv.getValue();
            } else {
                break;
            }
        } while (skuInfo != null);

        System.out.println(skuInfoMap.size());
        return stdProduct;
    }*/


}
