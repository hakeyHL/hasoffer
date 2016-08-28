package hasoffer.core.persistence.mongo;

import java.util.Date;

/**
 * Created by chevy on 2016/8/27.
 */
public class PriceNode {

    private Date priceTime;
    private long priceTimeL;
    private float price;

    public PriceNode(Date priceTime, float price) {
        this.priceTime = priceTime;
        this.price = price;
        this.priceTimeL = this.priceTime.getTime();
    }

    public Date getPriceTime() {
        return priceTime;
    }

    public void setPriceTime(Date priceTime) {
        this.priceTime = priceTime;
    }

    public long getPriceTimeL() {
        return priceTimeL;
    }

    public void setPriceTimeL(long priceTimeL) {
        this.priceTimeL = priceTimeL;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
