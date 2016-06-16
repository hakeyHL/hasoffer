package hasoffer.dubbo.api.fetch.task;

import hasoffer.dubbo.api.fetch.po.FetchResult;
import hasoffer.dubbo.api.fetch.service.IFetchService;
import hasoffer.dubbo.api.fetch.service.IKeywordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FetchWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FetchWorker.class);

    private IKeywordService keywordService;
    private IFetchService fetchService;

    public FetchWorker(IKeywordService keywordService, IFetchService fetchService) {
        this.keywordService = keywordService;
        this.fetchService = fetchService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("FetchWorker is alive.");
                Object pop = keywordService.popKeyword();
                if (pop == null) {
                    TimeUnit.SECONDS.sleep(10);
                } else {
                    FetchResult fetch = fetchService.fetch(pop.toString());
                    fetchService.cache(fetch);
                }
            } catch (Exception e) {
                logger.error("FetchWorker is error", e.getMessage());
            }
        }
    }


}
