package hasoffer.core.test;

import hasoffer.core.bo.common.ImagePath;
import hasoffer.core.utils.ImageUtil;
import org.junit.Test;

/**
 * Date : 2016/5/30
 * Function :
 */
public class ImageTest {


    @Test
    public void f() throws Exception {
        String url = "http://d.hiphotos.baidu.com/zhidao/pic/item/e7cd7b899e510fb38cc9205dd833c895d0430c23.jpg";

        ImagePath imagePath = ImageUtil.downloadAndUpload2(url);

        System.out.println(imagePath.toString());
    }

}
