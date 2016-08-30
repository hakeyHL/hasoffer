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
//        String temp = "{\"Fabric Care:\":\"Hand wash at 30°C, Do not bleach, Mild Iron, Do not Tumble Dry, Line Dry in shade, wash separately, do not iron on decorations/print, Use mild detergents\",\"Sales Package\":\"1 Kurti\",\"Legging Available\":\"No\",\"Ideal For\":\"Women's\",\"Other details\":\"Stitched\",\"Neck\":\"Mandarin collar\"}";
//        String ss = "\\ysf";
//        System.out.println(ss.replaceAll("\\\\", ""));
        List<PtmCmpSku> ptmCmpSkus = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PtmCmpSku ptmcmpSku = new PtmCmpSku();
            ptmcmpSku.setPrice(new Random().nextFloat());
            System.out.println("price " + i + "  " + ptmcmpSku.getPrice());
            ptmCmpSkus.add(ptmcmpSku);
        }
        for (PtmCmpSku ptmCmpSku : ptmCmpSkus) {
            System.out.println(ptmCmpSku.getPrice() + " a:");
        }

        Collections.sort(ptmCmpSkus, new Comparator<PtmCmpSku>() {
            @Override
            public int compare(PtmCmpSku o1, PtmCmpSku o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        });
        for (PtmCmpSku ptmCmpSku : ptmCmpSkus) {
            System.out.println(ptmCmpSku.getPrice() + " b:");
        }
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
        //1. 先拿到所有的价格数据
        List<PriceNode> priceNodes = iCmpSkuService.queryHistoryPrice(id);
        System.out.println(priceNodes != null ? "  priceNodes  :" + priceNodes.size() : "null a .....");
        //1.1 按照日升序给出
        Collections.sort(priceNodes, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPriceTimeL() < o2.getPriceTimeL()) {
                    return -1;
                } else if (o1.getPriceTimeL() > o2.getPriceTimeL()) {
                    return 1;
                }
                return 0;
            }
        });
        //1.2 过滤不合法数据
        Iterator<PriceNode> iterator = priceNodes.iterator();
        while (iterator.hasNext()) {
            PriceNode next = iterator.next();
            if (next.getPrice() <= 0) {
                iterator.remove();
            }
        }

        //2. 计算获得X轴显示数据
        //X轴  20天为间隔显示日期 , 格式为：　10-30
        List<String> X = new ArrayList<>();
        //2.1 最小日期 [0]
        X.add(this.getDateMMdd(priceNodes.get(0).getPriceTimeL()));
        //2.2 最大日期(一般为当前日期) [length-1]
        Long priceTimeL = priceNodes.get(priceNodes.size() - 1).getPriceTimeL();
        //2.3 遍历日期
        while (priceTimeL > priceNodes.get(0).getPriceTimeL()) {
            X.add(this.getDateMMdd(priceTimeL));
            priceTimeL = priceTimeL - 60 * 60 * 24 * 20;
        }
        //反转,按日期从小到大来
        Collections.reverse(X);


        for (PriceNode priceNode : priceNodes) {

        }

        Float maxPrice = Collections.max(priceNodes, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        }).getPrice();

        Float minPrice = Collections.max(priceNodes, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        }).getPrice();

        //3. 计算获得Y轴显示数据

        // SKU的最高价格处于（a+3(b-a)/4，b）的区间
        // 最低价格处于（a, a+(b-a)/4）
        //由最价格和最小价格算出a和b的值
        //3.1 最小值 a
        //3.2 最大值 b
        //3.3 a+(b-a)/4
        //3.4 a+(b-a)/2
        //3.5 a+3(b-a)/4）

        //4. 辅助点   --价格变化点前一天的价格按照上一个价格点给出


        // 若sku价格无变化则 则Y轴最小值为0 最高值为SKU价格

        //5. 给出坐标集合
        if (priceNodes != null && priceNodes.size() > 0) {
            for (PriceNode priceNode : priceNodes) {
//            jsonObject.put("data", JSONObject.toJSON(priceNodes));
                //查询到价格历史,开始分析 ,过滤不合理的价格
                priceXY.put(this.getDateMMdd(priceNode.getPriceTimeL()), priceNode.getPrice());
            }

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
