package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
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
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lihongde on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value="/deal")
public class DealController {

    private Logger logger = LoggerFactory.getLogger(DealController.class);

    @Resource
    IDealService dealService;

    @InitBinder
    public void initBinder(WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, new DateEditor());
    }

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public ModelAndView listDealData(HttpServletRequest request, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size){
        ModelAndView mav = new ModelAndView("deal/list");
        PageableResult<AppDeal> pageableResult = dealService.findDealList(page, size);
        mav.addObject("page", PageHelper.getPageModel(request, pageableResult));
        mav.addObject("datas", pageableResult.getData());
        return mav;
    }

    /**
     * excel导入
     * @param multiFile
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> importExcel(MultipartFile multiFile){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result = dealService.importExcelFile(multiFile);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value="/detail/{id}", method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable(value = "id") Long dealId){
        ModelAndView mav = new ModelAndView("deal/edit");
        mav.addObject("deal", dealService.getDealById(dealId));
        return mav;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ModelAndView edit(AppDeal deal, MultipartFile file) throws IOException{

        //上传图片, 暂支持单个
        String path = null;
        if (!file.isEmpty()) {
//            File newFile = new File(CoreConfig.get(CoreConfig.IMAGE_UPLOAD_URL) + File.separator + file.getOriginalFilename());
//            if(!newFile.exists()){
//                newFile.createNewFile();
//            }
//
//            OutputStream output = new FileOutputStream(newFile);
//            BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
//            bufferedOutput.write(file.getBytes());
            File imageFile = FileUtil.createTempFile();
            FileUtil.writeBytes(imageFile,file.getBytes());

            try {
                path = ImageUtil.uploadImage(imageFile);
            } catch (Exception e) {
                logger.debug("image upload fail");
            }
        }

        //推送至banner展示则点击保存时除deal信息外 创建一条banner数据 banner的生效、失效时间、banner图片与此deal相同 banner的rank为默认值
        if(deal.isPush()){
            AppBanner banner = dealService.getBannerByDealId(deal.getId());
            if(banner == null){
                banner = new AppBanner();
            }
            banner.setSourceId(String.valueOf(deal.getId()));
            banner.setImageUrl(path);
            banner.setCreateTime(deal.getCreateTime());
            banner.setLinkUrl(deal.getLinkUrl());
            banner.setBannerFrom(BannerFrom.DEAL);
            banner.setDeadline(deal.getExpireTime());
            banner.setRank(0);
            dealService.addOrUpdateBanner(banner);
        }

        deal.setImageUrl(path);
        dealService.updateDeal(deal);
        return new ModelAndView("redirect:/deal/list");
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ModelAndView delete(@PathVariable(value = "id") Long dealId){

        return new ModelAndView("redirect:/deal/list");
    }

}
