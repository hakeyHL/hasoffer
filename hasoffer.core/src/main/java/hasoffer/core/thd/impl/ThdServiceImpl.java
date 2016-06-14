package hasoffer.core.thd.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.enums.RelateStatus;
import hasoffer.core.bo.enums.RelateType;
import hasoffer.core.bo.product.ProductBo;
import hasoffer.core.exception.ERROR_CODE;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.thd.ThdCategory;
import hasoffer.core.persistence.po.thd.ThdProduct;
import hasoffer.core.persistence.po.thd.ThdProductUpdater;
import hasoffer.core.persistence.po.thd.updater.ThdFetchTaskUpdater;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.search.exception.NonMatchedProductException;
import hasoffer.core.thd.IThdService;
import hasoffer.core.thd.ThdHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
public class ThdServiceImpl implements IThdService {

    private final static String Q_THD_TEMP = "SELECT t FROM {T} t ";

    private final static String Q_THD_UNRELATED_TEMP = "SELECT t FROM {T} t WHERE t.cmpSkuId = 0";

    private final static String Q_THD_UNRELATED_BY_CATEGORY_TEMP =
            "SELECT t FROM {T} t " +
                    " WHERE t.cmpSkuId = 0 " +
                    "   AND t.ptmCateId = ?0 ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    ProductIndexServiceImpl productIndexService;
    @Resource
    IProductService productService;

    @Override
    public PageableResult<ThdProduct> getProducts(Website website, long category3, RelateType relateType, int page, int size) {
        final String sql = Q_THD_TEMP.replace("{T}", ThdHelper.getThdProductClass(website).getName()) +
                " WHERE t.relateType = ?0 " +
                "   AND t.ptmCateId = ?1  ";

        return dbm.queryPage(sql, page, size, Arrays.asList(relateType, category3));
    }

    @Override
    public PageableResult<ThdProduct> getProducts(Website website, RelateType relateType, int page, int size) {
        final String sql = Q_THD_TEMP.replace("{T}", ThdHelper.getThdProductClass(website).getName()) +
                " WHERE t.relateType = ?0 ";

        return dbm.queryPage(sql, page, size, Arrays.asList(relateType));
    }

    @Override
    public PageableResult<ThdProduct> getProducts(Website website, long cateId, int page, int size) {
        final String sql = Q_THD_TEMP.replace("{T}", ThdHelper.getThdProductClass(website).getName()) +
                " WHERE t.ptmCateId = ?0 ";

        return dbm.queryPage(sql, page, size, Arrays.asList(cateId));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateTask(long taskId, Date processTime, TaskStatus taskStatus) {
        ThdFetchTaskUpdater thdFetchTaskUpdater = new ThdFetchTaskUpdater(taskId);
        if (taskStatus == TaskStatus.STOPPED) {
            thdFetchTaskUpdater.getPo().setLastProcessTime(processTime);
        }
        thdFetchTaskUpdater.getPo().setTaskStatus(taskStatus);
        dbm.update(thdFetchTaskUpdater);
    }

    @Override
    public PageableResult<ThdProduct> getProducts(Website website, int page, int size) {
        return dbm.queryPage(Q_THD_TEMP.replace("{T}", ThdHelper.getThdProductClass(website).getName()), page, size);
    }

    @Override
    public PageableResult<ThdProduct> getPagedUnrelatedProductsByCategory(Website website, long cateId, int page, int size) {
        String sql = Q_THD_UNRELATED_BY_CATEGORY_TEMP.replace("{T}", ThdHelper.getThdProductClass(website).getName());
        return dbm.queryPage(sql, page, size, Arrays.asList(cateId));
    }

    @Override
    public List<ThdProduct> getUnrelatedProducts(Website website, int page, int size) {
        return dbm.query(Q_THD_UNRELATED_TEMP.replace("{T}", ThdHelper.getThdProductClass(website).getName()), page, size);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ThdProduct createProduct(ThdProduct product) {

        dbm.create(product);

        return product;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateRelateInfo(ThdProduct thd) {
        ThdProductUpdater thdProductUpdater = new ThdProductUpdater(thd.getId(), ThdHelper.getThdProductClass(thd.getWebsite()));
        thdProductUpdater.getPo().setCmpSkuId(thd.getCmpSkuId());
        thdProductUpdater.getPo().setRelateType(thd.getRelateType());
        dbm.update(thdProductUpdater);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ThdCategory createCategory(ThdCategory category) {

        dbm.create(category);

        return category;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public RelateStatus relate(ThdProduct thd) {
        RelateStatus rs = RelateStatus.NO_PRODUCT;

        long cateId = thd.getPtmCateId();
        int level = 3;

        String _q = StringUtils.toLowerCase(thd.getTitle());

        PageableResult<Long> pagedProIds = productIndexService.searchPro(cateId, level, _q, 1, 5);

        List<Long> proIds = pagedProIds.getData();
        long proId = 0L, cmpSkuId = 0L;

        PageableResult<PtmCmpSku> pageableResult = null;
        List<PtmCmpSku> cmpSkus = null;
        PtmCmpSku cmpSku = null;

        try {
            if (ArrayUtils.isNullOrEmpty(proIds)) {
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, "", 0);
            }

            proId = proIds.get(0);
            PtmProduct product = productService.getProduct(proId);

            float mc = StringUtils.wordMatchD(StringUtils.toLowerCase(product.getTitle()), _q);
            if (!StringUtils.isEmpty(product.getTag())) {
                mc = (mc + StringUtils.wordMatchD(StringUtils.toLowerCase(product.getTag()), _q) * 2) / 2;
            }

            // 匹配度如果小于40%, 则认为不匹配
            if (mc <= 0.4) {
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, product.getTitle(), mc);
            }

            pageableResult = productService.listPagedCmpSkus(proId, 1, 100);
            cmpSkus = pageableResult.getData();

            for (PtmCmpSku cs : cmpSkus) {
                if (cs.getWebsite() == null) {
                    continue;
                }
                if (cs.getWebsite().equals(thd.getWebsite())) {
                    cmpSku = cs;
                    rs = RelateStatus.EXISTS_CMPSKU;
                    break;
                }
            }

            if (cmpSku == null) {
                rs = RelateStatus.EXISTS_PRODUCT_NO_CMPSKU;
                cmpSku = productService.createCmpsku(proId, thd.getPrice(), thd.getUrl(), thd.getTitle(), thd.getImageUrl());
            }

        } catch (NonMatchedProductException e) {
            // 创建新的product，并关联该网站的比价
            ProductBo product = productService.createProduct(
                    cateId, thd.getTitle(), thd.getPrice(),
                    thd.getWebsite(), thd.getUrl(), thd.getSourceId(),
                    thd.getImageUrl()
            );

            cmpSku = product.getCmpSkus().get(0);
        }

        if (thd.getCmpSkuId() == 0 && cmpSku != null) {
            RelateType relateType = RelateType.NONE;

            switch (rs) {
                case EXISTS_PRODUCT_NO_CMPSKU:
                    relateType = RelateType.NEW_CMPSKU;
                    break;
                case EXISTS_CMPSKU:
                    relateType = RelateType.EXISTS;
                    break;
                case NO_PRODUCT:
                    relateType = RelateType.NEW_PRODUCT;
                    break;
            }

            thd.setCmpSkuId(cmpSku.getId());
            thd.setRelateType(relateType);

            updateRelateInfo(thd);
        }

        return rs;
    }
}
