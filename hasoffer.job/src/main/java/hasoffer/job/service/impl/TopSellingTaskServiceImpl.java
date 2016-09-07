package hasoffer.job.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import hasoffer.base.enums.TaskLevel;
import hasoffer.core.bo.enums.TopSellStatus;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.job.dto.TopSellingTaskDTO;
import hasoffer.job.service.ITopSellingTaskService;

@Service("topSellingTaskService")
public class TopSellingTaskServiceImpl implements ITopSellingTaskService {

	private Logger logger = LoggerFactory.getLogger(TopSellingTaskServiceImpl.class);

	@Resource
	IDataBaseManager dbm;

	@Resource
	IFetchDubboService fetchDubboService;
	
	@Resource
	ICmpSkuService cmpSkuService;
	
	@Resource
	IPriceOffNoticeService priceOffNoticeService;

	@Override
	public void commitTask() {
		String hql = "select new hasoffer.job.dto.TopSellingTaskDTO(p.id,p.productId,p.website,p.url,p.updateTime) from PtmTopSelling s , PtmCmpSku p  where  p.productId = s.id and s.status='"
				+ TopSellStatus.ONLINE.toString() + "' and p.id is not null order by s.count desc";
		List<TopSellingTaskDTO> page = new ArrayList<TopSellingTaskDTO>();
		
		ExecutorService service = Executors.newCachedThreadPool();
		/*
		 * for (int i = 0; i < 30; i++) { es.execute(new
		 * CmpSkuDubboUpdateWorker(dbm, queue, fetchDubboService, cmpSkuService,
		 * priceOffNoticeService, redisListService)); }
		 */

		for (int i = 1;; i++) {
			page = dbm.query(hql, i, 2000);
			if (page.isEmpty()) {
				break;
			}
			ConcurrentLinkedQueue<TopSellingTaskDTO> queue = new ConcurrentLinkedQueue<>();
			for (TopSellingTaskDTO ptmCmpSku : page) {
				fetchDubboService.sendUrlTask(ptmCmpSku.getWebsite(), ptmCmpSku.getUrl(), TaskLevel.LEVEL_3);
				logger.debug("commit topSelling task:" + ptmCmpSku.toString());
				queue.add(ptmCmpSku);
			}
			// 获取结果
			service.execute(new TopSellingTaskWorker(queue, fetchDubboService, cmpSkuService, priceOffNoticeService));
			// fetchDubboService, null));
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
