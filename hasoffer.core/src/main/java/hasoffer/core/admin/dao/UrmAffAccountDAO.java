package hasoffer.core.admin.dao;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.admin.UrmAffAccount;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class UrmAffAccountDAO {

    @Resource
    private IDataBaseManager dbm;

    public List<UrmAffAccount> findAffAccountList(Website website) {
        String sql = "SELECT t FROM UrmAffAccount t where t.validState='Y' and webSite=?0";
        return dbm.query(sql, Arrays.asList(website));
    }
}
