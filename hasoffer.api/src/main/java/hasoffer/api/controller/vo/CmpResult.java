package hasoffer.api.controller.vo;

import hasoffer.base.model.PageableResult;

/**
 * Date : 2016/5/27
 * Function :
 */
public class CmpResult {
    float priceOff;
    ProductVo productVo;
    PageableResult<ComparedSkuVo> pagedComparedSkuVos;

    public CmpResult(float priceOff, ProductVo productVo, PageableResult<ComparedSkuVo> pagedComparedSkuVos) {
        this.priceOff = priceOff;
        this.productVo = productVo;
        this.pagedComparedSkuVos = pagedComparedSkuVos;
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
}
