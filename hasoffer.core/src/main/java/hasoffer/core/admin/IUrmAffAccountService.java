package hasoffer.core.admin;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.admin.UrmAffAccount;

import java.util.List;

public interface IUrmAffAccountService {
    List<UrmAffAccount> findAffAccountList(Website website);
}
