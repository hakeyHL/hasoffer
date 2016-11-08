package hasoffer.core.bo.product;

import java.util.concurrent.atomic.AtomicInteger;

public class SkuUpdateResult {

    private String ymd; // ymd

    private AtomicInteger flipkartTotal = new AtomicInteger(0);
    private AtomicInteger flipkartSuccess = new AtomicInteger(0);

    private AtomicInteger amazonTotal = new AtomicInteger(0);
    private AtomicInteger amazonSuccess = new AtomicInteger(0);

    private AtomicInteger snapdealTotal = new AtomicInteger(0);
    private AtomicInteger snapdealSuccess = new AtomicInteger(0);

    private AtomicInteger ebayotal = new AtomicInteger(0);
    private AtomicInteger ebaySuccess = new AtomicInteger(0);

    private AtomicInteger shopcluesTotal = new AtomicInteger(0);
    private AtomicInteger shopcluesSuccess = new AtomicInteger(0);

    private AtomicInteger paytmTotal = new AtomicInteger(0);
    private AtomicInteger paytmSuccess = new AtomicInteger(0);

    private AtomicInteger myntraTotal = new AtomicInteger(0);
    private AtomicInteger myntraSuccess = new AtomicInteger(0);

    private AtomicInteger infibeamTotal = new AtomicInteger(0);
    private AtomicInteger infibeamSuccess = new AtomicInteger(0);

    private AtomicInteger allTotal = new AtomicInteger(0);
    private AtomicInteger allSuccess = new AtomicInteger(0);

    public SkuUpdateResult(String ymd) {
        this.ymd = ymd;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public long getFlipkartTotal() {
        return flipkartTotal.get();
    }

    public void addFlipkartTotal() {
        flipkartTotal.addAndGet(1);
    }

    public long getFlipkartSuccess() {
        return flipkartSuccess.get();
    }

    public void addFlipkartSuccess() {
        flipkartSuccess.addAndGet(1);
    }

    public long getAmazonTotal() {
        return amazonTotal.get();
    }

    public void addAmazonTotal() {
        amazonTotal.addAndGet(1);
    }

    public long getAmazonSuccess() {
        return amazonSuccess.get();
    }

    public void addAmazonSuccess() {
        amazonSuccess.addAndGet(1);
    }

    public long getSnapdealTotal() {
        return snapdealTotal.get();
    }

    public void addSnapdealTotal() {
        snapdealTotal.addAndGet(1);
    }

    public long getSnapdealSuccess() {
        return snapdealSuccess.get();
    }

    public void addSnapdealSuccess() {
        snapdealSuccess.addAndGet(1);
    }

    public long getEbayTotal() {
        return ebayotal.get();
    }

    public void addEbayotal() {
        ebayotal.addAndGet(1);
    }

    public long getEbaySuccess() {
        return ebaySuccess.get();
    }

    public void addEbaySuccess() {
        ebaySuccess.addAndGet(1);
    }

    public long getShopcluesTotal() {
        return shopcluesTotal.get();
    }

    public void addShopcluesTotal() {
        shopcluesTotal.addAndGet(1);
    }

    public long getShopcluesSuccess() {
        return shopcluesSuccess.get();
    }

    public void addShopcluesSuccess() {
        shopcluesSuccess.addAndGet(1);
    }

    public long getPaytmTotal() {
        return paytmTotal.get();
    }

    public void addPaytmTotal() {
        paytmTotal.addAndGet(1);
    }

    public long getPaytmSuccess() {
        return paytmSuccess.get();
    }

    public void addPaytmSuccess() {
        paytmSuccess.addAndGet(1);
    }

    public long getMyntraTotal() {
        return myntraTotal.get();
    }

    public void addMyntraTotal() {
        myntraTotal.addAndGet(1);
    }

    public long getMyntraSuccess() {
        return myntraSuccess.get();
    }

    public void addMyntraSuccess() {
        myntraSuccess.addAndGet(1);
    }

    public long getInfibeamTotal() {
        return infibeamTotal.get();
    }

    public void addInfibeamTotal() {
        infibeamTotal.addAndGet(1);
    }

    public long getInfibeamSuccess() {
        return infibeamSuccess.get();
    }

    public void addInfibeamSuccess() {
        infibeamSuccess.addAndGet(1);
    }

    public long getAllTotal() {
        return allTotal.get();
    }

    public void addAllTotal() {
        allTotal.addAndGet(1);
    }

    public long getAllSuccess() {
        return allSuccess.get();
    }

    public void addAllSuccess() {
        allSuccess.addAndGet(1);
    }

    @Override
    public String toString() {
        return "SkuUpdateResult{" +
                "ymd='" + ymd + '\'' +
                ", flipkartTotal=" + getFlipkartTotal() +
                ", flipkartSuccess=" + getFlipkartSuccess() +
                ", amazonTotal=" + getAmazonTotal() +
                ", amazonSuccess=" + getAmazonSuccess() +
                ", snapdealTotal=" + getSnapdealTotal() +
                ", snapdealSuccess=" + getSnapdealSuccess() +
                ", ebayotal=" + getEbayTotal() +
                ", ebaySuccess=" + getEbaySuccess() +
                ", shopcluesTotal=" + getShopcluesTotal() +
                ", shopcluesSuccess=" + getShopcluesSuccess() +
                ", paytmTotal=" + getPaytmTotal() +
                ", paytmSuccess=" + getPaytmSuccess() +
                ", myntraTotal=" + getMyntraTotal() +
                ", myntraSuccess=" + getMyntraSuccess() +
                ", infibeamTotal=" + getInfibeamTotal() +
                ", infibeamSuccess=" + getInfibeamSuccess() +
                ", allTotal=" + getAllTotal() +
                ", allSuccess=" + getAllSuccess() +
                '}';
    }
}
