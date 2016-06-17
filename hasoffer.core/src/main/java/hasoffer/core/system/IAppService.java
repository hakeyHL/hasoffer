package hasoffer.core.system;

import hasoffer.base.enums.AppType;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
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
}
