package hasoffer.job.service.impl;

import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.search.SearchProductService;
import hasoffer.job.service.IDealozFetchService;
import hasoffer.job.worker.DealozFetchWorker;
import org.springframework.stereotype.Service;
import sites.usa.dealoz.DealozCateProcessor;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("dealozFetchService")
public class DealozFetchServiceImpl implements IDealozFetchService {

    @Resource
    IDataBaseManager dbm;
    @Resource
    SearchProductService searchProductService;

    @Override
    public void fetchAllSite() {

        DealozCateProcessor.fetchProducts();
        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(DaemonThreadFactory.create(new DealozFetchWorker(searchProductService, dbm)));

    }
}
