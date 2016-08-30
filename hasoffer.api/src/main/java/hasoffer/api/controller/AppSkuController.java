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
import java.math.BigDecimal;
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
        float minPrice = 49f;
        float maxPrice = 49f;
//        BigDecimal a = (BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(minPrice)).subtract(BigDecimal.valueOf(maxPrice)).divide(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(2)));
        BigDecimal a = BigDecimal.ZERO;
        //3.2 最大值 b
//        BigDecimal b = (BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(maxPrice)).subtract(BigDecimal.valueOf(minPrice)).divide(BigDecimal.valueOf(2)).subtract(BigDecimal.valueOf(2)));
        BigDecimal b = BigDecimal.valueOf(60);
        System.out.println(a.longValue());
        System.out.println(b.longValue());
        //3.3 a+(b-a)/4
        BigDecimal pointOne = a.add((b.subtract(a)).divide(BigDecimal.valueOf(4)));

        //3.4 a+(b-a)/2
        BigDecimal pointTwo = a.add((b.subtract(a)).divide(BigDecimal.valueOf(2)));

        //3.5 a+3(b-a)/4）
        BigDecimal pointThree = a.add((b.subtract(a)).multiply(BigDecimal.valueOf(0.75)));
        System.out.println(pointOne.longValue());
        System.out.println(pointTwo.longValue());
        System.out.println(pointThree.longValue());
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
        Map<String, Long> priceXY = new HashMap<>();
        //1. 先拿到所有的价格数据
        List<PriceNode> priceNodes = iCmpSkuService.queryHistoryPrice(id);
        System.out.println(priceNodes != null ? "  priceNodes  :" + priceNodes.size() : "null a .....");
        if (priceNodes != null && priceNodes.size() > 1) {
            //如果有大于1个数据则代表其有价格变化

        } else if (priceNodes != null && priceNodes.size() == 1) {
            //只有一个代表价格未变化
            // 若sku价格无变化则 则Y轴最小值为0 最高值为SKU价格*2
            BigDecimal a = BigDecimal.ZERO;
            //3.2 最大值 b
//        BigDecimal b = (BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(maxPrice)).subtract(BigDecimal.valueOf(minPrice)).divide(BigDecimal.valueOf(2)).subtract(BigDecimal.valueOf(2)));
            BigDecimal b = BigDecimal.valueOf(priceNodes.get(0).getPrice() * 2);

            //3.3 a+(b-a)/4
            BigDecimal pointOne = a.add((b.subtract(a)).divide(BigDecimal.valueOf(4)));

            //3.4 a+(b-a)/2
            BigDecimal pointTwo = a.add((b.subtract(a)).divide(BigDecimal.valueOf(2)));

            //3.5 a+3(b-a)/4）
            BigDecimal pointThree = a.add((b.subtract(a)).multiply(BigDecimal.valueOf(0.75)));
            //绘制x
            List<String> X = new ArrayList<>();
            Long priceTimeL = priceNodes.get(0).getPriceTimeL();
            int i = 3;
            while (i > 0) {
                X.add(this.getDateMMdd(priceTimeL));
                priceTimeL = priceTimeL - 60 * 60 * 24 * 20;
                i--;
            }
            //反转
            Collections.reverse(X);

            //Y轴
            List<Long> Y = new ArrayList<>();
            Y.add(a.longValue());
            Y.add(pointOne.longValue());
            Y.add(pointTwo.longValue());
            Y.add(pointThree.longValue());
            Y.add(b.longValue());

            //数据点,给两个数据点,起始和最终,都是同个值
            priceXY.put(X.get(0), BigDecimal.valueOf(priceNodes.get(0).getPrice()).longValue());
            priceXY.put(X.get(X.size() - 1), BigDecimal.valueOf(priceNodes.get(0).getPrice()).longValue());

            PriceCurveVo priceCurveVo = new PriceCurveVo(X, Y, priceXY, BigDecimal.valueOf(priceNodes.get(0).getPrice()).longValue(), BigDecimal.valueOf(priceNodes.get(0).getPrice()).longValue());
            jsonObject.put("data", JSONObject.toJSON(priceCurveVo));
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;

        } else {
            //1.1 按照日期剩升序给出
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
            BigDecimal a = (BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(minPrice)).subtract(BigDecimal.valueOf(maxPrice)).divide(BigDecimal.valueOf(2)).add(BigDecimal.ONE));
            //3.2 最大值 b
            BigDecimal b = (BigDecimal.valueOf(3).multiply(BigDecimal.valueOf(maxPrice)).subtract(BigDecimal.valueOf(minPrice)).divide(BigDecimal.valueOf(2)).subtract(BigDecimal.ONE));
            //3.3 a+(b-a)/4
            BigDecimal pointOne = a.add((b.subtract(a)).divide(BigDecimal.valueOf(4)));

            //3.4 a+(b-a)/2
            BigDecimal pointTwo = a.add((b.subtract(a)).divide(BigDecimal.valueOf(2)));

            //3.5 a+3(b-a)/4）
            BigDecimal pointThree = a.add((b.subtract(a)).multiply(BigDecimal.valueOf(0.75)));

            //Y轴
            List<Long> Y = new ArrayList<>();
            Y.add(a.longValue());
            Y.add(pointOne.longValue());
            Y.add(pointTwo.longValue());
            Y.add(pointThree.longValue());
            Y.add(b.longValue());

            //5. 给出坐标集合
            if (priceNodes != null && priceNodes.size() > 0) {
                for (PriceNode priceNode : priceNodes) {
                    //查询到价格历史,开始分析
                    priceXY.put(this.getDateMMdd(priceNode.getPriceTimeL()), BigDecimal.valueOf(priceNode.getPrice()).longValue());
                }
                //4. 辅助点   --价格变化点前一天的价格按照上一个价格点给出
                //TODO 辅助点添加 , 假数据拼接测试
                //两个数据点
                PriceCurveVo priceCurveVo = new PriceCurveVo(X, Y, priceXY, BigDecimal.valueOf(minPrice).longValue(), BigDecimal.valueOf(maxPrice).longValue());
                jsonObject.put("data", JSONObject.toJSON(priceCurveVo));
            }
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
            return null;
        }
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
