package hasoffer.job.bean.image;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.sys.SysTimerTaskLog;
import hasoffer.core.product.IImageService;
import hasoffer.core.system.ITimerService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;

public class ImageDownloadJobBean extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloadJobBean.class);

    /**
     * 取ptmimage 逻辑：未下载下来的图片，按照失败次数从小到大排
     */
    private static final String Q_PTM_IMAGE =
            "SELECT t FROM PtmImage t WHERE t.path IS NULL ORDER BY t.errTimes ASC,t.id ASC";

    @Resource
    IImageService imageService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    ITimerService timerService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        SysTimerTaskLog log = timerService.createTaskLog("ImageDownloadTask");

        int page = 1, PAGE_SIZE = 1000;
        PageableResult<PtmImage> pagedImages = dbm.queryPage(Q_PTM_IMAGE, page, PAGE_SIZE);

        long totalPage = pagedImages.getTotalPage();

        while (page <= totalPage) {
            List<PtmImage> images = null;

            if (page == 1) {
                images = pagedImages.getData();
            } else {
                images = dbm.query(Q_PTM_IMAGE, page, PAGE_SIZE);
            }

            if (ArrayUtils.hasObjs(images)) {
                for (PtmImage image : images) {
                    imageService.downloadImage(image);
                }
            }

            page++;
            logger.debug(String.format("download images. page : %d", page));
        }

        timerService.updateTaskLog(log.getId(), "");
    }
}