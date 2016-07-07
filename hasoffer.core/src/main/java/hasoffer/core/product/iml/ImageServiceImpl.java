package hasoffer.core.product.iml;

import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.updater.PtmImageUpdater;
import hasoffer.core.product.IImageService;
import hasoffer.core.utils.ImageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 * Date : 2016/1/13
 * Function :
 */
@Service
public class ImageServiceImpl implements IImageService {
    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean downloadImage(PtmImage image) {

        PtmImageUpdater ptmImageUpdater = new PtmImageUpdater(image.getId());

        String path;

        try {
            path = ImageUtil.downloadAndUpload(image.getImageUrl());
        } catch (ImageDownloadOrUploadException e) {
            try {
                String url = image.getImageUrl();

                url = url.replaceFirst("-\\d+", "-1");
                path = ImageUtil.downloadAndUpload(image.getImageUrl());

                ptmImageUpdater.getPo().setImageUrl(url);
            } catch (Exception e2) {
                ptmImageUpdater.getPo().setErrTimes(image.getErrTimes() + 1);
                dbm.update(ptmImageUpdater);
                return false;
            }
        }

        ptmImageUpdater.getPo().setPath(path);
        dbm.update(ptmImageUpdater);

        return true;
    }

    /*public boolean downloadImage(PtmImage image) {

        PtmImageUpdater ptmImageUpdater = new PtmImageUpdater(image.getId());

        File file = new File(FileUtils.getTempDirectoryPath() + IDUtil.uuid() + ".jpg");
        // 下载图片到本地
        try {
            //HttpUtilx.getFile(image.getImageUrl(), file);
            boolean ret = HttpUtils.getImage(image.getImageUrl(), file);
            if (!ret) {
                String url = image.getImageUrl();
                url = url.replaceFirst("-\\d+", "-1");
                ret = HttpUtils.getImage(url, file);

                ptmImageUpdater.getPo().setImageUrl2(url);
                if (!ret) {
                    throw new Exception("failed to load img");
                }
            }

            // 上传图片
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("file", file);
            String resp = HttpUtils.postAsString(CoreConfig.get(CoreConfig.IMAGE_UPLOAD_URL), params);

            Map respMap = (Map) JSON.parse(resp);
            String path = (String) respMap.get("data");

            ptmImageUpdater.getPo().setPath2(path);

            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            ptmImageUpdater.getPo().setErrTimes(image.getErrTimes() + 1);
//            ptmImageUpdater.getPo().setPath2("");
            return false;
        } finally {
            // 删除图片
            dbm.update(ptmImageUpdater);
            FileUtils.deleteQuietly(file);
        }
    }*/


    /**
     * 注意使用此方法直接对ptmproduct的图片进行更新，参数imageUrl需要输入全路径
     *
     * @param ptmimageid
     * @param imageUrl
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePtmProductImage(long ptmimageid, String imageUrl) {

        PtmImageUpdater updater = new PtmImageUpdater(ptmimageid);

        updater.getPo().setImageUrl(imageUrl);

        dbm.update(updater);
    }
}
