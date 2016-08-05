package hasoffer.core.product;

import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;

import java.util.List;

/**
 * Created on 2016/8/1.
 */
public interface IPtmCmpSkuImageService {

    void createPtmCmpSkuImage(PtmCmpSkuImage ptmCmpSkuImage);

    List<PtmCmpSkuImage> ptmCmpSkuImages(Long productId);

    void delete(long ptmcmpskuid);
}
