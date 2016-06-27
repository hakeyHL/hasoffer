package hasoffer.core.admin;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


/**
 * Created by lihongde on 2016/6/21 14:02
 */
public interface IDealService {

    PageableResult<AppDeal> findDealList(int page, int size);

    Map<String, Object> importExcelFile(MultipartFile multipartFile) throws Exception;

    AppDeal getDealById(Long dealId);

    AppBanner getBannerByDealId(Long dealId);

    void addOrUpdateBanner(AppBanner banner);

    void updateDeal(AppDeal deal);

    public void delete(Long dealId);

    public void batchDelete(Long[] ids);
}
