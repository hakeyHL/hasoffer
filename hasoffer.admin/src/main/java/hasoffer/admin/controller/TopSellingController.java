package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.TopSellingVo;
import hasoffer.base.model.PageModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.admin.ITopSellingService;
import hasoffer.core.bo.enums.TopSellStatus;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                             @RequestParam(defaultValue = "") String topSellingStatusString,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size) {
        ModelAndView modelAndView = new ModelAndView("operate/topselling/list");

        List<TopSellingVo> topSellingVoList = new ArrayList<TopSellingVo>();

        if (StringUtils.isEmpty(topSellingStatusString)) {
            topSellingStatusString = TopSellStatus.ONLINE.toString();
            modelAndView.addObject("selectstatus", "");
        }

        TopSellStatus selectstatus = TopSellStatus.valueOf(topSellingStatusString);

        PageableResult<PtmTopSelling> pageableResult = topSellingService.findTopSellingList(selectstatus, page, size);

        List<PtmTopSelling> ptmTopSellingList = pageableResult.getData();

        for (PtmTopSelling ptmTopSelling : ptmTopSellingList) {

            TopSellingVo topSellingVo = new TopSellingVo();

            long productId = ptmTopSelling.getId();

            PtmProduct ptmProduct = productService.getProduct(productId);
            if (ptmProduct == null) {
                continue;
            }

            long skuNumber = dbm.querySingle(Q_COUNT_SKU, Arrays.asList(productId));

            topSellingVo.setId(ptmTopSelling.getId());
            topSellingVo.setName(ptmProduct.getTitle());
            topSellingVo.setImageurl(productService.getProductMasterImageUrl(productId));
            topSellingVo.setSkuNumber(skuNumber);

            topSellingVoList.add(topSellingVo);
        }

        PageModel pageModel = PageHelper.getPageModel(request, pageableResult);
        modelAndView.addObject("page", pageModel);
        modelAndView.addObject("topSellingVoList", topSellingVoList);

        List<TopSellStatus> statusList = Arrays.asList(TopSellStatus.values());
        modelAndView.addObject("statusList", statusList);

        modelAndView.addObject("selectstatus", selectstatus);

        return modelAndView;
    }

    @RequestMapping(value = "detail/{topSellingId}", method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable long topSellingId) {

        ModelAndView modelAndView = new ModelAndView("operate/topselling/edit");

        PtmProduct ptmProduct = productService.getProduct(topSellingId);

        String title = ptmProduct.getTitle();
        modelAndView.addObject("title", title);

        PtmImage ptmImage = productService.getProductMasterImage(topSellingId);

        if (ptmImage == null) {
            return new ModelAndView("system/error");
        }

        String oriImageUrl = productService.getProductMasterImageUrl(topSellingId);
        modelAndView.addObject("oriImageUrl", oriImageUrl);
        modelAndView.addObject("topSellingId", topSellingId);

        return modelAndView;
    }


    @RequestMapping(value = "/edit/{topSellingId}", method = RequestMethod.POST)
    public ModelAndView edit(@PathVariable long topSellingId, MultipartFile file) {

        try {

            File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
            FileUtil.writeBytes(imageFile, file.getBytes());
            String imagePath = ImageUtil.uploadImage(imageFile);

            //此处topsellingid就是productid
            imageService.updatePtmProductImagePath(topSellingId, imagePath);
//            //更新后需要更新topselling状态
//            topSellingService.updateTopSellingStatus(topSellingId, TopSellStatus.ONLINE);

//            //编辑的时候注意更新图片清除缓存
//            String PTMPRODUCT_IMAGE_CACHE_KEY = CACHE_KEY_PRE + "_getProductMasterImageUrl_" + ptmimageid;
//
//            cacheService.del(PTMPRODUCT_IMAGE_CACHE_KEY);
        } catch (Exception e) {
            logger.error("image upload fail");
        }

        return new ModelAndView("redirect:/topselling/list");
    }

    @RequestMapping(value = "/delete/{topsellingid}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable long topsellingid) {

        topSellingService.updateTopSellingStatus(topsellingid, TopSellStatus.OFFLINE);

        return new ModelAndView("redirect:/topselling/list");
    }

    @RequestMapping(value = "/changeStatus/{topsellingid}", method = RequestMethod.GET)
    public void changeStatus(@PathVariable long topsellingid) {

        PtmTopSelling topSelling = topSellingService.findTopSellingById(topsellingid);

        TopSellStatus status = topSelling.getStatus();

        if (TopSellStatus.WAIT.equals(status)) {
            topSellingService.updateTopSellingStatus(topsellingid, TopSellStatus.ONLINE);
        } else {
            topSellingService.updateTopSellingStatus(topsellingid, TopSellStatus.WAIT);
        }
    }
}
