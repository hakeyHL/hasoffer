package hasoffer.timer.product.worker;

import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IFetchService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuUpdateWorker implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(CmpSkuUpdateWorker.class);
    ConcurrentLinkedQueue<PtmCmpSku> skuQueue;
    ICmpSkuService cmpSkuService;
    IFetchService fetchService;

    public CmpSkuUpdateWorker(ConcurrentLinkedQueue<PtmCmpSku> skuQueue, ICmpSkuService cmpSkuService, IFetchService fetchService) {
        this.skuQueue = skuQueue;
        this.cmpSkuService = cmpSkuService;
        this.fetchService = fetchService;
    }

    public static List<String> ipPortList = new ArrayList<String>();
    private static int flag = 0;

    static {

        File ip = new File("C:/Users/wing/Desktop/ip.txt");

        try {

            BufferedReader reader = new BufferedReader(new FileReader(ip));

            while (reader.readLine() != null) {
                String line = reader.readLine();
                ipPortList.add(line);
            }

            reader.close();

        } catch (FileNotFoundException e) {
            logger.debug("file not found");
        } catch (IOException e) {
            logger.debug("io exception");
        }
    }

    @Override
    public void run() {

        while (true) {

            PtmCmpSku sku = skuQueue.poll();

            if (sku == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println("update job has no jobs. go to sleep!");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            // 判断，如果该sku 当天更新过价格, 直接跳过
            Date updateTime = sku.getUpdateTime();
            if (updateTime != null) {
                if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                    continue;
                }
            }

            // try update sku
            String url = sku.getUrl();
            Website website = WebsiteHelper.getWebSite(url);

            if (website == null) {
                logger.debug(url + " parse website get null");
            }

            if (Website.FLIPKART.equals(website)) {
                String oriUrl = sku.getOriUrl();
                if (!StringUtils.isEmpty(oriUrl)) {
                    String sourceId = FlipkartHelper.getProductIdByUrl(oriUrl);
                    if (!StringUtils.isEmpty(sourceId)) {
                        url = oriUrl;
                    }
                }
            }

            String ipProtString = ipPortList.get(flag);
            flag++;
            String[] subStrs = ipProtString.split(":");
            String ip = subStrs[0];
            String port = subStrs[1].trim();

            if (flag == ipPortList.size() - 1) {
                flag = 0;
            }

            System.setProperty("http.maxRedirects", "50");
            System.getProperties().setProperty("proxySet", "true");
            System.getProperties().setProperty("http.proxyHost", ip);
            System.getProperties().setProperty("http.proxyPort", port);


            OriFetchedProduct oriFetchedProduct = null;
            try {
                oriFetchedProduct = fetchService.fetchSummaryProductByUrl(url);
            } catch (Exception e) {

                //亚马逊解析空指针，重新解析
                if (Website.AMAZON.equals(website)) {
                    if (e instanceof IOException) {
                        logger.debug("AmazonAffiliateException");
                        skuQueue.add(sku);
                    }
                }

                String message = e.getMessage();
                if (message != null) {
                    if (message.contains("302") || message.contains("404")) {
                        oriFetchedProduct = new OriFetchedProduct();
                        oriFetchedProduct.setTitle("url expire");
                        oriFetchedProduct.setProductStatus(ProductStatus.OFFSALE);
                        oriFetchedProduct.setWebsite(website);
                        oriFetchedProduct.setUrl(url);
                    } else {
                        logger.error(e.toString() + "\n" + sku.getUrl());
                    }
                } else {
                    logger.error(e.toString() + "\n" + sku.getUrl());
                }

            }

            try {
                cmpSkuService.updateCmpSkuByOriFetchedProduct(sku.getId(), oriFetchedProduct);
            } catch (Exception e) {
                logger.debug(e.toString());
                if (oriFetchedProduct != null) {
                    logger.debug("title:" + oriFetchedProduct.getTitle());
                }
            }
        }
    }

}
