package hasoffer.core.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by glx on 2015/10/14.
 * 图片路径的规则
 * /资源类型/资源id的md5(16)/资源id/图片编码/图片类型(o|s|m|b)_wxh.jpg?_t={时间戳}
 * 其中md516需要分割，如下方式
 * 000/000/000/000/000/0
 * 例如，对于id为2的sku的第0个图片（主图,原图），其图片的路径为
 * /sku/9d4/c2f/636/f06/7f8/9/2/0/o_1920x389.jpg?_t=1122455852233
 */
public class ImageServerUtil {
    private static final Logger logger = LoggerFactory.getLogger(ImageServerUtil.class);

    /**
     * 不要用！
     *
     * @param imgFile
     * @param path
     */
    public static void save(File imgFile, String path) {
//        FileServer.getInstance().save(imgFile, path);
    }

    public static String scaleAndSave(ResourceType resourceType,
                                      Object resourceId,
                                      short index,
                                      ImageSize imageSize,
                                      File originalFile) {
        File tempFile = null;
        int width = 0;
        int height = 0;
        try {
            if (ImageSize.ORIGINAL.equals(imageSize)) {
                ImageUtil.ImageSize newSize = ImageUtil.getImageSize(originalFile);
                width = newSize.getWidth();
                height = newSize.getHeight();
            } else {
                tempFile = File.createTempFile("allbuy-image", ".jpg");
                ImageUtil.ImageSize newSize = ImageUtil.scale(originalFile, tempFile, imageSize.getWidth(), imageSize.getHeight());
                width = newSize.getWidth();
                height = newSize.getHeight();
            }
        } catch (IOException e) {
            logger.error("{}", e);
            return null;
        }

        String path = getPath(resourceType, resourceId, index, imageSize.shortName, width, height);

        if (ImageSize.ORIGINAL.equals(imageSize)) {
//            FileServer.getInstance().save(originalFile, path);
        } else {
//            FileServer.getInstance().save(tempFile, path);
            FileUtils.deleteQuietly(tempFile);
        }

        return path+"?_t="+System.currentTimeMillis();
    }

    private static String getPath(ResourceType resourceType, Object resourceId, short index, String shortName, int width, int height) {
        String md5 = DigestUtils.md5Hex(resourceId.toString()).toLowerCase();
        if (md5.length() == 32) {//16
            md5 = md5.substring(8, 24);
        }

        String path = "/" + resourceType.name().toLowerCase() + "/"
                + md5.substring(0, 3) + "/"
                + md5.substring(3, 6) + "/"
                + md5.substring(6, 9) + "/"
                + md5.substring(9, 12) + "/"
                + md5.substring(12, 15) + "/"
                + md5.substring(15, 16) + "/"
                + resourceId.toString() + "/"
                + index + "/"
                + shortName + "_" + width + "x" + height + ".jpg";

        return path;
    }

    public static String save(ResourceType resourceType,
                              Object resourceId,
                              short index,
                              File imgFile) {
        String md5 = DigestUtils.md5Hex(resourceId.toString()).toLowerCase();
        if (md5.length() == 32) {//16
            md5 = md5.substring(8, 24);
        }
        int width = 0;
        int height = 0;
        try {
            ImageUtil.ImageSize newSize = ImageUtil.getImageSize(imgFile);
            width = newSize.getWidth();
            height = newSize.getHeight();
        } catch (IOException e) {
            logger.error("{}", e);
            return null;
        }
        String path = getPath(resourceType, resourceId, index, "o", width, height);

//        FileServer.getInstance().save(imgFile, path);
        return path+"?_t="+System.currentTimeMillis();
    }

    public static void delete(String path) {
//        FileServer.getInstance().delete(path);
    }

    public static String getUrl(String path) {
        try {
            new URL(path);
            return path;
        } catch (MalformedURLException e) {
          //  e.printStackTrace();
        }

//        return FileServer.getInstance().getUrl(path);
        return "";
    }

