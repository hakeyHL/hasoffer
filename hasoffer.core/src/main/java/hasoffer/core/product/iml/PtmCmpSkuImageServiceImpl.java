package hasoffer.core.product.iml;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import hasoffer.core.product.IPtmCmpSkuImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2016/8/1.
 */
@Service
public class PtmCmpSkuImageServiceImpl implements IPtmCmpSkuImageService {

    private final String Q_APP_IMAGES_PRODUCTID = "SELECT t FROM PtmCmpSkuImage t  where t.id=?0";

    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPtmCmpSkuImage(PtmCmpSkuImage ptmCmpSkuImage) {
        dbm.create(ptmCmpSkuImage);
    }

    @Override
    public List<PtmCmpSkuImage> ptmCmpSkuImages(Long productId) {
        return dbm.query(Q_APP_IMAGES_PRODUCTID, Arrays.asList(productId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(long ptmcmpskuid) {
        dbm.delete(PtmCmpSkuImage.class, ptmcmpskuid);
    }
}
