package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.api.controller.vo.PriceCurveVo;
import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.Httphelper;
import hasoffer.api.helper.JsonHelper;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.PriceNode;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmCmpSkuImageService;
import hasoffer.core.product.impl.CmpSkuServiceImpl;
import hasoffer.core.product.impl.ProductServiceImpl;
import hasoffer.core.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Resource
    CmpSkuServiceImpl iCmpSkuService;
    @Resource
    ProductServiceImpl productService;
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
                map.put("description", ptmCmpSkuDescription.getJsonDescription() == null ? "" : ClientHelper.delHTMLTag(ptmCmpSkuDescription.getJsonDescription()));//描述
                String tempJsonParam = ptmCmpSkuDescription.getJsonParam();
                //去除html标签
                if (!StringUtils.isEmpty(tempJsonParam)) {
                    tempJsonParam = ClientHelper.delHTMLTag(tempJsonParam);
                }
                map.put("specs", JsonHelper.getJsonMap(tempJsonParam));//参数
            }
            List<PtmCmpSkuImage> ptmCmpSkuImages = ptmCmpSkuImageService.findPtmCmpSkuImages(ptmCmpSku.getId());
            if (ptmCmpSkuImages != null && ptmCmpSkuImages.size() > 0) {
                map.put("images", getImageArray(ptmCmpSkuImages));
            } else {
                map.put("images", Arrays.asList(ptmCmpSku.getBigImagePath() == null ? "" : ImageUtil.getImageUrl(ptmCmpSku.getBigImagePath())));
            }
            map.put("distribution", 5);
            jsonObject.put("data", JSONObject.toJSON(map));
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    /**
     * 获取sku的价格曲线
     *
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("curve")
    public String getPriceCurve(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        Map<String, Float> priceXY = new HashMap<>();
        List<PriceNode> priceNodes = iCmpSkuService.queryHistoryPrice(id);
        System.out.println(priceNodes != null ? "  priceNodes  :" + priceNodes.size() : "null a .....");
        if (priceNodes != null && priceNodes.size() > 0) {
            for (PriceNode priceNode : priceNodes) {
//            jsonObject.put("data", JSONObject.toJSON(priceNodes));
                //查询到价格历史,开始分析
                priceXY.put(this.getDateMMdd(priceNode.getPriceTimeL()), priceNode.getPrice());
            }
            //X轴  20天为间隔显示日期 , 格式为：　10-30
            List<String> X = new ArrayList<>();
            X.add("4-19");
            X.add("5-11");
            X.add("6-01");
            X.add("7-12");
            X.add("8-02");
            X.add("8-22");

            //Y轴
            List<Long> Y = new ArrayList<>();
            Y.add(3750l);
            Y.add(3850l);
            Y.add(3950l);
            Y.add(4050l);
            Y.add(4150l);
            //两个数据点
            PriceCurveVo priceCurveVo = new PriceCurveVo(X, Y, priceXY, 3799l, 4088l);
            jsonObject.put("data", JSONObject.toJSON(priceCurveVo));
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        return null;
    }

    public String getDateMMdd(Long time) {
        System.out.println("transfer date to MM-dd format ");
        Date date = new Date();
        date.setTime(time);
        String format = null;
        try {
            format = new SimpleDateFormat("MM-dd").format(date);
        } catch (Exception e) {
            logger.error("transfer long date to MM-dd failed " + date);
        }
        return format;
    }
}
