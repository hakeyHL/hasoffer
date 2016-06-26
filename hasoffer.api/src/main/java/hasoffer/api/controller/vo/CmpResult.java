package hasoffer.api.controller.vo;

import hasoffer.base.model.PageableResult;

import java.util.List;

/**
 * Date : 2016/5/27
 * Function :
 */
public class CmpResult {
    //the lowest price
    private  float bestPrice;
    //productname
    private  String name;
    //total comment number
    private  Long totalRatingsNum;
    //comment star number
    private  float ratingNum;
    PageableResult<CmpProductListVo> priceList;
    //product specification
    private  String specs;
    float priceOff;
    ProductVo productVo;
    PageableResult<ComparedSkuVo> pagedComparedSkuVos;
    private String [] images;
    public CmpResult(float priceOff, ProductVo productVo, PageableResult<ComparedSkuVo> pagedComparedSkuVos) {
        this.priceOff = priceOff;
        this.productVo = productVo;
        this.pagedComparedSkuVos = pagedComparedSkuVos;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public float getPriceOff() {
        return priceOff;
    }

    public void setPriceOff(float priceOff) {
        this.priceOff = priceOff;
    }

    public ProductVo getProductVo() {
        return productVo;
    }

    public void setProductVo(ProductVo productVo) {
        this.productVo = productVo;
    }

    public PageableResult<ComparedSkuVo> getPagedComparedSkuVos() {
        return pagedComparedSkuVos;
    }

    public void setPagedComparedSkuVos(PageableResult<ComparedSkuVo> pagedComparedSkuVos) {
        this.pagedComparedSkuVos = pagedComparedSkuVos;
    }

    public float getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(float bestPrice) {
        this.bestPrice = bestPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotalRatingsNum() {
        return totalRatingsNum;
    }

    public void setTotalRatingsNum(Long totalRatingsNum) {
        this.totalRatingsNum = totalRatingsNum;
    }

    public float getRatingNum() {
        return ratingNum;
    }

    public void setRatingNum(float ratingNum) {
        this.ratingNum = ratingNum;
    }

    public PageableResult<CmpProductListVo> getPriceList() {
        return priceList;
    }

    public void setPriceList(PageableResult<CmpProductListVo> priceList) {
        this.priceList = priceList;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public CmpResult() {
    }
}
