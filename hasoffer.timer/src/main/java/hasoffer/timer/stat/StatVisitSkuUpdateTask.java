package hasoffer.timer.stat;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.product.ICmpSkuUpdateStatService;
import hasoffer.fetch.helper.WebsiteHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;

/**
 * Created on 2016/6/12.
 */
@Component
public class StatVisitSkuUpdateTask {

    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuUpdateStatService cmpSkuUpdateStatService;

    private static final String Q_UPDATESUCCESS_SKU_BY_WEBSITE = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.updateTime > ?1 AND t.createTime < ?2 ";

    @Scheduled(cron = "0 30 0/1 * * ?")
    public void visitSkuUpdate() {

        String todayString = TimeUtils.parse(TimeUtils.now(), "yyyyMMdd");
        Date date = TimeUtils.toDate(TimeUtils.today());

        for (Website website : WebsiteHelper.DEFAULT_WEBSITES) {

            Long i = dbm.querySingle(Q_UPDATESUCCESS_SKU_BY_WEBSITE, Arrays.asList(website, date, date));

            String id = HexDigestUtil.md5(website.name() + todayString);

            cmpSkuUpdateStatService.saveOrUpdateSkuUpdateSuccessAmount(id, website, i);
        }
    }

}
