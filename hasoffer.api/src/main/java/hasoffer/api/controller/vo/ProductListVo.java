package hasoffer.api.controller.vo;

import java.math.BigDecimal;

/**
 * ��Ʒ�б�Vo
 * Created by hs on 2016/6/21.
 */
public class ProductListVo {
    private  Long id;
    private  String name;
    private double price;
    private  Long storesNum;
    private Long commentNum;

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

    public Long getStoresNum() {
        return storesNum;
    }

    public void setStoresNum(Long storesNum) {
        this.storesNum = storesNum;
    }

    public Long getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(Long commentNum) {
        this.commentNum = commentNum;
    }
}