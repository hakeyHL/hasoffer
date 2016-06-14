package hasoffer.admin.controller.vo;

import hasoffer.base.model.Website;

import java.util.Date;

/**
 * Created on 2016/5/31.
 */
public class StatPtmCmpSkuUpdateVo {

    private Date date;
    private Website website;
    private long onSaleAmount;
    private long soldOutAmount;
    private long offsaleAmount;
    private long updateSuccessAmount;
    private long alwaysFailAmount;
    private long newSkuAmount;
    private long indexAmount;
    private long newIndexAmount;
    private Date updateTime;
    private long proportion;

    public long getProportion() {
        return proportion;
    }

    public void setProportion(long proportion) {
        this.proportion = proportion;
    }

    public long getAlwaysFailAmount() {
        return alwaysFailAmount;
    }

    public void setAlwaysFailAmount(long alwaysFailAmount) {
        this.alwaysFailAmount = alwaysFailAmount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getIndexAmount() {
        return indexAmount;
    }

    public void setIndexAmount(long indexAmount) {
        this.indexAmount = indexAmount;
    }

    public long getNewIndexAmount() {
        return newIndexAmount;
    }

    public void setNewIndexAmount(long newIndexAmount) {
        this.newIndexAmount = newIndexAmount;
    }

    public long getNewSkuAmount() {
        return newSkuAmount;
    }

    public void setNewSkuAmount(long newSkuAmount) {
        this.newSkuAmount = newSkuAmount;
    }

    public long getOffsaleAmount() {
        return offsaleAmount;
    }

    public void setOffsaleAmount(long offsaleAmount) {
        this.offsaleAmount = offsaleAmount;
    }

    public long getOnSaleAmount() {
        return onSaleAmount;
    }

    public void setOnSaleAmount(long onSaleAmount) {
        this.onSaleAmount = onSaleAmount;
    }

    public long getSoldOutAmount() {
        return soldOutAmount;
    }

    public void setSoldOutAmount(long soldOutAmount) {
        this.soldOutAmount = soldOutAmount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public long getUpdateSuccessAmount() {
        return updateSuccessAmount;
    }

    public void setUpdateSuccessAmount(long updateSuccessAmount) {
        this.updateSuccessAmount = updateSuccessAmount;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }
}
