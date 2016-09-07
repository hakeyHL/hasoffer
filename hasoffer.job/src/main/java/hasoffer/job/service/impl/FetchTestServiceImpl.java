package hasoffer.job.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.service.IFetchTestService;

@Service("fetchTestService")
public class FetchTestServiceImpl implements IFetchTestService {

    private Logger logger = LoggerFactory.getLogger(FetchTestServiceImpl.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @Resource
    IDataBaseManager dbm;

    @Resource
    IFetchDubboService fetchDubboService;
 
    @Override
    public void commitTask() {
        String hql = "select p.* from SrmProductSearchCount s left join PtmCmpSku p on p.productId = s.productId   where s.ymd=?0 and p.website=?1 and s.productId is not null order by s.count desc";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String dateStr = sdf.format(calendar.getTime());
        List<Website> websiteList = Arrays.asList(Website.AMAZON, Website.FLIPKART, Website.SNAPDEAL, Website.SHOPCLUES,
                Website.PAYTM);
        long batch = System.currentTimeMillis();
        String baseOutFolder = "/home/hasoffer/logs/testFetch/" + dateStr + "/" + batch;

        ExecutorService service = Executors.newFixedThreadPool(websiteList.size());

        for (Website website : websiteList) {
            PageableResult<PtmCmpSku> page = dbm.queryPage(hql, 1, 1000, Arrays.asList(dateStr, website));
            logger.debug("Fetch Test start: website:{},count:{}", website.toString(), page.getNumFund());
            List<PtmCmpSku> list = page.getData();
            File file = new File(baseOutFolder + "/" + website.toString() + "_task.txt");
            for (PtmCmpSku ptmCmpSku : list) {
                fetchDubboService.sendUrlTask(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), TaskLevel.LEVEL_3);
                try {
                    FileUtils.write(file, ptmCmpSku.toString() + "\r\n", "utf-8", true);
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
