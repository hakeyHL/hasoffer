package hasoffer.job.listener;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spider.model.param.FetchedParamGroup;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/8/16.
 */
public class CompareWebsiteFetchResultWorker implements Runnable {

    private IFetchDubboService fetchDubboService;

    public CompareWebsiteFetchResultWorker(IFetchDubboService fetchDubboService) {
        this.fetchDubboService = fetchDubboService;
    }

    @Override
    public void run() {
        while (true) {

            FetchCompareWebsiteResult compareWebsiteFetchResult = fetchDubboService.getCompareWebsiteFetchResult(Website.MOBILE91);

            if (compareWebsiteFetchResult == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {

                }
                System.out.println("pop get null wait 10 seconds");
            }

            TaskStatus taskStatus = compareWebsiteFetchResult.getTaskStatus();

            if (TaskStatus.FINISH.equals(taskStatus)) {

                FetchedProduct ptmproduct = compareWebsiteFetchResult.getPtmproduct();
                List<FetchedProduct> ptmcmpskuList = compareWebsiteFetchResult.getPtmcmpskuList();
                List<FetchedParamGroup> fetchedParamGroupList = compareWebsiteFetchResult.getFetchedParamGroupList();
                long categoryId = compareWebsiteFetchResult.getCategoryId();


            } else {
                System.out.println("pop get " + taskStatus + "continue");
            }
        }
    }
}
