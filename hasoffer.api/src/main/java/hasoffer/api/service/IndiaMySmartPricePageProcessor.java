package hasoffer.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.po.app.mongo.AppMspProductDetail;
import hasoffer.core.persistence.po.app.mongo.AppMspSkuDetail;
import hasoffer.core.persistence.po.ptm.PtmMStdProduct;
import hasoffer.core.persistence.po.ptm.PtmMStdSku;
import hasoffer.core.product.PtmMStdProductService;
import hasoffer.core.product.PtmMStdSkuService;
import hasoffer.site.helper.FlipkartHelper;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hs on 2017年02月21日.
 * Time 16:33
 */
public class IndiaMySmartPricePageProcessor extends AbstractCompareWebsitePageProcessor {
    PtmMStdProductService ptmMStdProductService;
    MongoDbManager mongoDbManager;
    PtmMStdSkuService ptmMStdSkuService;

    public IndiaMySmartPricePageProcessor(PtmMStdProductService ptmMStdProductService, MongoDbManager mongoDbManager, PtmMStdSkuService ptmMStdSkuService) {
        this.ptmMStdProductService = ptmMStdProductService;
        this.mongoDbManager = mongoDbManager;
        this.ptmMStdSkuService = ptmMStdSkuService;
    }

    private static void printFoundString(List<String> all) {
        int i = 1;
        for (String str : all) {
            System.out.println("=============s" + i + "s=============");
            System.out.println(str);
            System.out.println("=============e" + i + "e=============");
            i++;
        }
    }

