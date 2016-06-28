package hasoffer.api.controller.vo;

/**
 * 商品列表Vo
 * Created by hs on 2016/6/21.
 */
public class ProductListVo {
    private  Long id;
    private  String name;
    private double price;
    private  int storesNum;
    private int commentNum;
    private  String  imageUrl;
    private  float ratingNum;

    public float getRatingNum() {
        return ratingNum;
    }

    public void setRatingNum(float ratingNum) {
        this.ratingNum = ratingNum;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStoresNum() {
        return storesNum;
    }

    public void setStoresNum(int storesNum) {
        this.storesNum = storesNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }
}
