package hasoffer.job.bean.deal;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.base.utils.http.XPathUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.utils.ImageUtil;
import hasoffer.spider.util.HtmlHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.File;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2017/1/12.
 */
public class PromDealFetchJobBean extends QuartzJobBean {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(PromDealFetchJobBean.class);

    @Resource
    IDealService dealService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("PromDealFetchJobBean.executeInternal(): fetch prom deal start.");
        try {

            TagNode root = HtmlUtils.getUrlRootTagNode("https://www.promodescuentos.com/nuevas");

            List<TagNode> dealNodeList = XPathUtils.getSubNodesByXPath(root, "//div[@class='thread-cardWrapper']", null);

            for (TagNode dealNode : dealNodeList) {

                TagNode timeFlagNode = XPathUtils.getSubNodeByXPath(dealNode, "//time", null);

                String timeFlagString = timeFlagNode.getText().toString();

                //带有小时的过滤   ex:2h,30m
                if (timeFlagString.contains("h,")) {
                    break;
                }

                //取10分钟以内的
                String[] subStr = timeFlagString.split(" ");
                if (subStr.length < 3) {
                    logger.error("timeFlagString parse error");
                    continue;
                } else {
                    String minStr = subStr[1];
                    if (NumberUtils.isNumber(minStr)) {
                        int minNum = Integer.parseInt(minStr);
                        if (minNum > 30) {
                            continue;
                        }
                    } else {
                        logger.error("timeFlagString parse error");
                        continue;
                    }
                }

                //失效样式
                TagNode expireFlagNode = XPathUtils.getSubNodeByXPath(dealNode, "//span[@title='EXPIRADO']", null);
                if (expireFlagNode != null) {
                    continue;
                }

                //找到链接
                TagNode hrefNode = XPathUtils.getSubNodeByXPath(dealNode, "//a[@class='cept-tt linkPlain thread-title-text box--all-b']", null);
                String href = hrefNode.getAttributeByName("href");

                //重定向到详情页面
                TagNode hrefRootNode = null;
                //String html = null;
                try {
                    hrefRootNode = HtmlUtils.getUrlRootTagNode(href);
                    //html = HtmlUtils.getUrlHtml(href);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                //类目数据和网站数据
                String categoryName = "";
                String websiteString = "";
                TagNode categoryFlagNode = XPathUtils.getSubNodeByXPath(dealNode, "//span[@class='hide--toW3 overflow--ellipsis']", null);
                if (categoryFlagNode == null) {//没有IRIr a la oferta,类目给定默认值
                    categoryName = "VARIOS";
                } else {

                    TagNode categoryAndWebsiteNode = XPathUtils.getSubNodeByXPath(hrefRootNode, "//div[@class='cept-thread-main overflow--fromW3-hidden space--fromW3-l-3']", null);
                    if (categoryAndWebsiteNode != null) {

                        TagNode categoryNode = XPathUtils.getSubNodeByXPath(categoryAndWebsiteNode, "/a[1]", null);
                        TagNode websiteNode = XPathUtils.getSubNodeByXPath(categoryAndWebsiteNode, "/a[2]", null);

                        if (categoryNode != null) {
                            categoryName = categoryNode.getText().toString();
                        }

                        if (websiteNode != null) {
                            websiteString = StringUtils.filterAndTrim(websiteNode.getText().toString(), Arrays.asList("Ofertas y promociones de"));
                        }
                    }
                }

                //标题
                String title = "";
                TagNode titleNode = XPathUtils.getSubNodeByXPath(hrefRootNode, "//h1/span", null);
                if (titleNode != null) {
                    title = titleNode.getText().toString();
                }

                //图片
                String imageUrl = "";
                TagNode imageNode = XPathUtils.getSubNodeByXPath(hrefRootNode, "//img[@class='cept-thread-img thread-image imgFrame-img link']", null);
                if (imageNode != null) {
                    imageUrl = imageNode.getAttributeByName("src");
                }

                //价格
                float price = 0.0f;
                TagNode priceNode = XPathUtils.getSubNodeByXPath(hrefRootNode, "//div[@class='overflow--hidden']/div/div", null);
                if (priceNode != null) {
                    String priceString = StringUtils.filterAndTrim(priceNode.getText().toString(), Arrays.asList("Precio:", "$", ","));
                    if (NumberUtils.isNumber(priceString)) {
                        price = Float.parseFloat(priceString);
                    }
                }

                //链接
                String url = "";
                TagNode linkNode = XPathUtils.getSubNodeByXPath(hrefRootNode, "//div[@class='overflow--hidden']/div/a", null);
                if (linkNode != null) {
                    String redirectLink = linkNode.getAttributeByName("href");

                    HttpResponseModel responseModel = HttpUtils.get(redirectLink, null);

                    String redirectUrl = responseModel.getRedirect();

                    String[] split = redirectUrl.split("\\?url=");

                    redirectUrl = split[1];

                    redirectUrl = URLDecoder.decode(redirectUrl);

                    String[] subStr3 = redirectUrl.split("/&ppref");

                    url = subStr3[0];
                }

                //描述
                TagNode descNode = XPathUtils.getSubNodeByXPath(hrefRootNode, "//div[@class='page2-space--h-p']/div[2]", null);
                String descriptionWithHtml = HtmlUtils.getInnerHTML(descNode);
                String descriptionWithOutHtml = HtmlHelper.delHTMLTagExclusion(descriptionWithHtml);

                //持久化deal信息
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("--------------------------------------------------------------------\n");
                stringBuilder.append("category:" + categoryName + "\n");
                stringBuilder.append("websiteString:" + websiteString + "\n");
                stringBuilder.append("title:" + title + "\n");
                stringBuilder.append("imageUrl:" + imageUrl + "\n");
                stringBuilder.append("price:" + price + "\n");
                stringBuilder.append("url:" + url + "\n");
                stringBuilder.append("descriptionWithOutHtml:" + descriptionWithOutHtml + "\n");
                stringBuilder.append("--------------------------------------------------------------------\n");

                logger.info(stringBuilder.toString());

                File imageFile = ImageUtil.downloadImage(imageUrl);

                String dealPath = ImageUtil.uploadImage(imageFile);
                String dealBigPath = ImageUtil.uploadImage(imageFile, 316, 180);
                String dealSmallPath = ImageUtil.uploadImage(imageFile, 180, 180);

                Website website = null;
                try {
                    website = Website.valueOf(websiteString.toUpperCase());
                } catch (Exception e) {
                    website = Website.UNKNOWN;
                }

                AppDeal mexicoAppDeal = new AppDeal();
                mexicoAppDeal.setListPageImage(dealSmallPath);
                mexicoAppDeal.setInfoPageImage(dealBigPath);
                mexicoAppDeal.setCategory(categoryName);
                mexicoAppDeal.setWebsite(website);
                mexicoAppDeal.setTitle(title);
                mexicoAppDeal.setImageUrl(dealPath);
                mexicoAppDeal.setPresentPrice(price);
                mexicoAppDeal.setLinkUrl(url);
                mexicoAppDeal.setDescription(descriptionWithOutHtml);
                mexicoAppDeal.setCreateTime(TimeUtils.nowDate());
                mexicoAppDeal.setExpireTime(TimeUtils.add(TimeUtils.nowDate(), TimeUtils.MILLISECONDS_OF_1_DAY * 2));
                logger.info("insert into appDeal:{}", mexicoAppDeal.toString());
                dealService.createAppDealByPriceOff(mexicoAppDeal);
            }

        } catch (Exception e) {
            logger.error("fetch prom deal error.", e);
        }
        logger.info("PromDealFetchJobBean.executeInternal(): fetch prom deal end.");
    }
}
