package hasoffer.job.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.dto.FetchTestTaskDTO;
import hasoffer.job.service.IFetchTestService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("fetchTestService")
public class FetchTestServiceImpl implements IFetchTestService {

    private Logger logger = LoggerFactory.getLogger(FetchTestServiceImpl.class);

    @Resource
    IDataBaseManager dbm;

    @Resource(name = "fetchTimerDubboService")
    IFetchDubboService fetchDubboService;

    @Override
    public void commitTask() {
        String hql = "select new hasoffer.job.dto.FetchTestTaskDTO(p.id,p.website,p.url) from SrmProductSearchCount s , PtmCmpSku p where p.productId=s.productId and s.ymd=?0 and p.website=?1 and s.productId is not null order by s.count desc";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String dateStr = DateFormatUtils.format(new Date(), "yyyyMMdd");
        List<Website> websiteList = Arrays.asList(Website.AMAZON, Website.FLIPKART, Website.SNAPDEAL, Website.SHOPCLUES,
                Website.PAYTM);
        String timeStr = DateFormatUtils.format(new Date(), "HH");
        String baseOutFolder = "/home/hasoffer/logs/testFetch/" + dateStr + "/" + timeStr;

        ExecutorService service = Executors.newFixedThreadPool(websiteList.size());
        int seconds = 60 * 20;
        for (Website website : websiteList) {
            PageableResult<FetchTestTaskDTO> page = dbm.queryPage(hql, 1, 1000, Arrays.asList(dateStr, website));
            logger.debug("Fetch Test start: website:{},count:{}", website.toString(), page.getNumFund());
            List<FetchTestTaskDTO> list = page.getData();
            File file = new File(baseOutFolder + "/" + website.toString() + "_task.txt");
            for (FetchTestTaskDTO ptmCmpSku : list) {
                fetchDubboService.sendUrlTask(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), seconds, TaskLevel.LEVEL_1);
                try {
                    FileUtils.write(file, ptmCmpSku.toString() + "\r\n", "utf-8", true);
                    logger.info("save task success ! " + file.getAbsolutePath() + "," + ptmCmpSku.toString());
                } catch (IOException e) {
                    logger.error("save task error ! " + file.getAbsolutePath() + "," + ptmCmpSku.toString(), e);
                }
            }
            //启动结果轮询线程
            File resultFile = new File(baseOutFolder + "/" + website.toString() + "_result.txt");
            service.execute(new FetchTestWorker(list, fetchDubboService, resultFile));
        }

        service.shutdown();
        while (true) {
            if (service.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
