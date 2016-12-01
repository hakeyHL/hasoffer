package hasoffer.admin.service.impl;

import hasoffer.admin.dao.AdminDAO;
import hasoffer.admin.po.SysAdmin;
import hasoffer.admin.service.IAdminService;
import hasoffer.common.util.HexDigestUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.security.auth.login.LoginException;
import java.util.Date;
import java.util.UUID;

@Service
public class AdminServiceImpl implements IAdminService {

    private Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Resource
    private AdminDAO adminDAO;

    public SysAdmin login(String uname, String password) throws LoginException {
        logger.debug(uname + " try login.");

        SysAdmin sysAdmin = adminDAO.querySingle(uname);
        if (sysAdmin == null) {
            throw new LoginException("用户不存在");
        }

        if (!sysAdmin.getPassword().equals(password)) {
            throw new LoginException("密码不正确");
        }

        if (!sysAdmin.isValid()) {
            throw new LoginException("用户已失效");
        }

        String ukey = UUID.randomUUID().toString();
        sysAdmin.setLastLoginTime(new Date());
        sysAdmin.setUkey(ukey);
        //
        adminDAO.updateUkey(sysAdmin);
        //
        //SysAdminUpdater sysAdminUpdater = new SysAdminUpdater(sysAdmin.getId());
        //sysAdminUpdater.getPo().setLastLoginTime(TimeUtils.nowDate());
        //sysAdminUpdater.getPo().setUkey(ukey);
        //dbm.update(sysAdminUpdater);

        return sysAdmin;
    }

    public SysAdmin findAdminByKey(String ukey) {
        if (StringUtils.isEmpty(ukey)) {
            return null;
        }

        return adminDAO.querySingleByUKey(ukey);
    }

    public void createWebsites() {

    }

    public void logout(SysAdmin admin) {
        String _ukey = HexDigestUtil.md5(String.valueOf(System.currentTimeMillis()));
        admin.setUkey(_ukey);
        adminDAO.updateUkey(admin);
    }

}