    public static String saveImgForAb(String tmpPath, long resourceId, ResourceType resourceType){
        try {
            URL imageUrl = new URL(tmpPath);
            File imageFile = null;
            try {
                imageFile = File.createTempFile("allbuy-ptm", ".jpg");
                FileUtils.copyURLToFile(imageUrl, imageFile);
                if (imageFile.length() == 0) {
                    imageFile.deleteOnExit();
                    imageFile = null;
                }
                else {
                    String imagePath = ImageServerUtil.save(resourceType, resourceId, (short) 0, imageFile);
                    return imagePath;
                }
            } catch (IOException e) {
                imageFile = null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        Map<String, Long> map = new HashMap<String, Long>();
        map.put("ZTE", 592L);
        map.put("XIAOMI", 64L);
        map.put("ULEFONE", 617L);
        map.put("Lenovo", 588L);
        map.put("Huawei", 20L);
        map.put("ELEPHONE", 94L);
        map.put("Doogee", 587L);



        /*String s = ImageServerUtil.save(ResourceType.BANNER, 1, (short) 0, new File("d:/2994.jpg"));
        System.out.println(ImageServerUtil.getUrl(s));
        s = ImageServerUtil.scaleAndSave(ResourceType.BANNER, 2, (short) 0, ImageSize.BIG, new File("d:/2994.jpg"));
        System.out.println(ImageServerUtil.getUrl(s));*/

        /*String s = ImageServerUtil.save(ResourceType.BRAND, 44, (short) 0, new File("d:/brand/图层 250.jpg"));
        System.out.println("update ptmbrand set topBrand=1,logo='" + s + "' where id=" + 44);

        s = ImageServerUtil.save(ResourceType.BRAND, 20, (short) 0, new File("d:/brand/logo.jpg"));
        System.out.println("update ptmbrand set topBrand=1,logo='" + s + "' where id=" + 20);

        s = ImageServerUtil.save(ResourceType.BRAND, 27, (short) 0, new File("d:/brand/图层 252.jpg"));
        System.out.println("update ptmbrand set topBrand=1,logo='" + s + "' where id=" + 27);

        s = ImageServerUtil.save(ResourceType.BRAND, 48, (short) 0, new File("d:/brand/图层 254.jpg"));
        System.out.println("update ptmbrand set topBrand=1,logo='" + s + "' where id=" + 48);
        s = ImageServerUtil.save(ResourceType.BRAND, 11, (short) 0, new File("d:/brand/图层 255.jpg"));
        System.out.println("update ptmbrand set topBrand=1,logo='" + s + "' where id=" + 11);
        s = ImageServerUtil.save(ResourceType.BRAND, 4, (short) 0, new File("d:/brand/图层 256 拷贝.jpg"));
        System.out.println("update ptmbrand set topBrand=1,logo='" + s + "' where id=" + 4);*/

        File rootDir = new File("D:\\work\\allbuy\\手机logo");
        File[] files = rootDir.listFiles();
        for(File file : files) {
            String brandName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            String path = ImageServerUtil.save(ResourceType.BRAND, map.get(brandName), (short) 0, file);

            System.out.println("update ptmbrand set logoPath='"+path+"'where id ="+map.get(brandName)+";");



        }
    }

    public static enum ResourceType {
        TEMP,
        SKU,
        BANNER,
        BRAND,
        CATEGORY,
    }

    public static enum ImageSize {
        ORIGINAL("o", -1, -1),
        SMALL("s", 220, 220),
        MIDDLE("m", 330, 330),
        BIG("b", 450, 450);

        private String shortName;
        private int width = -1;
        private int height = -1;

        ImageSize(String shortName, int width, int height) {
            this.shortName = shortName;
            this.width = width;
            this.height = height;
        }

        public String getShortName() {
            return shortName;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }


}
