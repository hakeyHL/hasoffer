package hasoffer.core.utils;

import com.mongodb.util.JSON;
import hasoffer.base.config.AppConfig;
import hasoffer.base.exception.ImageDownloadException;
import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.bo.common.ImagePath;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {
    private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    public static String getImageUrl(PtmCmpSku sku) {
        String imageUrl = "";

        if (StringUtils.isEmpty(sku.getSmallImagePath())) {
            if (StringUtils.isEmpty(sku.getImagePath())) {
                imageUrl = getImage3rdUrl(sku.getOriImageUrl());
            } else {
                imageUrl = getImageUrl(sku.getImagePath());
            }
        } else {
            imageUrl = getImageUrl(sku.getSmallImagePath());
        }

        return imageUrl;
    }


    /**
     * downloadAndUpload2
     * url对应的图片下载，然后上传到图片服务器，图片服务器返回3个路径：原图、小图、大图
     * @param imageUrl
     * @return
     * @throws ImageDownloadOrUploadException
     */
    public static ImagePath downloadAndUpload2(String imageUrl)
            throws ImageDownloadOrUploadException {

        String dirPath = FileUtils.getTempDirectoryPath();
        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }
        File file = new File(dirPath + IDUtil.uuid() + ".jpg");
        // 下载图片到本地
        try {
            //HttpUtilx.getFile(image.getImageUrl(), file);
            boolean ret = HttpUtils.getImage(imageUrl, file);
            if (!ret) {
                throw new ImageDownloadException(imageUrl);
            }

            // 上传图片
            return uploadImage2(file);

        } catch (Exception e) {
            logger.error(e.getMessage() + "[" + imageUrl + "]");
            throw new ImageDownloadOrUploadException("下载或上传图片时出错");
        } finally {
            // 删除图片
            FileUtils.deleteQuietly(file);
        }
    }

    /**
     * 将其他网站图片下载后上传到本地图片服务器, 图片服务器不对图片进行处理，返回图片路径
     *
     * @return
     */
    public static String downloadAndUpload(String imageUrl)
            throws ImageDownloadOrUploadException {

        String dirPath = FileUtils.getTempDirectoryPath();
        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }
        File file = new File(dirPath + IDUtil.uuid() + ".jpg");
        // 下载图片到本地
        try {
            //HttpUtilx.getFile(image.getImageUrl(), file);
            boolean ret = HttpUtils.getImage(imageUrl, file);
            if (!ret) {
                throw new ImageDownloadException(imageUrl);
            }

            // 上传图片
            return uploadImage(file);
        } catch (Exception e) {
            logger.error(e.getMessage() + "[" + imageUrl + "]");
            throw new ImageDownloadOrUploadException("下载或上传图片时出错");
        } finally {
            // 删除图片
            FileUtils.deleteQuietly(file);
        }
    }

    public static String uploadImage(File file) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file", file);

        String uploadUrl = AppConfig.get(AppConfig.IMAGE_UPLOAD_URL);
//        uploadUrl = "http://54.169.146.198:8080/hasoffer-file/s3/image";
        HttpResponseModel httpResponseModel = HttpUtils.uploadFile(uploadUrl, file);

        Map respMap = (Map) JSON.parse(httpResponseModel.getBodyString());
        return (String) respMap.get("data");
    }

    public static ImagePath uploadImage2(File file) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file", file);

        String uploadUrl = AppConfig.get(AppConfig.IMAGE_UPLOAD_URL2);
