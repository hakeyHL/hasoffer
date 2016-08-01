package hasoffer.api.controller;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
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
    Logger logger = LoggerFactory.getLogger(AppSkuController.class);

    /**
     * 根据sku的id获取sku详细信息
     *
     * @return
     */
    @RequestMapping("info")
    public ModelAndView getSkuInfo(@RequestParam(defaultValue = "0") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(id);
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "ok");
        if (ptmCmpSku != null) {
            PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, ptmCmpSku.getId());
            Map map = new HashMap<>();
            map.put("description", ptmCmpSkuDescription.getDescription());//描述
            map.put("specs", ptmCmpSkuDescription.getJsonParam());//参数
            //
            map.put("images", "[\"http://img12.360buyimg.com/n1/jfs/t1174/164/723303127/202924/1a956bbf/554acf00N87f6cea3.jpg\",\"http://img12.360buyimg.com/n1/jfs/t1033/328/802932418/412261/261452dc/554acd64N27651f09.jpg\"]");//图片列表
            map.put("distribution", 5);
            modelAndView.addObject("data", map);
        }
       /* modelAndView.addObject("data", "{\n" +
                "        \"description\": \"划线价：商品展示的划横线价格为参考价，该价格可能是品牌专柜标价、商品吊牌价或由品牌供应商提供的正品零售价（如厂商指导价、建议零售价等）或该商品在京东平台上曾经展示过的销售价；由于地区、时间的差异性和市场行情波动，品牌专柜标价、商品吊牌价等可能会与您购物时展示的不一致，该价格仅供您参考。\\n折扣：如无特殊说明，折扣指销售商在原价、或划线价（如品牌专柜标价、商品吊牌价、厂商指导价、厂商建议零售价）等某一价格基础上计算出的优惠比例或优惠金额；如有疑问，您可在购买前联系销售商进行咨询。\",\n" +
                "        \"specs\": \"\",\n" +
                "        \"images\": [\n" +
                "            \"http://img14.360buyimg.com/n1/jfs/t2191/111/699154754/198998/32d7bfe0/5624b582Nbc01af5b.jpg\",\n" +
                "            \"http://img13.360buyimg.com/n1/jfs/t2200/173/590579185/269686/4c299e77/56174e3eN362982a4.jpg\"\n" +
                "        ],\n" +
                "        \"distribution\": 5\n" +
                "    }");*/
        return modelAndView;
    }
}
