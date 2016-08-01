package hasoffer.core.product.iml;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import hasoffer.core.product.IPtmCmpSkuImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created on 2016/8/1.
 */
@Service
public class PtmCmpSkuImageServiceImpl implements IPtmCmpSkuImageService {

    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPtmCmpSkuImage(PtmCmpSkuImage ptmCmpSkuImage) {
        dbm.create(ptmCmpSkuImage);
    }
}
