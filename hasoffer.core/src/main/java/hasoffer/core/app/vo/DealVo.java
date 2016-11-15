package hasoffer.core.app.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hs on 2016/6/21.
 */
public class DealVo {
    private Long id;
    private String image;
    private String title;
    private String exp;
    private Double extra;
    private String link;
    private String logoUrl;
    private String website;
    private String priceDescription;
    private String deepLink;
    private int discount;
    private boolean isExpired;
    private float originPrice;
    private float presentPrice;
    //deal type ,0:general ;1:hot ;etc ,default is general
    private int type = 0;
    private Long thumbNumber; //点赞数
    private Long commentNumber;//评论数
    private String createTime;

    public String getPriceDescription() {
        return priceDescription;
    }

    public void setPriceDescription(String priceDescription) {
        this.priceDescription = priceDescription;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(Date time) {
        this.exp = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH).format(time);
    }

    public Double getExtra() {
        return extra;
    }

    public void setExtra(Double extra) {
        this.extra = extra;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public float getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(float originPrice) {
        this.originPrice = originPrice;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getPresentPrice() {
        return presentPrice;
    }

    public void setPresentPrice(float presentPrice) {
        this.presentPrice = presentPrice;
    }

    public Long getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(Long commentNumber) {
        this.commentNumber = commentNumber;
    }

    public Long getThumbNumber() {
        return thumbNumber;
    }

    public void setThumbNumber(Long thumbNumber) {
        this.thumbNumber = thumbNumber;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
