package hasoffer.timer.msp;

import hasoffer.base.utils.HtmlUtils;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.system.ITimerService;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeStringByXPath;

/**
 * Created on 2015/12/21.
 */
//@Component
public class MspCategoryUpdateTask {

    private final static String Q_CATEGORY =
            "SELECT t FROM MspCategory t";
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMspService mspService;
    @Resource
    ITimerService timerService;

    private Logger logger = LoggerFactory.getLogger(MspCategoryUpdateTask.class);

    @Scheduled(cron = "0 0/1 * * * ?")
    public void updateCategories() {
        SysTimerTaskLog log = timerService.createTaskLog("MspCategoryUpdateTask");

        final List<MspCategory> categories = dbm.query(Q_CATEGORY);

        int count = 0;

        for (MspCategory category : categories) {
            logger.debug(category.getName());
            if (category.getParentId() == 0) {
                continue;
            }

            long id = category.getId();
            String url = category.getUrl();
            try {
                TagNode root = HtmlUtils.getUrlRootTagNode(url);
                String proCount = getSubNodeStringByXPath(root, "//div[@class='list-hdr__prdct-cnt']/b[@class='js-prdct-cnt__totl']", null);
                if (NumberUtils.isDigits(proCount)) {
                    count++;
//					logger.debug(proCount);
                    mspService.updateCategory(id, Integer.parseInt(proCount));
                    logger.debug(category.getProCount() + "-" + proCount);
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

        String result = "MspCategoryUpdateTask update " + count + " categories.";
        timerService.updateTaskLog(log.getId(), result);
    }

}
