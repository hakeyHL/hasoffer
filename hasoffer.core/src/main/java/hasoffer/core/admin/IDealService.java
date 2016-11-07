package hasoffer.core.admin;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.product.solr.DealModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


/**
 * Created by lihongde on 2016/6/21 14:02
 */
public interface IDealService {

    PageableResult<AppDeal> findDealList(int page, int size, int type, String orderByType);

    /**
     * admin后台手动导入deal
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    Map<String, Object> importExcelFile(MultipartFile multipartFile) throws Exception;

    AppDeal createAppDealByPriceOff(AppDeal appDeal);

    AppDeal getDealById(Long dealId);

    AppDeal getDealBySourceId(Long skuId);

    AppBanner getBannerByDealId(Long dealId);

    /**
     * 将deal设置为过期
     * 过期时间为创建时间
     *
     * @param id
     */
    void updateDealExpire(Long id);

    void saveOrUpdateBanner(AppBanner banner);

    void updateDeal(AppDeal deal);

    void deleteDeal(Long dealId);

    void deleteBanner(Long bannerId);

    void batchDelete(Long[] ids);

    void importDeal2Solr(DealModel dm);

    void reimportAllDeals2Solr();
}
