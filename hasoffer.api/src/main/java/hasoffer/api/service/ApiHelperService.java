package hasoffer.api.service;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.webcommon.context.Context;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by hs on 2017年01月04日.
 * Time 15:32
 * 这里用于处理apiUtils不能处理的逻辑(主要是循环依赖)
 */
@Component
public class ApiHelperService {
    @Resource
    AppServiceImpl appService;
    @Resource
    private ICacheService<UrmUser> userCacheService;

    /**
     * 获取当前用户
     *
     * @return
     */
    public UrmUser getCurrentUser() {
        UrmUser urmUser = null;
        String userToken = Context.currentContext().getHeader("usertoken");
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(userToken)) {
            String key = "user_" + userToken;
            urmUser = userCacheService.get(UrmUser.class, key, 0);
            if (urmUser == null) {
                urmUser = appService.getUserByUserToken(userToken);
                if (urmUser != null) {
                    userCacheService.add(key, urmUser, TimeUtils.SECONDS_OF_1_DAY);
                }
            }
        }
        return urmUser;
    }
}