    private static String cleanUrl(String url, Website website) {
        String regexString = "";
        try {
            int maxDecoderNumber = 3;
            while (url.contains("%") && maxDecoderNumber > 0) {
                url = java.net.URLDecoder.decode(url, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return url;
        }

        switch (website) {
            case JABONG:
                regexString = "ulp=([\\s\\S]*?)\\.html";
                break;
            case FLIPKART:
                return FlipkartHelper.getCleanUrl(url).replace("dl.flipkart.com", "www.flipkart.com");
            case SHOPCLUES:
                regexString = "ckmrdr.([\\s\\S]*?\\.html)";
                break;
            case INFIBEAM:
                regexString = "([\\s\\S]*?)\\?";
                break;
            case MYNTRA:
                regexString = "ulp=([\\s\\S]*?)\\?";
                break;
            case CROMA:
                regexString = "url=([\\s\\S]*?)\\?";
                break;
            case AMAZON:
                if (url.contains("tag")) {
                    url = url.substring(0, url.indexOf("tag") - 1);
                }
                regexString = "mpre=([\\s\\S]*?)";
                break;
            case EBAY:
                regexString = "mpre=([\\s\\S]*?)\\?";
                break;
            case SHOPMONK:
                regexString = "url=([\\s\\S]*?).utm";
                break;
            case TATACLIQ:
                regexString = "url=([\\s\\S]*?)\\?";
                break;
        }
        Pattern compile = Pattern.compile(regexString);
        Matcher matcher = compile.matcher(url);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    @Override
    public Website getWebSite() {
        return Website.MYSMARTPRICE;
    }

    @Override
    public void processPage(Page page) throws Exception {
        Date currentDate = new Date();
        PtmMStdProduct ptmMStdProduct = new PtmMStdProduct();
//        JSONObject ptmproductJsonObj = new JSONObject();

//        List<JSONObject> skuListJsonObj = new ArrayList<>();
        List<PtmMStdSku> mStdSkus = new ArrayList<>();
        List<PtmMStdSku> trueStdSkuList = new ArrayList<>();

        //虽然当前页面会有不同的size,选择时会重定向到其他页面 , 但是研究之后决定可以忽略,
        //因为按照类目获取商品的话,  这些不同size的商品会出现在类目下的商品列表中.

        //sku没有图片


        Html html = page.getHtml();
        Selectable regex = html.regex("dataLayer = \\[([\\s\\S]*?})");
        List<String> all = regex.all();
        Selectable xpath;
        long mspId = 0;
        Map<Website, Integer> shippingFeeSiteSet = new HashMap<>();
        //主页商品信息获取完毕
        for (String str : all) {
            JSONObject jsonObject = JSONObject.parseObject(str);
            mspId = jsonObject.getLong("mspid");

            ptmMStdProduct.setTitle(jsonObject.getString("title"));
            ptmMStdProduct.setSourceId(String.valueOf(mspId));
            ptmMStdProduct.setCategoryName(jsonObject.getString("category"));
            ptmMStdProduct.setSubCategoryName(jsonObject.getString("subcategory"));
            ptmMStdProduct.setBrand(jsonObject.getString("brand"));
            ptmMStdProduct.setCreateTime(currentDate);
            ptmMStdProduct.setUpdateTime(currentDate);
            ptmMStdProduct.setImageUrl(jsonObject.getString("image"));
            ptmMStdProduct.setSourceUrl(jsonObject.getString("url"));
        }
        //2.
        Selectable modelXpath = html.xpath("/html/body/div[@class='body-wrpr clearfix']/div[2]/div[@class='sctn prdct-dtl clearfix']/div[@class='prdct-dtl__rght']/div[@class='prdct-dtl__vrnt-wrpr']/div[@class='prdct-dtl__vrnt-item prdct-dtl__vrnt-size']/div[@class='avlbl-sizes']/div[@class='avlbl-sizes__item-wrpr js-open-link avlbl-sizes__item-wrpr--slctd']/span[@class='avlbl-sizes__item']/text()");
        ptmMStdProduct.setModel(modelXpath.get());


        Selectable reviewXpath = html.xpath("/html/body/div[@class='body-wrpr clearfix']/div[2]/div[@class='sctn prdct-dtl clearfix']/div[@class='prdct-dtl__rght']/div[@class='prdct-dtl__tlbr clearfix']/div[@class='prdct-dtl__tlbr-rtng']/span[@class='prdct-dtl__tlbr-rvws prdct-dtl__tlbr-item js-inpg-link']/text()");
        ptmMStdProduct.setReview(Integer.parseInt(getNumberFromString(reviewXpath.get()) + ""));

        Selectable ratingsXpath = html.regex("ratingValue\":\"([\\s\\S]*?)\"");
        ptmMStdProduct.setRatings(BigDecimal.valueOf(Float.parseFloat(ratingsXpath.get())).multiply(BigDecimal.valueOf(20)).intValue());

        Selectable updateTimeRegex = html.xpath("/html/body/div[@class='body-wrpr clearfix']/div[@class='page-info clearfix']/div[@class='pt_meta_topunit']/div[@class='algn-rght']/abbr/@title");
        ptmMStdProduct.setMspUpdateTime(null);
        //获取描述
        xpath = html.xpath("/html/body/div[@class='body-wrpr clearfix']/div[@class='algn-wrpr clearfix']/div[@class='main-wrpr algn-left']");
        ///html/body/div[@class='body-wrpr clearfix']/div[@class='algn-wrpr clearfix']/div[@class='main-wrpr algn-left']/div[@class='sctn prdct-dtl-wdgt prdct-dscrptn']
        ///html/body/div[@class='body-wrpr clearfix']/div[@class='algn-wrpr clearfix']/div[@class='main-wrpr algn-left']
        ///div[@class='sctn prdct-dtl-wdgt prdct-dscrptn']
        Selectable descriptionXpath = xpath.xpath("//div[@class='sctn prdct-dtl-wdgt prdct-dscrptn']/div[@class='sctn__inr clearfix']/div[@class='prdct-dscrptn__item']/div[@class='prdct-dscrptn__item-dscrptn ']/div[@class='prdct-dscrptn__item-text']/text()");
        List<Selectable> descriptionNodes = descriptionXpath.nodes();
        for (Selectable selectable1 : descriptionNodes) {
            System.out.println(selectable1.get());
        }
        //获取参数
        ///div[@class='sctn sctn--no-pdng tchncl-spcftn clearfix']
        //TODO 解析参数
        Selectable paramsXpath = xpath.xpath("//div[@class='sctn sctn--no-pdng tchncl-spcftn clearfix']");
       /* System.out.println("参数html");
        printFoundString(paramsXpath.all());
        System.out.println("尚未解析参数列表....");*/

        //http://www.mysmartprice.com/mobile/ptrows_details.php?mspid=4882&data=store
        String skuExtentionUrl = "http://www.mysmartprice.com/mobile/ptrows_details.php?mspid=%d&data=store";
        //访问此接口可以获取sku的拓展属性列表
        if (mspId > 0) {
            HttpResponseModel httpResponseModel = HtmlUtils.getResponse(String.format(skuExtentionUrl, mspId), 3);
            //返回的是一个json,json里面是html
            //属性信息中只需要获取COD,Return Policy color price link  EMI delivery ,rating ,site : shippingFee 需要从列表页获取
            String bodyString = httpResponseModel.getBodyString();
            JSONObject bodyJsonObj = JSONObject.parseObject(bodyString);
            Set<String> strings = bodyJsonObj.keySet();
            Html skuHtml;
            List<Selectable> commonSelectables;
            Selectable skuCommonSelectable;
            String deliveryTime;
            String cod;
            String emi;
            String returnDays;
            Integer ratings;
            List<String> offerList;
            String color = "";
            for (String key : strings) {
                PtmMStdSku truePtmStdSku;

                String skuProsValueString = bodyJsonObj.getString(key);
                if (skuProsValueString.contains("ol")) {
                    skuProsValueString = skuProsValueString.replaceAll("ol>", "ul>");
                }
                skuHtml = new Html(skuProsValueString);

                //获取delivery days
                skuCommonSelectable = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__srvc']/div[@class='prc-grid-expnd__optn js-str-dlvry']/text()");
                deliveryTime = skuCommonSelectable.get();
                //获取COD

                skuCommonSelectable = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__srvc']/div[@class='prc-grid-expnd__optn js-str-cod']/text()");
                cod = skuCommonSelectable.get() == null ? "" : "COD";

                //获取EMI
                skuCommonSelectable = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__srvc']/div[@class='prc-grid-expnd__optn js-str-emi']/span/text()");
                emi = skuCommonSelectable.get() == null ? "" : "EMI";
                //return days

//                    skuCommonSelectable = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__srvc']/div[@class='prc-grid-expnd__optn']/span[@class='prc-grid__bold-txt']/text()");
                skuCommonSelectable = skuHtml.regex("([0-9]{2})[\\s\\S]*?days");
                returnDays = skuCommonSelectable.get();
                //星级

                skuCommonSelectable = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__ftr']/div[@class='prc-grid-expnd__slr']/div[@class='prc-grid-expnd__slr-rtng']/div[@class='rtng-num']/text()");
                //BigDecimal.valueOf(Float.parseFloat(ratingsXpath.get())).multiply(BigDecimal.valueOf(20)).intValue()
                ratings = BigDecimal.valueOf(Float.parseFloat(skuCommonSelectable.get().split("/")[0])).multiply(BigDecimal.valueOf(20)).intValue();

                //offer列表
                offerList = new ArrayList<>();
                commonSelectables = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__ofr-clmn']/div[@class='prc-grid-expnd__ofr']/div[@class='prc-grid-expnd__ofr-txt']").nodes();
                for (Selectable tempSelectable : commonSelectables) {
                    if (tempSelectable.get().contains("ul")) {
                        List<Selectable> nodes1 = tempSelectable.xpath("//div[@class='prc-grid-expnd__ofr-txt']/ul/").nodes();
                        for (Selectable selectable1 : nodes1) {
                            offerList.add(selectable1.regex("<li>([\\s\\S]*?)</li>").get());
                        }
                    } else {
                        offerList.add(tempSelectable.xpath("//div[@class='prc-grid-expnd__ofr-txt']/text()").get());
                    }

                }

                //如果获取不到其他size和价格的sku列表,链接是要的,要适配这种情况

                commonSelectables = skuHtml.xpath("//div[@class='prc-grid-expnd__data clearfix']/div[@class='prc-grid-expnd__vrtn']/div[@class='prc-grid-expnd__all-vrtns']/div[@class='prc-grid-expnd__vrtn-optn clearfix']").nodes();
                Selectable tempXpath;
                for (Selectable selectable : commonSelectables) {
                    //每循环一次, 都要创建一个sku
                    truePtmStdSku = new PtmMStdSku();
                    truePtmStdSku.setSkuStatus(SkuStatus.ONSALE);
                    truePtmStdSku.setWebsite(Website.valueOfString(key));
                    tempXpath = selectable.xpath("//div/div[@class='prc-grid-expnd__vrtn-name']/text()");
                    if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(tempXpath.get()))) {
                        color = StringUtils.deleteWhitespace(tempXpath.get());
                    }
                    tempXpath = selectable.xpath("//div/div[@class='prc-grid-expnd__vrtn-prc js-prc-tbl__gts-btn']/@data-url");
                    long mspSkuId = getMspSkuIdFromUrl(tempXpath.get());
                    truePtmStdSku.setColor(color);
                    truePtmStdSku.setUrl(getCleanUrlFromMsp(key, tempXpath.get()));
                    tempXpath = selectable.xpath("//div/div[@class='prc-grid-expnd__vrtn-prc js-prc-tbl__gts-btn']/text()");
                    truePtmStdSku.setPrice(Float.parseFloat(String.valueOf(getNumberFromString(tempXpath.get()))));
                    truePtmStdSku.setSourceId(String.valueOf(mspSkuId));
                    truePtmStdSku.setSupportPays(cod);
                    truePtmStdSku.setSupportPays(emi);
                    truePtmStdSku.setRatings(ratings);
                    truePtmStdSku.setOfferList(offerList);
                    truePtmStdSku.setReturnDays(returnDays);
                    truePtmStdSku.setDeliverTime(deliveryTime);
                    truePtmStdSku.setCreateTime(currentDate);
                    truePtmStdSku.setUpdateTime(currentDate);
                    truePtmStdSku.setTitle(ptmMStdProduct.getTitle());
                    //TODO 到目标网站获取title,不支持的site的title与主商品同
                    System.out.println("还需要根据url到目标网站获取sku的title");
                    trueStdSkuList.add(truePtmStdSku);
                }
            }
        }
        xpath = html.xpath("/html/body/div[@class='body-wrpr clearfix']/div[@class='algn-wrpr clearfix']/div[@class='main-wrpr algn-left']/div[@class='sctn'][1]");
        List<Selectable> nodes = xpath.xpath("div[@class='prc-grid clearfix'").nodes();
        for (Selectable selectable : nodes) {
            Selectable websiteSelectable = selectable.xpath("///@data-storename");
            PtmMStdSku ptmMStdSku = new PtmMStdSku();
            //获取当前是那个site
            Website website = Website.valueOfString(websiteSelectable.get());
            ptmMStdSku.setWebsite(website);
            Selectable shippingSelectable = selectable.xpath("//div[@class='prc-grid__clmn-3']/div[@class='prc-grid__shpng']/span/text()");
            //默认是0
            int inShippingFee = hasoffer.base.utils.StringUtils.getInt(shippingSelectable.get());
            shippingFeeSiteSet.put(Website.valueOfString(websiteSelectable.get()), -1);
            if (inShippingFee > 0) {
                shippingFeeSiteSet.put(Website.valueOfString(websiteSelectable.get()), inShippingFee);
                ptmMStdSku.setShippingFee(inShippingFee);
            } else {
                //不是数字
                if (shippingSelectable.get().replaceAll(" ", "").toLowerCase().contains("free")) {
                    shippingFeeSiteSet.put(Website.valueOfString(websiteSelectable.get()), 0);
                    ptmMStdSku.setShippingFee(0);
                }
            }
            Selectable urlSelectable = selectable.xpath("//div[@class='prc-grid__clmn-4']/div/@data-url");
            //获取sku的id
            ptmMStdSku.setSourceId(String.valueOf(getMspSkuIdFromUrl(urlSelectable.get())));
            //获取sku的主商品id
            //获取sku的rank
            String cleanUrl = getCleanUrlFromMsp(websiteSelectable.get(), urlSelectable.get());
            ptmMStdSku.setUrl(cleanUrl);
            mStdSkus.add(ptmMStdSku);
        }

        for (PtmMStdSku ptmMStdSku : mStdSkus) {
            for (PtmMStdSku ptmMStdSku1 : trueStdSkuList) {
                if (ptmMStdSku.getWebsite().equals(ptmMStdSku1.getWebsite())) {
                    ptmMStdSku1.setShippingFee(ptmMStdSku.getShippingFee());
                }
            }
        }

        //创建商品
        if (ptmMStdProduct != null) {
            Long pId = ptmMStdProductService.savePtmMStdProduct(ptmMStdProduct);
            AppMspProductDetail appMspProductDetail = new AppMspProductDetail();
            appMspProductDetail.setId(pId);
//            appMspProductDetail.setDescription(String.join(",", descriptionXpath.all()));
            appMspProductDetail.setDescription(descriptionXpath.all().toString());
            mongoDbManager.save(appMspProductDetail);
        }
        //创建sku
        for (PtmMStdSku mStdSku : trueStdSkuList) {
            Long aLong = ptmMStdSkuService.savePtmMStdSkuSinge(mStdSku);
            //因为需要用到ID,但是这时候sku还没有id,所以要先创建id
            AppMspSkuDetail appMspSkuDetail = new AppMspSkuDetail();
            appMspSkuDetail.setId(aLong);
            appMspSkuDetail.setOffers(mStdSku.getOfferList());
            mongoDbManager.save(appMspSkuDetail);
        }
        System.out.println(JSON.toJSONString(ptmMStdProduct, SerializerFeature.PrettyFormat));
        System.out.println(JSON.toJSONString(mStdSkus, SerializerFeature.PrettyFormat));
    }

    private long getMspSkuIdFromUrl(String url) {
        Pattern compile = Pattern.compile("&id=([0-9]{1,20})");
        Matcher matcher = compile.matcher(url);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return 0;
    }

    private long getMspSkuProductIdFromUrl(String url) {
        Pattern compile = Pattern.compile("mspid=([0-9]{1,20})");
        Matcher matcher = compile.matcher(url);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return 0;
    }

    /**
     * 从字符串中获取数字(组合)
     *
     * @param numString 带有数字的字符串
     * @return
     */
    private long getNumberFromString(String numString) {
        String desString = "";
        for (int i = 0; i < numString.length(); i++) {
            if (numString.charAt(i) >= 48 && numString.charAt(i) <= 57) {
                desString += numString.charAt(i);
            }
        }
        if (desString.length() > 0) {
            return Long.parseLong(desString);
        }
        return 0;
    }

    /**
     * @param mspUrl msp的链接
     * @return
     */
    private String getCleanUrlFromMsp(String webSite, String mspUrl) {
        HttpResponseModel httpResponseModel = HtmlUtils.getResponse(mspUrl, 3);
        Html new_html = new Html(httpResponseModel.getBodyString());
        Selectable regexWin = new_html.regex("window.location.replace\\(\"([\\s\\S]*?)\"\\)");
        //shopmonk的链接是这样的http://mysmartprice.go2cloud.org/aff_c?offer_id=61&aff_id=2&aff_sub=electronics&aff_sub2=2017022337849&url=https%3A%2F%2Fshopmonk.com%2Fapple-iphone-6%3Fstorage%3D332%26color%3D113%26utm_source%3Dmysmartprice%26utm_medium%3Daffiliate
        if (webSite.toLowerCase().contains("shopmonk")) {
            mspUrl = cleanUrl(regexWin.get(), Website.SHOPMONK);
        }
        //ebay的链接是这样的http://mysmartprice.go2cloud.org/aff_c?offer_id=59&aff_id=2&aff_sub=electronics&aff_sub2=2017022337854&url=http%3A%2F%2Frover.ebay.com%2Frover%2F1%2F4686-145536-10941-3%2F2%3F%26site%3DPartnership_MSP%26epi=2017022337854%26mpre%3Dhttp%253A%252F%252Fwww.ebay.in%252Fitm%252FApple-iPhone-6-64GB-Silver-VAT-Bill-Apple-India-Warranty-one-year-manufactur-%252F252762352335%253Faff_source%253Dmysmartprice%26aff_source%3Dmysmartprice
        if (webSite.toLowerCase().contains("ebay")) {
            mspUrl = cleanUrl(regexWin.get(), Website.EBAY);
        }
        //amazon http://www.amazon.in/gp/offer-listing/B00O4WTX2G/?sort=price&/ref=as_li_tf_tl?ie=UTF8&camp=3626&creative=24790&creativeASIN=9380349300&linkCode=as2&tag=mysm-21&ascsubtag=2017022337859&condition=new
        if (webSite.toLowerCase().contains("amazon")) {
            mspUrl = cleanUrl(regexWin.get(), Website.AMAZON);
        }
        //flipkart https://dl.flipkart.com/dl/apple-iphone-6-space-grey-64-gb/p/itme8gfcs2dhysgq?pid=MOBEYHZ28FRMNDCW&affid=sulakshanm&affExtParam1=electronics&affExtParam2=2017022337864
        if (webSite.toLowerCase().contains("flipkart")) {
            mspUrl = cleanUrl(regexWin.get(), Website.FLIPKART);
        }
        //tatacliq http://mysmartprice.go2cloud.org/aff_c?offer_id=168&aff_id=2&aff_sub=electronics&aff_sub2=2017022337869&url=https://www.tatacliq.com/apple-iphone-6-64gb-silver/p-mp000000000100299%3Fcid%3Daf%3Aproducts%3Amysmartprice%3Acps%3A2017022337869


        if (webSite.toLowerCase().contains("tatacliq")) {
            mspUrl = cleanUrl(regexWin.get(), Website.TATACLIQ);
        }
        //shopclues http://c.affiliateshopclues.com/?a=27&c=69&p=r&E=AXZHEP%2bFivk%3d&s1=&s2=electronics&s3=2017022337873&ckmrdr=http://www.shopclues.com/iphone-6-64gb-space-grey-4.html%3Fty%3D0%26id%3D5-72-7c25fced-586b-4b37-b1da-e5527341e654%26mcid%3Daff%26tid%3Dnh%26utm_source%3DMysmartprice%26OfferId%3D15
        if (webSite.toLowerCase().contains("shopclues")) {
            mspUrl = cleanUrl(regexWin.get(), Website.SHOPCLUES);
        }
        //snapdeal  暂时没有看到snapdeal的 , 看到了再说吧, 分析的时候先打印出来

        //MYNTRA https://ad.admitad.com/g/s56leml8ck7e8b58392d23d5247706/?subid=fashion&subid2=2017022375624&ulp=http://www.myntra.com/casual-shoes/puma/puma-unisex-grey-lazy-slip-ons/1434758/buy%3Futm_source%3Dadmitad%26utm_medium%3Daffiliate%26utm_campaign%3D367885%26admitad_uid%3D608f491eb50887131140e324fb391349

        if (webSite.toLowerCase().contains("myntra")) {
            mspUrl = cleanUrl(regexWin.get(), Website.MYNTRA);
        }

        //jabong  https://ad.admitad.com/g/hpgbdque4i7e8b58392d0427dca3fa/?subid=fashion&subid2=2017022375967&ulp=http://www.jabong.com/Puma-Lazy-Slip-On-Ii-Dp-Grey-Sneakers-2721290.html%3Futm_source%3Dcpv_admitadmailer%26utm_medium%3Ddc-clicktracker%26utm_campaign%3D367885%26utm_content%3D608f491eb50887131140e324fb391349

        if (webSite.toLowerCase().contains("jabong")) {
            mspUrl = cleanUrl(regexWin.get(), Website.JABONG);
        }

        //CROMA http://mysmartprice.go2cloud.org/aff_c?offer_id=28&aff_id=2&aff_sub=electronics&aff_sub2=2017022374512&url=http://www.croma.com/oppo-f1-gold-16gb-/p/195677%3Fcm_mmc%3Dmysmartprice-_-affiliates-_-offers-_-na%26utm_source%3Dmysmartprice%26utm_medium%3Daffiliates%26utm_campaign%3Dna

        if (webSite.toLowerCase().contains("croma")) {
            mspUrl = cleanUrl(regexWin.get(), Website.CROMA);
        }
        //INFIBEAM; https://www.infibeam.com/Mobiles/oppo-oppo-f1-plus-64gb/P-mobi-24993014468-cat-z.html?trackId=gadget_store&subTrackId=2017022374902#variantId=P-mobi-24993014468

        if (webSite.toLowerCase().contains("infibeam")) {
            mspUrl = cleanUrl(regexWin.get(), Website.INFIBEAM);
        }
        return mspUrl;
    }

    private Date getDateFromStr(String mspDateStr) {
        //2017-03-02T09:29:47+05:30
        //2017-03-02 09:29:47 05:30
        //2017-03-02 09:29:47
        String tempDateStr = mspDateStr.replaceAll("[T|\\+]", " ");
        String[] split = tempDateStr.split(" ");
        if (split.length > 2l) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(split[0] + split[1]);
            } catch (ParseException e) {
                return null;
            }
        }
        return null;
    }
}
