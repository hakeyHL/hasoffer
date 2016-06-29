package hasoffer.core.utils;

import hasoffer.base.config.AppConfig;
import hasoffer.base.exception.ImageDownloadException;
import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.base.model.ImagePath;
import hasoffer.base.storage.S3Storage;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ImageUtil {
    private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    private static S3Storage s3Storage = new S3Storage();

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

    public static File downloadImage(String imageUrl) throws ImageDownloadException {
        String dirPath = FileUtils.getTempDirectoryPath();
        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }
        File file = new File(dirPath + IDUtil.uuid() + ".jpg");

        //HttpUtilx.getFile(image.getImageUrl(), file);
        boolean ret = HttpUtils.getImage(imageUrl, file);
        if (!ret) {
            throw new ImageDownloadException(imageUrl);
        }

        return file;
    }

    /**
     * downloadAndUpload2
     * url对应的图片下载，然后上传到图片服务器，图片服务器返回3个路径：原图、小图、大图
     *
     * @param imageUrl
     * @return
     * @throws ImageDownloadOrUploadException
     */
    public static ImagePath downloadAndUpload2(String imageUrl)
            throws ImageDownloadOrUploadException {

        File file = null;

        try {
            // 下载图片到本地
            file = downloadImage(imageUrl);

            // 转图片格式 + 上传图片
            return convertAndUploadImage(file);

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

        File file = null;

        try {
            // 下载图片到本地
            file = downloadImage(imageUrl);

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
        return "/s3" + s3Storage.save(file);
    }

    public static ImagePath convertAndUploadImage(File file) throws Exception {
        ImagePath imagePath = s3Storage.saveAndConvert(file);

        return new ImagePath("/s3" + imagePath.getOriginalPath(), "/s3" + imagePath.getSmallPath(), "/s3" + imagePath.getBigPath());
    }

    /*public static String uploadImage(File file) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file", file);

        String uploadUrl = AppConfig.get(AppConfig.IMAGE_UPLOAD_URL);
//        uploadUrl = "http://54.169.146.198:8080/hasoffer-file/s3/image";
        HttpResponseModel httpResponseModel = HttpUtils.uploadFile(uploadUrl, file);

        Map respMap = (Map) JSON.parse(httpResponseModel.getBodyString());
        return (String) respMap.get("data");
    }

    public static ImagePath convertAndUpload(File file) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("file", file);

        String uploadUrl = AppConfig.get(AppConfig.IMAGE_UPLOAD_URL2);
//        uploadUrl = "http://54.169.146.198:8080/hasoffer-file/s3/image2";
        HttpResponseModel httpResponseModel = HttpUtils.uploadFile(uploadUrl, file);

        Map respMap = (Map) JSON.parse(httpResponseModel.getBodyString());
        Map pathMap = (Map) respMap.get("data");

        return new ImagePath((String) pathMap.get("originalPath"), (String) pathMap.get("smallPath"), (String) pathMap.get("bigPath"));
    }*/

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

}