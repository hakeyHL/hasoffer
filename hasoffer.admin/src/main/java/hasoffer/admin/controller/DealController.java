package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.AppdealVo;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.admin.impl.DealServiceImpl;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.BannerFrom;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.utils.DateEditor;
import hasoffer.core.utils.ImageUtil;
import hasoffer.webcommon.helper.PageHelper;
import jodd.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lihongde on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value = "/deal")
public class DealController {

    @Resource
    IDealService dealService;
    @Resource
    DealServiceImpl dealServiceImple;
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(DealController.class);

    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M月-yyyy");
        System.out.println(simpleDateFormat.format(date));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, new DateEditor());
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView listDealData(HttpServletRequest request,
                                     @RequestParam(defaultValue = "createTime") String orderByField,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "50") int size,
                                     @RequestParam(defaultValue = "0") int type,
                                     @RequestParam(defaultValue = "") String keyword,
                                     @RequestParam(defaultValue = "0") Long keyId) {
        ModelAndView mav = new ModelAndView("deal/list");
        PageableResult<AppDeal> pageableResult = null;
        if (keyId > 0) {
            AppDeal appDeal = dealService.getDealById(keyId);
            if (appDeal != null) {
                pageableResult = new PageableResult<>(Arrays.asList(appDeal), 1, 1, 1);
            }
        } else if (org.apache.commons.lang3.StringUtils.isNotEmpty(keyword)) {
            pageableResult = dealService.getDealsByTitle(keyword, page, size);
        }

        if (pageableResult == null || pageableResult.getData().size() < 1) {
            pageableResult = dealService.findDealList(page, size, type, orderByField);
        }
        List<AppdealVo> appdealVoList = new ArrayList<>();
        for (AppDeal appDeal : pageableResult.getData()) {
            AppdealVo appdealVo = new AppdealVo();

            appdealVo.setId(appDeal.getId());
            appdealVo.setWebsite(appDeal.getWebsite());
            appdealVo.setCreateTime(appDeal.getCreateTime());
            appdealVo.setListPageImage(ImageUtil.getImageUrl(appDeal.getListPageImage()));
            appdealVo.setPush(appDeal.isPush());
            appdealVo.setDisplay(appDeal.isDisplay());
            appdealVo.setTitle(appDeal.getTitle());
            appdealVo.setPriceDescription(appDeal.getPriceDescription());
            appdealVo.setExpireTime(appDeal.getExpireTime());
            Long dealClickCount = appDeal.getDealClickCount();
            Long originClickCount = appDeal.getOriginClickCount();
            if (dealClickCount != null) {
                if (originClickCount != null) {
                    appdealVo.setDealClickCount(appDeal.getDealClickCount() - appDeal.getOriginClickCount());
                } else {
                    appdealVo.setDealClickCount(appDeal.getDealClickCount());
                }
            }

            appdealVo.setLinkUrl(appDeal.getLinkUrl());
            appdealVo.setWeight(appDeal.getWeight());
            appdealVo.setDiscount(appDeal.getDiscount());
            appdealVo.setAppdealSource(appDeal.getAppdealSource());
            appdealVo.setOriginPrice(appDeal.getOriginPrice());
            appdealVo.setPresentPrice(appDeal.getPresentPrice() == null ? 0 : appDeal.getPresentPrice());
            appdealVo.setOriLinkUrl(appDeal.getOriLinkUrl());
            appdealVo.setOriginClickCount(appDeal.getOriginClickCount());
            if (TimeUtils.nowDate().getTime() > appDeal.getExpireTime().getTime()) {
                appdealVo.setExpireStatus(0);//已经失效
            } else {
                appdealVo.setExpireStatus(1);//有效
            }
            appdealVoList.add(appdealVo);
        }
        mav.addObject("page", PageHelper.getPageModel(request, pageableResult));
        mav.addObject("datas", appdealVoList);
        mav.addObject("type", type);
        return mav;
    }

    /**
     * excel导入
     *
     * @param multiFile
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> importExcel(MultipartFile multiFile) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result = dealService.importExcelFile(multiFile);
//            dealServiceImple.reimportAllDeals2Solr();
            result.put("success", true);
        } catch (Exception e) {
            logger.error("导入失败");
            result.put("success", false);
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable(value = "id") Long dealId) {

        ModelAndView mav = new ModelAndView("deal/edit");
        AppDeal deal = dealService.getDealById(dealId);

        mav.addObject("imagePath", deal.getImageUrl());

        if (!StringUtils.isEmpty(deal.getImageUrl())) {
            deal.setImageUrl(ImageUtil.getImageUrl(deal.getImageUrl()));
        }

        //不管该deal是不是被push到banner位置，都检查他的图片路径是否存在
//        if (deal.isPush() == true) {
        AppBanner appBanner = dealService.getBannerByDealId(dealId);
        if (appBanner != null && !StringUtils.isEmpty(appBanner.getImageUrl())) {
            mav.addObject("bannerImageUrl", ImageUtil.getImageUrl(appBanner.getImageUrl()));
        }
//        }

        if (deal.getOriginPrice() == null) {
            deal.setOriginPrice(0f);
        }
        mav.addObject("deal", deal);
        return mav;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ModelAndView edit(AppDeal deal, MultipartFile dealFile, MultipartFile bannerFile, String bannerImageUrl) {

        if (dealFile == null) {
            logger.error("deal.edit: dealFile is null.");
        }
        if (bannerFile == null) {
            logger.error("deal.edit: bannerFile is null.");
        }
        /**
         * 无关属性: 不需要展示,不需要修改的属性,
         * 不要忘记放入jsp文件中,否则信息丢失
         */
        String dealPath = "";
        String dealSmallPath = "";
        String dealBigPath = "";
        if (StringUtils.isEmpty(bannerImageUrl)) {
            //修改了图片
            if (bannerFile != null && !bannerFile.isEmpty()) {
                try {
                    File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
                    FileUtil.writeBytes(imageFile, bannerFile.getBytes());
                    bannerImageUrl = ImageUtil.uploadImage(imageFile);
                } catch (Exception e) {
                    logger.error("banner image upload fail ,{}", e.getMessage());
                    return new ModelAndView("redirect:/deal/list");
                }
            }
        }
        if (StringUtils.isEmpty(deal.getImageUrl())) {

            if (dealFile != null && !dealFile.isEmpty()) {
                try {
                    File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
                    FileUtil.writeBytes(imageFile, dealFile.getBytes());
                    dealPath = ImageUtil.uploadImage(imageFile);
                    dealBigPath = ImageUtil.uploadImage(imageFile, 316, 180);
                    dealSmallPath = ImageUtil.uploadImage(imageFile, 180, 180);
                } catch (Exception e) {
                    logger.error("deal image upload fail , {}", e.getMessage());
                    return new ModelAndView("redirect:/deal/list");
                }
            }
        }
        if (bannerImageUrl.contains("http")) {
            bannerImageUrl = bannerImageUrl.substring(bannerImageUrl.indexOf("com") + 3, bannerImageUrl.length());
        }
        if (deal.getImageUrl().contains("http")) {
            deal.setImageUrl(deal.getImageUrl().substring(deal.getImageUrl().indexOf("com") + 3, deal.getImageUrl().length()));
        }
        //推送至banner展示则点击保存时除deal信息外 创建一条banner数据 banner的生效、失效时间、banner图片与此deal相同 banner的rank为默认值
        if (deal.isPush()) {
            AppBanner banner = dealService.getBannerByDealId(deal.getId());

            if (banner == null) {
                banner = new AppBanner();
            }

            banner.setSourceId(String.valueOf(deal.getId()));
            if (!bannerFile.isEmpty()) {
                banner.setImageUrl(bannerImageUrl);
            }
            banner.setCreateTime(deal.getCreateTime());
            banner.setLinkUrl(deal.getLinkUrl());
            banner.setBannerFrom(BannerFrom.DEAL);
            banner.setDeadline(deal.getExpireTime());
            banner.setRank(0);

            dealService.saveOrUpdateBanner(banner);
        } else {//检查是否由该deal生成的banner，该选项是不展示，不需要删除，修改状态即可
            AppBanner appBanner = dealService.getBannerByDealId(deal.getId());
            if (appBanner != null) {
//                dealService.deleteBanner(appBanner.getId());
                dealService.logicalDeleteBanner(appBanner.getId());
            }
        }
        if (!StringUtils.isEmpty(dealPath)) {
            deal.setImageUrl(dealPath);
        }
        if (deal.getWebsite() != null) {
            deal.setWebsite(deal.getWebsite());
        }
        if (!StringUtils.isEmpty(dealBigPath)) {
            deal.setInfoPageImage(dealBigPath);
        }
        if (!StringUtils.isEmpty(dealSmallPath)) {
            deal.setListPageImage(dealSmallPath);
        }
        dealService.updateDeal(deal);
