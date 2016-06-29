package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
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
        String url = "http://img20.360buyimg.com/da/jfs/t2752/14/2642732614/92420/3092a687/576c9acbNcb1a36b0.jpg";

//        ImagePath imagePath = ImageUtil.downloadAndUpload2(url);
//        System.out.println(imagePath.toString());

        String imagePath = ImageUtil.downloadAndUpload(url);
        System.out.println(imagePath);
    }

    @Test
    public void download() {
        PageableResult<PtmCmpSku> pagedSkus = dbm.queryPage(Q_SKU_IMAGE, 1, 10);

        System.out.println(pagedSkus.getNumFund());
    }
}
