package hasoffer.api.controller.vo;

import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016/6/25.
 */
public class CmpProductListVo {
    List<String> support = new ArrayList<String>();
    private String image;
    private float ratingNum;
    private Long totalRatingsNum;
    private int price;
    private float freight;
    private String distributionTime;
    private Long coins;
    private float backRate;
    private int returnGuarantee;
    private String deepLink;
    private String deepLinkUrl;
    private Website website;

    public CmpProductListVo() {
    }

    public CmpProductListVo(PtmCmpSku cmpSku, String logoImage) {
        this.coins = cmpSku.getWebsite() == Website.SHOPCLUES || cmpSku.getWebsite() == Website.FLIPKART ? Math.round(0.015 * cmpSku.getPrice()) : 0;
        this.ratingNum = cmpSku.getRatings();
        this.totalRatingsNum = cmpSku.getCommentsNumber();
        this.image = logoImage;
        this.ratingNum = Long.valueOf(cmpSku.getRating() == null ? "0" : cmpSku.getRating());
        this.price = Math.round(cmpSku.getPrice());
        this.website = cmpSku.getWebsite();
        this.freight = cmpSku.getShipping();
        this.distributionTime = cmpSku.getDeliveryTime();
        this.backRate = cmpSku.getWebsite() == Website.SHOPCLUES || cmpSku.getWebsite() == Website.FLIPKART ? 1.5f : 0;
        this.returnGuarantee = cmpSku.getReturnDays();
        String payMethod = cmpSku.getSupportPayMethod();
        if (!StringUtils.isEmpty(payMethod)) {
            String[] temps = payMethod.split(",");
            for (String str : temps) {
                this.support.add(str);
            }
        }
    }

    public CmpProductListVo(String image, float ratingNum, Long totalRatingsNum, int price, int freight, String distributionTime, Long coins, float backRate, int returnGuarantee, List<String> support) {
        this.image = image;
        this.ratingNum = ratingNum;
        this.totalRatingsNum = totalRatingsNum;
        this.price = price;
        this.freight = freight;
        this.distributionTime = distributionTime;
        this.coins = coins;
        this.backRate = backRate;
        this.returnGuarantee = returnGuarantee;
        this.support = support;
    }

    public String getImage() {

        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getDeepLinkUrl() {
        return deepLinkUrl;
    }

    public void setDeepLinkUrl(String deepLinkUrl) {
        this.deepLinkUrl = deepLinkUrl;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public float getRatingNum() {
        return ratingNum;
    }

    public void setRatingNum(float ratingNum) {
        this.ratingNum = ratingNum;
    }

    public Long getTotalRatingsNum() {
        return totalRatingsNum;
    }

    public void setTotalRatingsNum(Long totalRatingsNum) {
        this.totalRatingsNum = totalRatingsNum;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setFreight(float freight) {
        this.freight = freight;
    }

    public float getFreight() {
        return freight;
    }

    public void setFreight(int freight) {
        this.freight = freight;
    }

    public String getDistributionTime() {
        return distributionTime;
    }

    public void setDistributionTime(String distributionTime) {
        this.distributionTime = distributionTime;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public float getBackRate() {
        return backRate;
    }

    public void setBackRate(float backRate) {
        this.backRate = backRate;
    }

    public int getReturnGuarantee() {
        return returnGuarantee;
    }

    public void setReturnGuarantee(int returnGuarantee) {
        this.returnGuarantee = returnGuarantee;
    }

    public List<String> getSupport() {
        return support;
    }

    public void setSupport(List<String> support) {
        this.support = support;
    }
}
