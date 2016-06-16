package hasoffer.core.system.impl;

import hasoffer.base.enums.AppType;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.AppVersion;
import hasoffer.core.persistence.po.app.AppWebsite;
import hasoffer.core.system.IAppService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2015/12/30.
 */
@Service
public class AppServiceImpl implements IAppService {

    private static final String Q_APP_VERSION =
            "SELECT t FROM AppVersion t " +
                    " WHERE t.appType = ?0 " +
                    " ORDER BY t.publishTime DESC";

    private static final String Q_APP_WEBSITE =
            "SELECT t FROM AppWebsite t " +
                    " WHERE t.appshow = ?0 ";

    @Resource
    IDataBaseManager dbm;

    @Override
    public AppVersion getLatestVersion(AppType appType) {
//        return dbm.get(AppVersion.class, 3L);
        List<AppVersion> versions = dbm.query(Q_APP_VERSION, Arrays.asList(appType));

        return ArrayUtils.hasObjs(versions) ? versions.get(0) : null;
    }

    @Override
    public List<AppWebsite> getWebsites(boolean appshow) {
        return dbm.query(Q_APP_WEBSITE, Arrays.asList(appshow));
    }
}
