package hasoffer.core.system.impl;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.system.AppUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hs on 2016年12月06日.
 * Time 17:45
 */
@Service
public class AppUserServiceImpl implements AppUserService {
    private final String API_URMUSER_GET_BY_EMAIL = "";
    private final String API_URMUSER_GET_BY_EMAIL_USERNAME = "";
    @Resource
    IDataBaseManager dbm;

    @Override
    public void insertUser(UrmUser urmUser) {
        dbm.createIfNoExist(urmUser);
    }

    @Override
    public UrmUser getUrmUserById(long userId) {
        return dbm.get(UrmUser.class, userId);
    }

    @Override
    public UrmUser getUrmUserByEmail(String email) {
        List<UrmUser> userList = dbm.query(API_URMUSER_GET_BY_EMAIL, Arrays.asList(email));
        if (userList.size() > 0) {
            System.out.println(userList.size() > 1 ? "price userSize " + userList.size() : "");
            return userList.get(0);
        }
        return null;
    }

    @Override
    public UrmUser getUrmUserByUserNameAndPwd(String userName, String passwd) {
        return dbm.querySingle(API_URMUSER_GET_BY_EMAIL_USERNAME, Arrays.asList(passwd));
    }

    @Override
    public void updateUrmUser(UrmUser urmUser) {
        dbm.update(urmUser);
    }
}
