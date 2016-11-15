package hasoffer.core.admin.impl;

import hasoffer.base.model.Website;
import hasoffer.core.admin.IUrmAffAccountService;
import hasoffer.core.admin.dao.UrmAffAccountDAO;
import hasoffer.core.persistence.po.admin.UrmAffAccount;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UrmAffAccountServiceImpl implements IUrmAffAccountService {

    @Resource
    UrmAffAccountDAO urmAffAccountDAO;

    @Override
    public List<UrmAffAccount> findAffAccountList(Website website) {
        return urmAffAccountDAO.findAffAccountList(website);
    }
}
