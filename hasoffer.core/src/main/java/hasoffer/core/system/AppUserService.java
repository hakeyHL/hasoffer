package hasoffer.core.system;

import hasoffer.core.persistence.po.urm.UrmUser;

/**
 * Created by hs on 2016年12月06日.
 * Time 17:44
 */
public interface AppUserService {
    void insertUser(UrmUser urmUser);

    UrmUser getUrmUserById(long userId);

    UrmUser getUrmUserByEmail(String email);

    UrmUser getUrmUserByUserNameAndPwd(String userName, String passwd);

    void updateUrmUser(UrmUser urmUser);
}
