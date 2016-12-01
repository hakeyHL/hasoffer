package hasoffer.admin.service;

import hasoffer.admin.po.SysAdmin;

import javax.security.auth.login.LoginException;

public interface IAdminService {

    SysAdmin login(String uname, String password) throws LoginException;

    SysAdmin findAdminByKey(String ukey);

    void createWebsites();

    void logout(SysAdmin admin);
}