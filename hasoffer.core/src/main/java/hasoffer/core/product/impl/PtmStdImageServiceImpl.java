package hasoffer.core.product.impl;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmStdImage;
import hasoffer.core.product.IPtmStdImageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hs on 2016年11月29日.
 * Time 15:05
 */
@Service
public class PtmStdImageServiceImpl implements IPtmStdImageService {
    private static final String PTMSTDIMAGE_GET_IMAGEURL_BY_SKUID = "select t from PtmStdImage t where t.stdSkuId=?0 order by id asc ";
    @Resource
    IDataBaseManager dbm;

    @Override
    public List<PtmStdImage> getStdSkuImageBySkuId(Long stdSkuId) {
        return dbm.query(PTMSTDIMAGE_GET_IMAGEURL_BY_SKUID, Arrays.asList(stdSkuId));
    }
}
