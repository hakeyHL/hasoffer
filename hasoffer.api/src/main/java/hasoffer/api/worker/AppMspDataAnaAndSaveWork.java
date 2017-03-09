package hasoffer.api.worker;

import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.product.PtmMStdProductService;
import hasoffer.core.product.PtmMStdSkuService;

/**
 * Created by hs on 2017年03月03日.
 * Time 15:00
 */
public class AppMspDataAnaAndSaveWork implements Runnable {
    PtmMStdProductService ptmMStdProductService;
    MongoDbManager mongoDbManager;
    PtmMStdSkuService ptmMStdSkuService;
    private String url;

    public AppMspDataAnaAndSaveWork(String url, PtmMStdProductService ptmMStdProductService, MongoDbManager mongoDbManager, PtmMStdSkuService ptmMStdSkuService) {
        this.url = url;
        this.ptmMStdProductService = ptmMStdProductService;
        this.mongoDbManager = mongoDbManager;
        this.ptmMStdSkuService = ptmMStdSkuService;
    }

    @Override
    public void run() {
        //获取可以获取商品列表的url
        //获取商品列表
        //获取商品属性,title ,url,图片,store
        //将商品信息传给页面解析,存入商品信息--放入队列
   /*     Spider.create(new IndiaMySmartPricePageProcessor(ptmMStdProductService, mongoDbManager, ptmMStdSkuService))
                .addUrl(url)
                .thread(1)
                .run();*/
    }
}
