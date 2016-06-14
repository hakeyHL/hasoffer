package hasoffer.core.test;

import hasoffer.core.utils.ImageUtil;
import org.junit.Test;

/**
 * Date : 2016/5/30
 * Function :
 */
public class ImageTest {


    @Test
    public void f() throws Exception {
        String url = "http://i3.itc.cn/20160530/370f_871bc5fb_e7ab_5e26_beb7_a5a5cdcea240_1.jpg";

        System.out.println(ImageUtil.downloadAndUpload(url));

    }

}
