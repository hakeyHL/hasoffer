package hasoffer.job.listener;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.product.IProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;

import java.util.Arrays;
import java.util.Date;
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

    }
}
