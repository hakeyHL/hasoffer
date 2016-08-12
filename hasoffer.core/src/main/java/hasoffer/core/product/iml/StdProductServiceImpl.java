package hasoffer.core.product.iml;

import hasoffer.affiliate.model.FlipkartAttribute;
import hasoffer.affiliate.model.FlipkartSkuInfo;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmStdDef;
import hasoffer.core.persistence.po.ptm.PtmStdProduct;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.persistence.po.ptm.PtmStdSkuValue;
import hasoffer.core.product.IStdProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by chevy on 2016/8/12.
 */
@Service
public class StdProductServiceImpl implements IStdProductService {

    @Resource
    IDataBaseManager dbm;

    @Override
    public void createStd(Map<String, FlipkartSkuInfo> skuInfoMap) {
        skuInfoMap.keySet();
        Set<Map.Entry<String, FlipkartSkuInfo>> skuInfoSet = skuInfoMap.entrySet();
        Iterator<Map.Entry<String, FlipkartSkuInfo>> it = skuInfoSet.iterator();

        // No.1
        Map.Entry<String, FlipkartSkuInfo> kv = it.next();
        FlipkartSkuInfo skuInfo = kv.getValue();

        // build std product
        String productName = skuInfo.getTitle();
        String brandName = skuInfo.getProductBrand();
        String modelName = skuInfo.getModelName();
        String desc = skuInfo.getDesc();

        PtmStdProduct stdProduct = new PtmStdProduct(productName, brandName, modelName, desc);

        // create product
        // dbm.create(stdProduct);
        do {
            createStdSku(stdProduct.getId(), skuInfo);

            if (it.hasNext()) {
                kv = it.next();
                skuInfo = kv.getValue();
            } else {
                break;
            }
        } while (skuInfo != null);

        System.out.println(skuInfoMap.size());
    }

    private void createStdSku(long stdProductId, FlipkartSkuInfo fsi) {
        PtmStdSku stdSku = new PtmStdSku(stdProductId, fsi.getTitle(), fsi.getFlipkartSellingPrice().getAmount());
        // create sku
        // dbm.create(stdSku);

        createStdSkuValues(stdSku.getId(), fsi.getAttributes());
    }

    private void createStdSkuValues(long stdSkuId, FlipkartAttribute fa) {

        String color = fa.getColor();
        String displaySize = fa.getDisplaySize();
        String size = fa.getSize();
        String sizeUnit = fa.getSizeUnit();
        String storage = fa.getStorage();

        setStdSkuValue(stdSkuId, "color", color);
        setStdSkuValue(stdSkuId, "displaySize", displaySize);
        setStdSkuValue(stdSkuId, "size", size);
        setStdSkuValue(stdSkuId, "sizeUnit", sizeUnit);
        setStdSkuValue(stdSkuId, "storage", storage);
    }

    private void setStdSkuValue(long stdSkuId, String stdName, String value) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(value.trim())) {
            return;
        }

        PtmStdDef ptmStdDef = new PtmStdDef(stdName);
        dbm.createIfNoExist(ptmStdDef);

        PtmStdSkuValue stdSkuValue = new PtmStdSkuValue(stdSkuId, ptmStdDef.getId(), ptmStdDef.getStdName(), value);
        dbm.create(stdSkuValue);
    }
}
