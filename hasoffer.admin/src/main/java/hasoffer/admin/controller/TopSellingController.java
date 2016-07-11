package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.TopSellingVo;
import hasoffer.base.model.PageModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.ITopSellingService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.IImageService;
import hasoffer.core.product.IProductService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.webcommon.helper.PageHelper;
import jodd.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

/**
 * Created on 2016/7/6.
 */
@Controller
@RequestMapping(value = "/topselling")
public class TopSellingController {

    private static final String Q_COUNT_SKU = "SELECT COUNT(*) FROM PtmCmpSku t WHERE t.productId = ?0 ";
    private static final String Q_SRMSEARCHLOG_BYPRODUCTID = "SELECT t FROM SrmSearchLog t WHERE t.ptmProductId = ?0 ";

    @Resource
    ITopSellingService topSellingService;
    @Resource
    IProductService productService;
    @Resource
    IImageService imageService;
    @Resource
    ICacheService cacheService;
    @Resource
    IDataBaseManager dbm;

    private Logger logger = LoggerFactory.getLogger(TopSellingController.class);

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView list(HttpServletRequest request,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size) {
        ModelAndView modelAndView = new ModelAndView("operate/topselling/list");

        List<TopSellingVo> topSellingVoList = new ArrayList<TopSellingVo>();

        long startLongTime = TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_DAY;

        PageableResult<PtmTopSelling> pageableResult = topSellingService.findTopSellingListByDate(startLongTime, null, page, size);

        List<PtmTopSelling> ptmTopSellingList = pageableResult.getData();

        //此处需要对productid进行去重操作
        Set<Long> ptmproductIdSet = new HashSet<Long>();

        for (PtmTopSelling ptmTopSelling : ptmTopSellingList) {

            TopSellingVo topSellingVo = new TopSellingVo();

            long productId = ptmTopSelling.getProductId();
            if (ptmproductIdSet.contains(productId)) {
                continue;
            } else {
                ptmproductIdSet.add(productId);
            }

            PtmProduct ptmProduct = productService.getProduct(productId);
            if (ptmProduct == null) {
                continue;
            }

            long skuNumber = dbm.querySingle(Q_COUNT_SKU, Arrays.asList(productId));

            List<SrmSearchLog> loglist = dbm.query(Q_SRMSEARCHLOG_BYPRODUCTID, Arrays.asList(productId));

            if (loglist == null || loglist.size() == 0) {
                continue;
            }

            String logid = loglist.get(0).getId();

            topSellingVo.setId(ptmTopSelling.getId());
            topSellingVo.setName(ptmProduct.getTitle());
            topSellingVo.setProductId(productId);
            topSellingVo.setYmd(ptmTopSelling.getYmd());
            topSellingVo.setImageurl(productService.getProductMasterImageUrl(productId));
            topSellingVo.setSkuNumber(skuNumber);
            topSellingVo.setLogid(logid);

            topSellingVoList.add(topSellingVo);
        }

        PageModel pageModel = PageHelper.getPageModel(request, pageableResult);
        modelAndView.addObject("page", pageModel);
        modelAndView.addObject("topSellingVoList", topSellingVoList);

        return modelAndView;
    }

    @RequestMapping(value = "detail/{productId}", method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable long productId) {

        ModelAndView modelAndView = new ModelAndView("operate/topselling/edit");

        PtmProduct ptmProduct = productService.getProduct(productId);

        String title = ptmProduct.getTitle();
        modelAndView.addObject("title", title);

        PtmImage ptmImage = productService.getProductMasterImage(productId);

        if (ptmImage == null) {
            return new ModelAndView("system/error");
        }

        String oriImageUrl = productService.getProductMasterImageUrl(ptmImage.getId());
        modelAndView.addObject("oriImageUrl", oriImageUrl);
        modelAndView.addObject("ptmimageid", ptmImage.getId());

        return modelAndView;
    }


    @RequestMapping(value = "/edit/{ptmimageid}", method = RequestMethod.POST)
    public ModelAndView edit(@PathVariable long ptmimageid, MultipartFile file) {

        try {

            File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
            FileUtil.writeBytes(imageFile, file.getBytes());
            String imagePath = ImageUtil.uploadImage(imageFile);

            imageService.updatePtmProductImagePath(ptmimageid, imagePath);

//            //编辑的时候注意更新图片清除缓存
//            String PTMPRODUCT_IMAGE_CACHE_KEY = CACHE_KEY_PRE + "_getProductMasterImageUrl_" + ptmimageid;
//
//            cacheService.del(PTMPRODUCT_IMAGE_CACHE_KEY);
        } catch (Exception e) {
            logger.error("image upload fail");
        }

        return new ModelAndView("redirect:/topselling/list");
    }

}
