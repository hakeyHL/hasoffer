package hasoffer.api.controller.vo;

import hasoffer.core.persistence.po.ptm.PtmCmpSku;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016/6/25.
 */
public class CmpProductListVo {
    private String image;
    private float ratingNum;
    private Long totalRatingsNum;
    private float price;
    private int freight;
    private int distributionTime;
    private Long coins;
    private float backRate;
    private int returnGuarantee;
    private  String deepLink;
    private  String deepLinkUrl;
    List<String> support = new ArrayList<String>();

    public CmpProductListVo() {
    }
    public CmpProductListVo(PtmCmpSku cmpSku,String LogoImage) {
        this.image = LogoImage;
        this.ratingNum =Long.valueOf(cmpSku.getRating()==null?"0":cmpSku.getRating());
        this.price = cmpSku.getPrice();
        this.freight = 20;
        this.distributionTime = 10;
        this.coins =Long.valueOf(20);
        this.backRate = 0.015f;
        this.returnGuarantee = 5;
        this.support = null;
        this.image = cmpSku.getBigImagePath();
    }
    public CmpProductListVo(String image, float ratingNum, Long totalRatingsNum, float price, int freight, int distributionTime, Long coins, float backRate, int returnGuarantee, List<String> support) {
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

    public void setImage(String image) {
        this.image = image;
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getFreight() {
        return freight;
    }

    public void setFreight(int freight) {
        this.freight = freight;
    }

    public int getDistributionTime() {
        return distributionTime;
    }

    public void setDistributionTime(int distributionTime) {
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
