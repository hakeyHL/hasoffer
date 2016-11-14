package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.Website;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created on 2016/11/7.
 */
public class DealSiteSendRquestJobBean extends QuartzJobBean {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(DealSiteSendRquestJobBean.class);

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IDealService dealService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("DealSiteFetchDealJobBean is run at {}", new Date());

        fetchDubboService.sendDealTask(Website.DESIDIME, TaskLevel.LEVEL_2);

        logger.info("DealSiteFetchDealJobBean send request success will stop at {}", new Date());
//


    }

//
}