//        uploadUrl = "http://54.169.146.198:8080/hasoffer-file/s3/image2";
        HttpResponseModel httpResponseModel = HttpUtils.uploadFile(uploadUrl, file);

        Map respMap = (Map) JSON.parse(httpResponseModel.getBodyString());
        Map pathMap = (Map) respMap.get("data");

        return new ImagePath((String) pathMap.get("originalPath"), (String) pathMap.get("smallPath"), (String) pathMap.get("bigPath"));
    }

    public static String getImageUrl(final String path) {
        return "http://" + AppConfig.get(AppConfig.IMAGE_HOST) + path;
    }

    public static String getImage3rdUrl(final String url) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }

        String _url = url.replaceAll("https??://", "");
        return AppConfig.get(AppConfig.IMAGE_URL_3RD_PREFIX) + _url;
    }

    public static ImageSize getImageSize(File imgFile) throws IOException {
        ImageIcon ii = new ImageIcon(FileUtils.readFileToByteArray(imgFile));
        Image i = ii.getImage();
        int iWidth = i.getWidth(null);
        int iHeight = i.getHeight(null);

        ImageSize imageSize = new ImageSize(iWidth, iHeight);
        return imageSize;
    }

    public static ImageSize scale(File originalFile, File resizedFile, int newWidth, int newHeight) throws IOException {
        try {
            return scale_2(originalFile, resizedFile, newWidth, newHeight);
        } catch (Exception ex) {
            logger.error("error", ex);
        }

        return null;

    } // Example usage

    private static ImageSize scale_2(File originalFile, File resizedFile,
                                     int newWidth, int newHeight) throws IOException {

        float quality = 1;
        if (quality > 1) {
            throw new IllegalArgumentException(
                    "Quality has to be between 0 and 1");
        }

        ImageIcon ii = new ImageIcon(originalFile.getCanonicalPath());
        Image i = ii.getImage();
        Image resizedImage = null;

        resizedImage = i.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // This code ensures that all the pixels in the image are loaded.
        Image temp = new ImageIcon(resizedImage).getImage();

        // Create the buffered image.
        BufferedImage bufferedImage = new BufferedImage(temp.getWidth(null),
                temp.getHeight(null), BufferedImage.TYPE_INT_RGB);

        // Copy image to buffered image.
        Graphics g = bufferedImage.createGraphics();

        // Clear background and paint the image.
        g.setColor(Color.white);
        g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
        g.drawImage(temp, 0, 0, null);
        g.dispose();

        // Soften.
        float softenFactor = 0.05f;
        float[] softenArray = {0, softenFactor, 0, softenFactor,
                1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0};
        Kernel kernel = new Kernel(3, 3, softenArray);
        ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        bufferedImage = cOp.filter(bufferedImage, null);

        // Write the jpeg to a file.
        FileOutputStream out = new FileOutputStream(resizedFile);

        String ext = resizedFile.getName().substring(
                resizedFile.getName().lastIndexOf('.') + 1);
        ImageIO.write(bufferedImage, ext, out);

        return ImageUtil.getImageSize(resizedFile);
    }

    private static ImageSize scale_1(String originalFile, String resizedFile,
                                     int newWidth) throws Exception {

        // create command
        // ConvertCmd cmd = new ConvertCmd();

        // create the operation, add images and operators/options
        // IMOperation op = new IMOperation();
        // op.addImage(originalFile);
        // op.quality((double) 95);
        // op.strip();
        // op.samplingFactor((double) 2, (double) 2);
        // op.resize(newWidth*2, newHeight*2, "!");
        // op.addImage(resizedFile);

        // execute the operation
        // cmd.run(op);

        String cmd = String
                .format("convert %s -interlace plane -quality 85 -define filter:blur=0.88549061701764 -sampling-factor 4:2:2 -strip -scale %dx %s",
                        originalFile, newWidth, resizedFile);

        logger.trace("cmd=" + cmd);

        //JavaShellUtil.executeShell(cmd);

        return ImageUtil.getImageSize(new File(
                resizedFile));
    }

    public static void main(String[] args) throws IOException {
        // File originalImage = new File("C:\\11.jpg");
        // resize(originalImage, new File("c:\\11-0.jpg"),150, 0.7f);
        // resize(originalImage, new File("c:\\11-1.jpg"),150, 1f);
        // resize("d:\\test.png", "d:\\1207-0.jpg", 150, 30);
//        ImageSize size = scale(new File("d:\\test2.jpg"), new File("d:\\1207-0.jpg"), 50, 100);
//        System.out.print(size);
//        System.out.println(getImage3rdUrl("https://d1nfvnlhmjw5uh.cloudfront.net/222942-4-desktop-normal.jpg"));
//        System.out.println(getImage3rdUrl("http://d1nfvnlhmjw5uh.cloudfront.net/222942-4-desktop-normal.jpg"));

//        HttpUtilx.postAsString("img.hasoffer.com")
    }

    public static class ImageSize {
        private int width;
        private int height;

        public ImageSize(int width, int height) {
            super();
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public String toString() {
            return "ImageSize [width=" + width + ", height=" + height + "]";
        }

    }

}