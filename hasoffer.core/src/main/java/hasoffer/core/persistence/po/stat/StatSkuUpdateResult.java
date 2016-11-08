package hasoffer.core.persistence.po.stat;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Date : 2016/1/22
 * Function : 统计每天价格更新了多少数据
 */
@Entity
public class StatSkuUpdateResult implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    private String id; // ymd

    private long flipkartTotal = 0;
    private long flipkartSuccess = 0;

    private long amazonTotal = 0;
    private long amazonSuccess = 0;

    private long snapdealTotal = 0;
    private long snapdealSuccess = 0;

    private long ebayotal = 0;
    private long ebaySuccess = 0;

    private long shopcluesTotal = 0;
    private long shopcluesSuccess = 0;

    private long paytmTotal = 0;
    private long paytmSuccess = 0;

    private long myntraTotal = 0;
    private long myntraSuccess = 0;

    private long infibeamTotal = 0;//INFIBEAM
    private long infibeamSuccess = 0;

    private long allTotal = 0;
    private long allSuccess = 0;

    public StatSkuUpdateResult(String ymd) {
        this.id = ymd;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public long getFlipkartTotal() {
        return flipkartTotal;
    }

    public void setFlipkartTotal(long flipkartTotal) {
        this.flipkartTotal = flipkartTotal;
    }

    public long getFlipkartSuccess() {
        return flipkartSuccess;
    }

    public void setFlipkartSuccess(long flipkartSuccess) {
        this.flipkartSuccess = flipkartSuccess;
    }

    public long getAmazonTotal() {
        return amazonTotal;
    }

    public void setAmazonTotal(long amazonTotal) {
        this.amazonTotal = amazonTotal;
    }

    public long getAmazonSuccess() {
        return amazonSuccess;
    }

    public void setAmazonSuccess(long amazonSuccess) {
        this.amazonSuccess = amazonSuccess;
    }

    public long getSnapdealTotal() {
        return snapdealTotal;
    }

    public void setSnapdealTotal(long snapdealTotal) {
        this.snapdealTotal = snapdealTotal;
    }

    public long getSnapdealSuccess() {
        return snapdealSuccess;
    }

    public void setSnapdealSuccess(long snapdealSuccess) {
        this.snapdealSuccess = snapdealSuccess;
    }

    public long getEbayotal() {
        return ebayotal;
    }

    public void setEbayTotal(long ebayotal) {
        this.ebayotal = ebayotal;
    }

    public long getEbaySuccess() {
        return ebaySuccess;
    }

    public void setEbaySuccess(long ebaySuccess) {
        this.ebaySuccess = ebaySuccess;
    }

    public long getShopcluesTotal() {
        return shopcluesTotal;
    }

    public void setShopcluesTotal(long shopcluesTotal) {
        this.shopcluesTotal = shopcluesTotal;
    }

    public long getShopcluesSuccess() {
        return shopcluesSuccess;
    }

    public void setShopcluesSuccess(long shopcluesSuccess) {
        this.shopcluesSuccess = shopcluesSuccess;
    }

    public long getPaytmTotal() {
        return paytmTotal;
    }

    public void setPaytmTotal(long paytmTotal) {
        this.paytmTotal = paytmTotal;
    }

    public long getPaytmSuccess() {
        return paytmSuccess;
    }

    public void setPaytmSuccess(long paytmSuccess) {
        this.paytmSuccess = paytmSuccess;
    }

    public long getMyntraTotal() {
        return myntraTotal;
    }

    public void setMyntraTotal(long myntraTotal) {
        this.myntraTotal = myntraTotal;
    }

    public long getMyntraSuccess() {
        return myntraSuccess;
    }

    public void setMyntraSuccess(long myntraSuccess) {
        this.myntraSuccess = myntraSuccess;
    }

    public long getInfibeamTotal() {
        return infibeamTotal;
    }

    public void setInfibeamTotal(long infibeamTotal) {
        this.infibeamTotal = infibeamTotal;
    }

    public long getInfibeamSuccess() {
        return infibeamSuccess;
    }

    public void setInfibeamSuccess(long infibeamSuccess) {
        this.infibeamSuccess = infibeamSuccess;
    }

    public long getAllTotal() {
        return allTotal;
    }

    public void setAllTotal(long allTotal) {
        this.allTotal = allTotal;
    }

    public long getAllSuccess() {
        return allSuccess;
    }

    public void setAllSuccess(long allSuccess) {
        this.allSuccess = allSuccess;
    }
}
