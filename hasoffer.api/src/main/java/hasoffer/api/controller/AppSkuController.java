package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.helper.Httphelper;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016/7/25.
 * 专用于比价的Controller
 */
@Controller
@RequestMapping("sku")
public class AppSkuController {
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IMongoDbManager mongoDbManager;
    @Resource
    IPtmCmpSkuImageService ptmCmpSkuImageService;
    Logger logger = LoggerFactory.getLogger(AppSkuController.class);

    public static List getImageArray(List<PtmCmpSkuImage> list) {
        List li = new ArrayList();
        if (list != null && list.size() > 0) {
            PtmCmpSkuImage ptmCmpSkuImage = list.get(0);
            String imagePath1 = ptmCmpSkuImage.getImagePath1();
            String imagePath2 = ptmCmpSkuImage.getImagePath2();
            String imagePath3 = ptmCmpSkuImage.getImagePath3();
            String imagePath4 = ptmCmpSkuImage.getImagePath4();
            if (!StringUtils.isEmpty(imagePath1)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath1()));
            }
            if (!StringUtils.isEmpty(imagePath2)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath2()));
            }
            if (!StringUtils.isEmpty(imagePath3)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath3()));
            }
            if (!StringUtils.isEmpty(imagePath4)) {
                li.add(ImageUtil.getImageUrl(ptmCmpSkuImage.getImagePath4()));
            }
        }
        return li;
    }

    public static void main(String[] args) {
        String temp = "{\"Fabric Care:\":\"Hand wash at 30°C, Do not bleach, Mild Iron, Do not Tumble Dry, Line Dry in shade, wash separately, do not iron on decorations/print, Use mild detergents\",\"Sales Package\":\"1 Kurti\",\"Legging Available\":\"No\",\"Ideal For\":\"Women's\",\"Other details\":\"Stitched\",\"Neck\":\"Mandarin collar\"}";
        String ss = "\\ysf";
        System.out.println(ss.replaceAll("\\\\", ""));
    }

    /**
     * 根据sku的id获取sku详细信息
     *
     * @return
     */
    @RequestMapping("info")
    public String getSkuInfo(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        //PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"ratingNum", "bestPrice", "priceOff", "backRate", "support", "price", "returnGuarantee", "freight"});
        PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(id);
        if (ptmCmpSku != null) {
            logger.info(" has this sku " + id);
            PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, ptmCmpSku.getId());
            logger.info("get sku totalWeigth from  mongo " + ptmCmpSkuDescription == null ? " not null" : " is null");
            Map map = new HashMap<>();
            if (ptmCmpSkuDescription != null) {
                map.put("description", ptmCmpSkuDescription.getJsonDescription() == null ? "" : ptmCmpSkuDescription.getJsonDescription());//描述
                String tempJsonParam = ptmCmpSkuDescription.getJsonParam();
                if (tempJsonParam != null) {
                    tempJsonParam = tempJsonParam.replaceAll("\\\\", "");
                }
                map.put("specs", tempJsonParam);//参数
            }
            List<PtmCmpSkuImage> ptmCmpSkuImages = ptmCmpSkuImageService.findPtmCmpSkuImages(ptmCmpSku.getId());
            map.put("images", getImageArray(ptmCmpSkuImages));
            map.put("distribution", 5);
            jsonObject.put("data", JSONObject.toJSON(map));
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

}
