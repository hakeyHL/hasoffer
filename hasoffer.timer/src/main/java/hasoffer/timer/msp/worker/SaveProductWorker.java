package hasoffer.timer.msp.worker;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.po.msp.MspProductJob;
import hasoffer.fetch.sites.mysmartprice.NewMspSkuCompareProcessor;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by on 2015/12/21.
 */
public class SaveProductWorker implements Runnable {
	ConcurrentLinkedQueue<MspProductJob> jobs;
	ConcurrentLinkedQueue<MspProductJob> jobs2;
	AtomicInteger saveCount;
	IMspService mspService;
	private Logger logger = LoggerFactory.getLogger(SaveProductWorker.class);

	public SaveProductWorker(ConcurrentLinkedQueue<MspProductJob> jobs,
	                         ConcurrentLinkedQueue<MspProductJob> jobs2,
	                         AtomicInteger saveCount,
	                         IMspService mspService) {
		this.jobs = jobs;
		this.jobs2 = jobs2;
		this.saveCount = saveCount;
		this.mspService = mspService;
	}

	@Override
	public void run() {
		while (true) {
			MspProductJob productJob = jobs.poll();

			if (productJob == null) {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			NewMspSkuCompareProcessor compareProcessor = new NewMspSkuCompareProcessor();
			try {
				MySmartPriceProduct mspp = compareProcessor.parse(productJob.getUrl());

				if (productJob.getPtmProductId() > 0) {
					mspService.updateProductByMspProduct(productJob.getPtmProductId(), mspp);
				} else {
					mspService.saveProduct(productJob.getId(), productJob.getCategoryId(), mspp);
				}

				mspService.updateJobProcessTime(productJob.getId(), TimeUtils.nowDate());

				logger.debug(String.format("queue size : %d(%d), saved count : %d, ",
				                           jobs.size(),
				                           jobs2 == null ? 0 : jobs2.size(),
				                           saveCount.addAndGet(1)));
			} catch (Exception e) {
				logger.error(e.toString() + "\n" + productJob.getUrl());
				if (jobs2 != null) {
					jobs2.add(productJob);
				}
			}
		}
	}
}