package hasoffer.core.product.iml;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.product.ISummaryProductService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created on 2016/5/5.
 */
@Service
public class SummaryProductServiceImpl implements ISummaryProductService {

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDataBaseManager dbm;

    @Override
    public PageableResult<SummaryProduct> getPagedSummaryProductByTime(long startLongTime, boolean gt, int sort, int page, int size) {

        Query query = new Query();

        if (gt == true) {
            query.addCriteria(Criteria.where("lUpdateTime").gt(startLongTime));
        } else {
            Criteria.where("lUpdateTime").lt(startLongTime);
        }

        if (sort == 0) {
            query.with(new Sort(Sort.Direction.ASC, "lUpdateTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "lUpdateTime"));
        }

        return mdm.queryPage(SummaryProduct.class, query, page, size);
    }

    @Override
    public void updateCmpSkuByFetchResult(SummaryProduct summaryProduct, long id) {
        if (summaryProduct.getPrice() <= 0) {
            return;
        }

        PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, id);
        if (ptmCmpSku == null) {
            return;
        }

        String title = summaryProduct.getTitle();
        String subTitle = summaryProduct.getSubTitle();

        if ("null".equals(title) || "null".equals(subTitle)) {
            return;
        }

        PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(id);

        String skuTitle = summaryProduct.getTitle() + summaryProduct.getSubTitle();
        skuTitle = skuTitle.replaceAll("\\s+", "");

        updater.getPo().setSkuTitle(skuTitle);

        updater.getPo().setTitle(summaryProduct.getTitle());
        updater.getPo().setPrice(summaryProduct.getPrice());
        updater.getPo().setUpdateTime(TimeUtils.nowDate());
        updater.getPo().setSourceSid(summaryProduct.getSourceId());

        if (!StringUtils.isEmpty(summaryProduct.getImageUrl())) {
            updater.getPo().setOriImageUrl(summaryProduct.getImageUrl());
        }

        if (summaryProduct.getSkuStatus() != null) {
            if (!summaryProduct.getSkuStatus().equals(ptmCmpSku.getStatus())) {
                updater.getPo().setStatus(summaryProduct.getSkuStatus());
            }
        }

        if (ptmCmpSku.getWebsite() == null) {
            updater.getPo().setWebsite(summaryProduct.getWebsite());
        }

        dbm.update(updater);
    }
}
