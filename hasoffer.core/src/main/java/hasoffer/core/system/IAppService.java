package hasoffer.core.system;

import hasoffer.base.enums.AppType;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.*;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.urm.UrmSignCoin;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.persistence.po.urm.UrmUserRedeemGroup;

import java.util.List;
import java.util.Map;

/**
 * Created on 2015/12/30.
 */
public interface IAppService {

    AppVersion getLatestVersion(AppType appType);

    AppVersion getLatestVersion(MarketChannel marketChannel, AppType appType);

    List<AppWebsite> getWebsites(boolean appshow);

    List<OrderStatsAnalysisPO> getBackDetails(String userId);

    UrmUser getUserByUserToken(String userToken);

    OrderStatsAnalysisPO getOrderDetail(String orderId, String userId);

    PageableResult getDeals(Long page, Long pageSize);

    PageableResult getDeals(Long page, Long pageSize, int temp);

    List<PtmCategory> getCategory();

    AppDeal getDealDetail(long id);

    UrmUser getUserByThirdId(String thirdId);

    UrmUser getUserById(Long Id);

    List<UrmUser> getIdDescUserListByThirdId(String thirdId);

    List getProductByCriteria(SearchCriteria criteria);

    List<String> getUserDevicesByUserId(String userId);

    List<String> getUserIdsByDeviceId(String urmDeviceId);

    int addUrmUserDevice(List<UrmUserDevice> urmUserDevices);

    int addUser(UrmUser urmUser);

    List<String> getUserDevices(String deviceId);

    void updateUserInfo(UrmUser uUser);

    List<AppBanner> getBanners();

    List<PtmCategory> getChildCategorys(String categoryId);

    int isHasChildNode(Long id);

    void countDealClickCount(AppDeal appDeal);

    List<UrmUser> getUsersByUserName(String userName);

    Map<Integer, Integer> getSignAwardNum();

    List<HasofferCoinsExchangeGift> getGiftList();

    void bakUserInfo(UrmUser urmUser);

    UrmSignCoin getSignCoinByUserId(Long id);

    void updateUrmSignCoin(UrmSignCoin urmSignCoin);

    void checkAndAlertUser2Sign();

    List<Map<String, String>> getIndexPage(MarketChannel marketChannel, String deviceId);

    List<UrmSignCoin> getUserSignRecord();

    PageableResult<UrmSignCoin> getUserList(int page, int pageSize);

    int addUserRedeemGroup(List<UrmUserRedeemGroup> groupList);

    UrmUserRedeemGroup getUrmRedeemGroupById(Long id);
}
