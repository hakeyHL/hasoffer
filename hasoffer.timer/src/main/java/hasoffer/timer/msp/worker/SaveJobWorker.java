package hasoffer.timer.msp.worker;

import hasoffer.core.msp.IMspService;
import hasoffer.fetch.model.ListJob;
import hasoffer.fetch.model.ProductJob;
import hasoffer.fetch.sites.mysmartprice.MspListProcessor;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by on 2015/12/21.
 */
public class SaveJobWorker implements Runnable {

	Set<String> existingProIds;
	ConcurrentLinkedQueue<ListJob> listQueue;
	IMspService mspService;

	public SaveJobWorker(Set<String> existingProIds, ConcurrentLinkedQueue<ListJob> listQueue, IMspService mspService) {
		this.existingProIds = existingProIds;
		this.listQueue = listQueue;
		this.mspService = mspService;
	}

	@Override
	public void run() {
		MspListProcessor listProcessor = new MspListProcessor();
		listProcessor.setExistingProductIds(existingProIds);

		while (true) {
			ListJob listJob = listQueue.poll();

			if (listJob == null) {
				try {
					TimeUnit.SECONDS.sleep(5);
					System.out.println("save job has no jobs. go to sleep!");
				} catch (InterruptedException e) {
					return;
				}
				continue;
			}

			try {
				System.out.println(listJob.getListUrl());
				if (Integer.parseInt(listJob.getCategoryId()) >= 4 && Integer.parseInt(listJob.getCategoryId()) <= 7) {
					System.out.println(listJob.getListUrl());
				}
				listProcessor.extractProductJobs(listJob);
				Set<ProductJob> proJobs = listJob.getProductJobs();
				mspService.saveProductJobs(proJobs);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}

}
