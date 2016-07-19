package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.admin.IDealService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lihongde on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value = "/deal")
public class DealController {

    @Resource
    IDealService dealService;
    private Logger logger = LoggerFactory.getLogger(DealController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, new DateEditor());
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView listDealData(HttpServletRequest request, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size) {
        ModelAndView mav = new ModelAndView("deal/list");
        PageableResult<AppDeal> pageableResult = dealService.findDealList(page, size);
        for (AppDeal appDeal : pageableResult.getData()) {
            appDeal.setImageUrl(ImageUtil.getImageUrl(appDeal.getImageUrl()));
        }
        mav.addObject("page", PageHelper.getPageModel(request, pageableResult));
        mav.addObject("datas", pageableResult.getData());
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

        if (deal.isPush() == true) {
            AppBanner appBanner = dealService.getBannerByDealId(dealId);
            if (!StringUtils.isEmpty(appBanner.getImageUrl())) {
                deal.setImageUrl(ImageUtil.getImageUrl(appBanner.getImageUrl()));
            }
        }

        mav.addObject("deal", deal);
        return mav;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ModelAndView edit(AppDeal deal, MultipartFile file) throws IOException {

        //上传图片, 暂支持单个
        String path = "";
        if (!file.isEmpty()) {
            File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
            FileUtil.writeBytes(imageFile, file.getBytes());
            try {
                path = ImageUtil.uploadImage(imageFile);
            } catch (Exception e) {
                logger.error("image upload fail");
            }
        }

        //推送至banner展示则点击保存时除deal信息外 创建一条banner数据 banner的生效、失效时间、banner图片与此deal相同 banner的rank为默认值
        if (deal.isPush()) {
            AppBanner banner = dealService.getBannerByDealId(deal.getId());

            if (banner == null) {
                banner = new AppBanner();
            }

            banner.setSourceId(String.valueOf(deal.getId()));
            if (!file.isEmpty()) {
                banner.setImageUrl(path);
            }
            banner.setCreateTime(deal.getCreateTime());
            banner.setLinkUrl(deal.getLinkUrl());
            banner.setBannerFrom(BannerFrom.DEAL);
            banner.setDeadline(deal.getExpireTime());
            banner.setRank(0);

            dealService.saveOrUpdateBanner(banner);

        }
        if (!path.equals("")) {
            deal.setImageUrl(path);
        }
        if (deal.isDisplay()) {
            if (StringUtils.isEmpty(deal.getPriceDescription()) || StringUtils.isEmpty(deal.getImageUrl())) {
                return new ModelAndView().addObject("success", false).addObject("msg", "显示在前台时图片和价格描述不能为空");
            }
        }
        dealService.updateDeal(deal);
        return new ModelAndView("redirect:/deal/list");
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Object delete(@PathVariable(value = "id") Long dealId) {
        dealService.delete(dealId);
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
}
