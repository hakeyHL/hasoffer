package hasoffer.api.service;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.spider.constants.SpiderConstants;
import hasoffer.spider.logger.SpiderLogger;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.IOException;

/**
 * Created on 2016/11/14.
 */
public abstract class AbstractCompareWebsitePageProcessor implements PageProcessor {
    private final Logger logger = LoggerFactory.getLogger(AbstractCompareWebsitePageProcessor.class);
    private Site site = Site.me();

    public abstract Website getWebSite();

    @Override
    public void process(Page page) {
        String url = page.getRequest().getUrl();
        SpiderLogger.debugHtml("URL:{} ", url);
        int statusCode = page.getStatusCode();
        if (statusCode != 200) {
            logger.error("Http request error. URL is {}, error code is {}", url, statusCode);
            FetchCompareWebsiteResult fetchCompareWebsiteResult = new FetchCompareWebsiteResult();
            fetchCompareWebsiteResult.setWebsite(getWebSite());
            fetchCompareWebsiteResult.setUrl(url);
            fetchCompareWebsiteResult.setTaskStatus(TaskStatus.EXCEPTION);
            page.putField(SpiderConstants.SPIDER_PARSE_RESULT, fetchCompareWebsiteResult);
            return;
        }
        String userHome = System.getProperty("user.home");
        try {
            processPage(page);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                FileUtils.writeStringToFile(new File(userHome + File.separator + "logs" + File.separator + "sku-fail-html" + File.separator + url + ".html"), page.getHtml().toString());
            } catch (IOException e1) {
                logger.error("Write File error: {}.", url, e1);
            }
            FetchCompareWebsiteResult fetchCompareWebsiteResult = new FetchCompareWebsiteResult();
            fetchCompareWebsiteResult.setWebsite(getWebSite());
            fetchCompareWebsiteResult.setUrl(url);
            fetchCompareWebsiteResult.setTaskStatus(TaskStatus.EXCEPTION);
            page.putField(SpiderConstants.SPIDER_PARSE_RESULT, fetchCompareWebsiteResult);
        }
    }

    public abstract void processPage(Page page) throws Exception;

    @Override
    public Site getSite() {
        site.addHeader("x-user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 FKUA/website/41/website/Desktop");
        return site;
    }
}
