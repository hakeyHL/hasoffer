package hasoffer.job.bean.stat;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.product.ICmpSkuUpdateStatService;
import hasoffer.fetch.helper.WebsiteHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;

/**
 * Created on 2016/6/12.
 */
public class StatVisitSkuUpdateJobBean extends QuartzJobBean {

    private static final String Q_UPDATESUCCESS_SKU_BY_WEBSITE1 = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.updateTime > ?1 AND t.createTime < ?2 ";
    private static final String Q_UPDATESUCCESS_SKU_BY_WEBSITE2 = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.website = ?0 AND t.updateTime > ?1 AND t.createTime IS NULL ";
    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuUpdateStatService cmpSkuUpdateStatService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String todayString = TimeUtils.parse(TimeUtils.now(), "yyyyMMdd");
        Date date = TimeUtils.toDate(TimeUtils.today());

        int count = 0;//用来记录更新的数量

        for (Website website : WebsiteHelper.DEFAULT_WEBSITES) {

            Long i = dbm.querySingle(Q_UPDATESUCCESS_SKU_BY_WEBSITE1, Arrays.asList(website, date, date));
            Long j = dbm.querySingle(Q_UPDATESUCCESS_SKU_BY_WEBSITE2, Arrays.asList(website, date));

            if (i != null) {
                count += i.intValue();
            }

            if (j != null) {
                count += j.intValue();
            }

            String id = HexDigestUtil.md5(website.name() + todayString);

            cmpSkuUpdateStatService.saveOrUpdateSkuUpdateSuccessAmount(id, website, count);
        }

    }
}
