package hasoffer.timer.test;

import hasoffer.core.product.IAliSpuService;
import hasoffer.core.product.IFetchService;
import hasoffer.core.worker.BrandSaveWorker;
import hasoffer.fetch.model.FetchStats;
import hasoffer.fetch.model.PageModel;
import hasoffer.fetch.sites.Ali.AliexpressBandListProcessor;
import hasoffer.fetch.sites.Ali.model.AliSpu;
import hasoffer.fetch.worker.AliBrandAnalysisWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class BrandFetchTest {
	@Resource
	IFetchService fetchService;
	@Resource
	IAliSpuService aliSpuService;

	private Logger logger = LoggerFactory.getLogger(BrandFetchTest.class);

	@Test
	public void fetchTest() {
		String url = "http://www.aliexpress.com/spulist.html?catId=5090301&page=6";

		AliexpressBandListProcessor abListProcessor = new AliexpressBandListProcessor();

		PageModel pageModel = abListProcessor.getPageModel(url);

		LinkedBlockingDeque<AliSpu> spuQueue = new LinkedBlockingDeque<AliSpu>();
		LinkedBlockingDeque<String> pageUrls = new LinkedBlockingDeque<String>();

		FetchStats fetchStats = new FetchStats();

		String pageUrlTmp = pageModel.getUrlTemplate();
		int pageCount = pageModel.getPageCount();

		for (int i = 1; i <= pageCount; i++) {
			pageUrls.add(pageUrlTmp.replace("{page}", String.valueOf(i)));
		}

		logger.debug(pageUrls.size() + "");

		ThreadGroup threadGroup = new ThreadGroup("t1");
		int THREAD_COUNT = 10;
		Thread[] ts = new Thread[THREAD_COUNT];
		for (int i = 0; i < THREAD_COUNT; i++) {
			ts[i] = new Thread(threadGroup, new AliBrandAnalysisWorker(spuQueue, pageUrls, fetchStats));
			ts[i].start();
		}

		ThreadGroup threadGroup2 = new ThreadGroup("t2");
		Thread[] ts2 = new Thread[THREAD_COUNT];
		for (int i = 0; i < THREAD_COUNT; i++) {
			ts2[i] = new Thread(threadGroup2, new BrandSaveWorker(spuQueue, aliSpuService, fetchStats));
			ts2[i].start();
		}

		status(spuQueue, threadGroup, threadGroup2, fetchStats);

		logger.debug(String.format("--- Queue[%d].Active Threads[%d/%d] ---",
		                           spuQueue.size(),
		                           threadGroup.activeCount(),
		                           threadGroup2.activeCount()));

		threadGroup.destroy();
		threadGroup2.destroy();
		logger.debug("all job finished.");
	}

	private void status(LinkedBlockingDeque<AliSpu> spuQueue,
	                    ThreadGroup threadGroup,
	                    ThreadGroup threadGroup2,
	                    FetchStats fetchStats) {
		while (true) {
			if (threadGroup.activeCount() == 0) {
				fetchStats.listWorksEnded();
				if (threadGroup2.activeCount() == 0 && spuQueue.size() == 0) {
					break;
				}
			}

			logger.debug(String.format("--- Queue[%d].Active Threads[%d/%d] ---",
			                           spuQueue.size(),
			                           threadGroup.activeCount(),
			                           threadGroup2.activeCount()));

			logger.debug(String.format("--- listed[%d].saved[%d] ---",
			                           fetchStats.getListedJobCount(),
			                           fetchStats.getSavedJobCount()));

			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
