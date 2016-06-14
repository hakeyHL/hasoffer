package hasoffer.timer.test;


import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.thd.snapdeal.ThdAProduct;
import hasoffer.core.thd.IThdService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.sites.snapdeal.SnapdealListProcessor;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author:menghaiquan
 * Date:2016/1/14 2016/1/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class SnapDealFetchTest {
	private static Logger logger = LoggerFactory.getLogger(SnapDealFetchTest.class);

//	@Resource
//	ISnapdealService snapdealService;
	@Resource
	IThdService thdService;
	@Resource
	IDataBaseManager dbm;

//	@Test
//	public void fetchCategories() {
//		SnapDealCategoryProcessor processor = new SnapDealCategoryProcessor();
//		try {
//			Set<SnapDealFetchCategory> categories = processor.parseCategories();
//			for (SnapDealFetchCategory category :categories){
//				saveCategories(category, 0L);
//			}
//
//		} catch (ContentParseException e) {
//			e.printStackTrace();
//		} catch (XPatherException e) {
//			e.printStackTrace();
//		} catch (HttpFetchException e) {
//			e.printStackTrace();
//		}
//	}

//	@Test
//	public void updateSouceIds() throws HttpFetchException, XPatherException {
//		List<ThdACategory> cate2s = snapdealService.getCate2s();
//		for (ThdACategory category : cate2s){
//			long id = SnapDealCategoryProcessor.parseCateId(category.getUrl());
//			snapdealService.updateSouceId(category.getId(), id);
//		}
//	}

//	@Test
//	public void fetchProducts(){
//		List<ThdACategory> cate2s = snapdealService.getCate2sAll();
//
//		BlockingQueue<SnapDealFetchProduct> queue = new ArrayBlockingQueue<SnapDealFetchProduct>(1024);
//		ExecutorService service = Executors.newFixedThreadPool(5);
//		for (int i = 0; i <5; i++){
//			service.execute(new SaveSnapDealProductWorker(queue, snapdealService));
//		}
//
//		for (ThdACategory category : cate2s){
//			System.out.println("Start to fetch category " + category.getId());
//			SnapDealCategoryProcessor.fetchProductsByCate(category.getId(), category.getSourceSid(), category.getProCount(), queue);
//		}
//
//		SaveSnapDealProductWorker.setIsFinished(true);
//		System.out.println("Start to finish!");
//	}
//
//	public void saveCategories(SnapDealFetchCategory category, long parentId){
//		hasoffer.core.persistence.po.thd.snapdeal.ThdACategory categoryPo = new hasoffer.core.persistence.po.thd.snapdeal.ThdACategory();
//		categoryPo.setParentId(parentId);
//		categoryPo.setName(category.getName());
//		categoryPo.setDepth(category.getDepth());
//		categoryPo.setProCount(category.getProCount());
//		categoryPo.setUrl(category.getUrl());
//		categoryPo.setImageUrl(category.getImageUrl());
//		categoryPo.setWebsite(Website.SNAPDEAL);
//		categoryPo = snapdealService.createCategory(categoryPo);
//		if (category.getSubCates() != null){
//			for (SnapDealFetchCategory subCate : category.getSubCates()){
//				saveCategories(subCate, categoryPo.getId());
//			}
//		}
//	}

	@Test
	public void testFetchSnapdealProduct() throws XPatherException {

		SnapdealListProcessor listProcessor = new SnapdealListProcessor();

//		String ajaxUrlTemplate = "http://www.snapdeal.com/acors/json/product/get/search/175/startNum/4?sort=plrty";
		//对ajaxUrlTemplate中“？号”后面的部分截取，保留sort的排序规则
//		String ajaxUrlTemplate = "http://www.snapdeal.com/acors/json/product/get/search/2908/startNum/4?sort=plrty";
//		String ajaxUrlTemplate = "http://www.snapdeal.com/acors/json/product/get/search/2907/startNum/4?q=&sort=plrty&brandPageUrl=&keyword=&vc=&webpageName=categoryPage&campaignId=&brandName=&isMC=false&clickSrc=";
//		String ajaxUrlTemplate = "http://www.snapdeal.com/acors/json/product/get/search/288/startNum/4?q=&sort=plrty&brandPageUrl=&keyword=&vc=&webpageName=categoryPage&campaignId=&brandName=&isMC=false&clickSrc=";
		String ajaxUrlTemplate = "http://www.snapdeal.com/acors/json/product/get/search/154/startNum/4?q=&sort=plrty&brandPageUrl=&keyword=&vc=&webpageName=categoryPage&campaignId=&brandName=&isMC=false&clickSrc=";



		for (int i=0;i<500;i++){

			String ajaxUrl = ajaxUrlTemplate.replace("startNum",4*i+"");

			List<ListProduct> products = null;
			try {
				logger.debug(ajaxUrl+"  start  ");
				products = listProcessor.getProductByAjaxUrl(ajaxUrl,45L);
			} catch (HttpFetchException e) {

			} catch (ContentParseException e) {
				logger.debug(ajaxUrl+"解析失败");
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			//todo 如果最后一页返回的商品的记录为0个，该线程将无法终止
			if(products.size()==0){
				i--;
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}

			for (ListProduct product:products){

				ThdAProduct aProduct = new ThdAProduct(175,product.getUrl(),product.getSourceId(),product.getImageUrl(),product.getTitle(),product.getPrice());

				try{
					thdService.createProduct(aProduct);
					logger.debug(product.getSourceId()+"  add success");
				}catch (Exception e){
					logger.debug(aProduct.getSourceId());
				}
			}
			logger.debug(ajaxUrl+"  finish  ");
		}
	}

	@Test
	public void testFetchSnapdealProductTask(){

//		SnapdealProductFetchByAjaxUrlTask task = new SnapdealProductFetchByAjaxUrlTask();
//
//		task.dbm = this.dbm;
//		task.thdService = this.thdService;
//
//		task.fetchSnapdealProductByAjaxUrl();

	}

}