//        DealModel dm = new DealModel(deal);
//        dealServiceImple.importDeal2Solr(dm);
//        dealServiceImple.reimportAllDeals2Solr();
        return new ModelAndView("redirect:/deal/list");
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Object delete(@PathVariable(value = "id") Long dealId) {
        dealService.deleteDeal(dealId);
        return true;
    }

    @RequestMapping(value = "/batchDelete", method = RequestMethod.GET)
    @ResponseBody
    public Object batchDelete(@RequestParam(value = "ids[]") Long[] ids) {
        dealService.batchDelete(ids);
        return true;
    }

    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String fileName = "Deal表格模板.xlsx";
            String downloadPath = request.getSession().getServletContext().getRealPath("/") + "/download/" + fileName;//获取下载模版路径
            File file = new File(downloadPath);
            toDownload(request, response, new FileInputStream(downloadPath), file, fileName);
        } catch (Exception e) {
            logger.error("download excel template fail");
            e.printStackTrace();
        }
    }

    @RequestMapping("/createDeal")
    public ModelAndView redirect2DealDetail() {
        ModelAndView mav = new ModelAndView("deal/edit");
        return mav;
    }

    /**
     * 提供文件下载
     *
     * @param inputStream
     * @param file
     * @param fileName
     * @return
     */
    public void toDownload(HttpServletRequest request, HttpServletResponse response, FileInputStream inputStream,
                           File file, String fileName) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        fileName = new String(fileName.getBytes("GBK"), "ISO8859-1");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(file.length()));
        try {
            in = new BufferedInputStream(inputStream);
            out = new BufferedOutputStream(response.getOutputStream());
            byte[] data = new byte[2048];
            int len = 0;
            while (-1 != (len = in.read(data, 0, data.length))) {
                out.write(data, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @RequestMapping(value = "disableDeal/{id}", method = RequestMethod.GET)
    public ModelAndView setDealDisable(@PathVariable long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("code", "00000");
        modelAndView.addObject("msg", "ok");
        if (id > 0) {
            AppDeal appDeal = dealService.getDealById(id);
            if (appDeal != null) {
                appDeal.setExpireTime(new Date());
                try {
                    dealService.updateDeal(appDeal);
                } catch (Exception e) {
                    modelAndView.addObject("code", "10000");
                }
            }
        }
        return modelAndView;
    }
}
