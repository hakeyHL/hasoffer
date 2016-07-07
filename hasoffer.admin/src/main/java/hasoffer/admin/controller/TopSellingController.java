package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.TopSellingVo;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.ITopSellingService;
import hasoffer.core.persistence.po.ptm.PtmImage;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.search.SrmSearchCount;
import hasoffer.core.product.IImageService;
import hasoffer.core.product.IProductService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.ImageUtil;
import jodd.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 2016/7/6.
 */
@Controller
@RequestMapping(value = "/topselling")
public class TopSellingController {

    private static final String CACHE_KEY_PRE = "PRODUCT_";
    private static final String IMAGE_PREFIX = "http://img.hasoffer.com";
    @Resource
    ITopSellingService topSellingService;
    @Resource
    IProductService productService;
    @Resource
    IImageService imageService;
    @Resource
    ICacheService cacheService;
    private Logger logger = LoggerFactory.getLogger(TopSellingController.class);

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("operate/topselling/list");

        List<TopSellingVo> topSellingVoList = new ArrayList<TopSellingVo>();

        long startLongTime = TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_DAY;

        List<SrmSearchCount> srmSearchCountList = topSellingService.findTopSellingListByDate(startLongTime, null);

        //此处需要对productid进行去重操作
        Set<Long> ptmproductIdSet = new HashSet<Long>();

        for (SrmSearchCount srmSearchCount : srmSearchCountList) {

            TopSellingVo topSellingVo = new TopSellingVo();

            long productId = srmSearchCount.getProductId();
            if (ptmproductIdSet.contains(productId)) {
                continue;
            } else {
                ptmproductIdSet.add(productId);
            }

            PtmProduct ptmProduct = productService.getProduct(productId);
            if (ptmProduct == null) {
                continue;
            }

            topSellingVo.setId(srmSearchCount.getId());
            topSellingVo.setName(ptmProduct.getTitle());
            topSellingVo.setProductId(productId);
            topSellingVoList.add(topSellingVo);
        }

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
