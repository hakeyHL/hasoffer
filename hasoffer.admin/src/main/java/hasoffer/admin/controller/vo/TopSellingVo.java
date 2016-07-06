package hasoffer.admin.controller.vo;

/**
 * Created on 2016/7/6.
 */
public class TopSellingVo {

    private long id;
    private String name;
    private long productId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
