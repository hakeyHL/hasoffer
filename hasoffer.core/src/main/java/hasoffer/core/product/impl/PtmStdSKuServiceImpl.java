package hasoffer.core.product.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Created by hs on 2016年11月28日.
 * Time 17:21
 */
@Service
public class PtmStdSKuServiceImpl implements IPtmStdSkuService {
    private static final String SOLR_GET_PTMSTDSKU_BY_MINID = " select t from PtmStdSku t where id >= ?0";
    Logger logger = LoggerFactory.getLogger(PtmStdSKuServiceImpl.class);
    @Resource
    private IDataBaseManager dbm;
    @Resource
    private PtmStdSkuIndexServiceImpl ptmStdSkuIndexServicel;

    @Override
    public PtmStdSku getStdSkuById(Long id) {
        return dbm.get(PtmStdSku.class, id);
    }

    @Override
    public PageableResult<PtmStdSku> getPtmStdSkuListByMinId(Long minId, int page, int pageSize) {
        return dbm.queryPage(SOLR_GET_PTMSTDSKU_BY_MINID, page, pageSize, Arrays.asList(minId));
    }

    @Override
    public void importPtmStdSku2Solr(PtmStdSku ptmStdSku) {
        //导入sku(product)到solr
        if (ptmStdSku == null) {
            return;
        }
        PtmStdSku ptmStdSku1 = dbm.get(PtmStdSku.class, ptmStdSku.getId());
        if (ptmStdSku1 == null) {
            //delete it from solr ,if it exist .
            ptmStdSkuIndexServicel.remove(ptmStdSku.getId() + "");
            return;
        }
        PtmStdSkuModel ptmStdSKuModel = getPtmStdSKuModel(ptmStdSku1);
        if (ptmStdSKuModel == null) {
            ptmStdSkuIndexServicel.remove(ptmStdSku.getId() + "");
            return;
        } else {
            ptmStdSkuIndexServicel.createOrUpdate(ptmStdSKuModel);
        }
    }

    public PtmStdSkuModel getPtmStdSKuModel(PtmStdSku ptmStdSku1) {
        return null;
    }

}
