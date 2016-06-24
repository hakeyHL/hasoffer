import hasoffer.base.utils.IDUtil;
import hasoffer.core.utils.ImageUtil;
import jodd.io.FileUtil;
import org.junit.Test;

import java.io.*;

/**
 * Created by lihongde on 2016/6/24 14:22
 */
public class TestFileUpload {

    @Test
    public void upload(){
        String fileName = "D:\\Capture001.png";
        File file = new File(fileName);
        try {
            File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
            FileUtil.writeBytes(imageFile,getBytesFromFile(file));
            String path = ImageUtil.uploadImage(imageFile);
            System.out.println(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        int n;
        while ((n = stream.read(b)) != -1)
        out.write(b, 0, n);
        stream.close();
        out.close();
        return out.toByteArray();
    }
}
