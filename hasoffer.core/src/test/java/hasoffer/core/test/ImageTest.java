package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.core.bo.common.ImagePath;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.utils.ImageUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Date : 2016/5/30
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ImageTest {

    private static final String Q_SKU_IMAGE =
            "SELECT t FROM PtmCmpSku t WHERE t.imagePath IS NULL AND t.oriImageUrl IS NOT NULL AND t.failLoadImage = 0";
    @Resource
    IDataBaseManager dbm;

    @Test
    public void f() throws Exception {
        String url = "http://d.hiphotos.baidu.com/zhidao/pic/item/e7cd7b899e510fb38cc9205dd833c895d0430c23.jpg";

        ImagePath imagePath = ImageUtil.downloadAndUpload2(url);

        System.out.println(imagePath.toString());
    }

    @Test
    public void download() {
        PageableResult<PtmCmpSku> pagedSkus = dbm.queryPage(Q_SKU_IMAGE, 1, 10);

        System.out.println(pagedSkus.getNumFund());
    }
}
