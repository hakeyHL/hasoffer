package hasoffer.fetch.sites.amazon.ext.model;

/**
 * Created on 2016/9/5.
 */
public class UsaAmazonData {

    private String link;
    private String title;
    private String imageUrl;
    private float price;
    private float disPrice;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getDisPrice() {
        return disPrice;
    }

    public void setDisPrice(float disPrice) {
        this.disPrice = disPrice;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
