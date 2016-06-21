package hasoffer.core.system;

import hasoffer.base.enums.AppType;
import hasoffer.base.model.PageableResult;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.urm.urmUser;

import java.util.List;

/**
 * Created on 2015/12/30.
 */
public interface IAppService {

    AppVersion getLatestVersion(AppType appType);

    List<AppWebsite> getWebsites(boolean appshow);

    List<OrderStatsAnalysisPO>getBackDetails(String userId);

    urmUser getUserByUserToken(String userToken);

    OrderStatsAnalysisPO getOrderDetail(String orderId,String  userId);
    PageableResult getDeals(Long page,Long pageSize);
    List<PtmCategory> getCategory();
    urmUser getUserById(String thirdId);
    List getProductByCriteria(SearchCriteria criteria);
    int addUser(urmUser urmUser);
    void updateUserInfo(urmUser uUser);
    List<AppBanner> getBanners();
}
