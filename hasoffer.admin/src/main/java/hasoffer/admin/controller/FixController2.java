package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.ptm.updater.PtmProductUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.fetch.helper.WebsiteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/3/25
 * Function : 修复商品数据
 * 目前以手机类目开始，fix的项目有：cleanUrl更新、brand标记、sku非相同品牌的删除(修复完成后，更新solr)
 * 1cleanUrl-/fix2/clean_url_sku?cateId=5
 * 2brand1-/fix2/tag_brand?cateId=5
 * 3brand2-/fix2/tag_brand_man 手工处理品牌
 */
@Controller
@RequestMapping(value = "/fix2")
public class FixController2 {

    private static Logger logger = LoggerFactory.getLogger(FixController2.class);
    private static Map<String, String> dataMap = new HashMap<>();

    static {
        dataMap.put("4051", "Ktouch");
        dataMap.put("98892", "Ikall");
        dataMap.put("1135944", "Ikall");
        dataMap.put("2904", "Vell Com");
        dataMap.put("1258710", "Swipe");
        dataMap.put("653507", "Factor");
        dataMap.put("370710", "Ikall");
        dataMap.put("1194944", "Sony");
        dataMap.put("1408388", "Josh");
        dataMap.put("1402969", "Nokia");
        dataMap.put("99764", "Huawei");
        dataMap.put("4225", "i-Smart");
        dataMap.put("1567645", "Josh");
        dataMap.put("98811", "Ikall");
        dataMap.put("1380606", "i-Smart");
        dataMap.put("1212050", "Ikall");
        dataMap.put("1259333", "Ikall");
        dataMap.put("1232321", "Ikall");
        dataMap.put("352922", "i-Smart");
        dataMap.put("608537", "i-Smart");
        dataMap.put("129723", "YU");
        dataMap.put("1432634", "iBall");
        dataMap.put("365756", "Ikall");
        dataMap.put("577055", "Rage");
        dataMap.put("1457184", "Infix");
        dataMap.put("1104107", "Zen");
        dataMap.put("1251097", "Ikall");
        dataMap.put("350734", "Samsung");
        dataMap.put("1204586", "Huawei");
        dataMap.put("388120", "Josh");
        dataMap.put("1149756", "BSNL");
        dataMap.put("1590164", "Gfive");
        dataMap.put("539134", "Ikall");
        dataMap.put("98968", "Onida");
        dataMap.put("500853", "Oppo");
        dataMap.put("1117818", "Whitecherry");
        dataMap.put("1203720", "Ikall");
        dataMap.put("2863", "Reach");
        dataMap.put("98835", "UNI");
        dataMap.put("4442", "Intex");
        dataMap.put("1523577", "Champion");
        dataMap.put("1421680", "Ikall");
        dataMap.put("670436", "i-Smart");
        dataMap.put("543288", "Lava");
        dataMap.put("421334", "Intex");
        dataMap.put("916890", "Ikall");
        dataMap.put("5264", "Lava");
        dataMap.put("637270", "Lava");
        dataMap.put("1203808", "Samsung");
        dataMap.put("4259", "Rocktel");
        dataMap.put("1109425", "Ikall");
        dataMap.put("105156", "Datawind");
        dataMap.put("1056426", "iBall");
        dataMap.put("379892", "Intex");
        dataMap.put("1061602", "Sony");
        dataMap.put("1151792", "UNI");
        dataMap.put("710473", "Ikall");
        dataMap.put("974695", "Sansui");
        dataMap.put("3501", "Karbonn");
        dataMap.put("467433", "iBall");
        dataMap.put("3525", "MTech");
        dataMap.put("1503315", "Ikall");
        dataMap.put("1153746", "Infix");
        dataMap.put("1406105", "i-Smart");
        dataMap.put("1107104", "Motorola");
        dataMap.put("1050191", "Huawei");
        dataMap.put("1127608", "i-Smart");
        dataMap.put("502519", "Intex");
        dataMap.put("1383190", "HTC");
        dataMap.put("1161951", "Ikall");
        dataMap.put("1064329", "Dynacon");
        dataMap.put("1592375", "Ikall");
        dataMap.put("1139357", "iBall");
        dataMap.put("1385852", "LeEco");
        dataMap.put("1050842", "LeEco");
        dataMap.put("4597", "Philips");
        dataMap.put("1342755", "Xillion");
        dataMap.put("4083", "Xolo");
        dataMap.put("1222244", "Karbonn");
        dataMap.put("401152", "Ikall");
        dataMap.put("106676", "Ikall");
        dataMap.put("1153913", "Infix");
        dataMap.put("374169", "Lava");
        dataMap.put("5864", "i-Smart");
        dataMap.put("1362381", "Rage");
        dataMap.put("1123337", "Dynacon");
        dataMap.put("4710", "Ikall");
        dataMap.put("4746", "Go Hello");
        dataMap.put("1056642", "Intex");
        dataMap.put("99675", "Alcatel");
        dataMap.put("99663", "RELIANCE");
        dataMap.put("598320", "Ikall");
        dataMap.put("1156106", "BlackBerry");
        dataMap.put("1602955", "Infix");
        dataMap.put("412509", "Panasonic");
        dataMap.put("1139328", "Ikall");
        dataMap.put("520425", "Ikall");
        dataMap.put("1586230", "Intex");
        dataMap.put("1267872", "Zen");
        dataMap.put("728422", "Ikall");
        dataMap.put("6182", "Lava");
        dataMap.put("1179179", "Ikall");
        dataMap.put("5461", "Hitech");
        dataMap.put("5841", "Josh");
        dataMap.put("1468161", "UNI");
        dataMap.put("3886", "Karbonn");
        dataMap.put("530328", "Ikall");
        dataMap.put("4733", "WHAM");
        dataMap.put("514207", "Ikall");
        dataMap.put("99688", "Nokia");
        dataMap.put("6615", "BSNL");
        dataMap.put("513801", "iBall");
        dataMap.put("1123922", "Ikall");
        dataMap.put("2786", "Karbonn");
        dataMap.put("1080642", "LeEco");
        dataMap.put("1130901", "Mi");
        dataMap.put("4377", "Digimac");
        dataMap.put("98948", "Ikall");
        dataMap.put("1158136", "MTech");
        dataMap.put("98688", "LeEco");
        dataMap.put("1601512", "Alcatel");
        dataMap.put("1479069", "Acer");
        dataMap.put("614547", "Ikall");
        dataMap.put("993757", "LeEco");
        dataMap.put("406665", "Lava");
        dataMap.put("4998", "Motorola");
        dataMap.put("99665", "Tecmax");
        dataMap.put("1449583", "UNI");
        dataMap.put("1586494", "Gfive");
        dataMap.put("2933", "MTech");
        dataMap.put("4950", "Lava");
        dataMap.put("98959", "Intex");
        dataMap.put("98826", "Vell Com");
        dataMap.put("1364983", "i-Smart");
        dataMap.put("1188607", "Evo");
        dataMap.put("1120401", "GreenBerry");
        dataMap.put("1350636", "Microsoft");
        dataMap.put("1179169", "Videocon");
        dataMap.put("726364", "LeEco");
        dataMap.put("514023", "Ikall");
        dataMap.put("1127628", "Hitech");
        dataMap.put("379637", "Onida");
        dataMap.put("1081389", "Ikall");
        dataMap.put("674131", "UNI");
        dataMap.put("1521568", "Ikall");
        dataMap.put("1221132", "MTech");
        dataMap.put("3805", "Lenovo");
        dataMap.put("1180708", "Samsung");
        dataMap.put("98801", "Ikall");
        dataMap.put("6770", "BSNL");
        dataMap.put("617456", "MTS");
        dataMap.put("6075", "Mafe");
        dataMap.put("98852", "UNI");
        dataMap.put("98864", "Dynacon");
        dataMap.put("3205", "Ktouch");
        dataMap.put("1070361", "Nokia");
        dataMap.put("5222", "Ikall");
        dataMap.put("1365832", "Zen");
        dataMap.put("513806", "Ikall");
        dataMap.put("1153771", "Infix");
        dataMap.put("740712", "BlackBerry");
        dataMap.put("103157", "Ikall");
        dataMap.put("554724", "i-Smart");
        dataMap.put("533265", "LeEco");
        dataMap.put("1485405", "i-Smart");
        dataMap.put("369718", "Ikall");
        dataMap.put("98875", "My Phone");
        dataMap.put("3458", "Karbonn");
        dataMap.put("1116487", "vinner");
        dataMap.put("1397782", "Ikall");
        dataMap.put("1121858", "MTS");
        dataMap.put("1057021", "Virat FanBox");
        dataMap.put("1377597", "Lenovo");
        dataMap.put("1085090", "Ikall");
        dataMap.put("1081044", "Champion");
        dataMap.put("614899", "Ikall");
        dataMap.put("4559", "Ikall");
        dataMap.put("1459302", "Ikall");
        dataMap.put("4414", "Karbonn");
        dataMap.put("1109468", "Nokia");
        dataMap.put("477477", "iBall");
        dataMap.put("2932", "i-Smart");
        dataMap.put("1195527", "Ikall");
        dataMap.put("1476524", "Intex");
        dataMap.put("537256", "Ikall");
        dataMap.put("1114871", "Infix");
        dataMap.put("4818", "Lava");
        dataMap.put("1410122", "Hitech");
        dataMap.put("737631", "Oppo");
        dataMap.put("605065", "MTech");
        dataMap.put("1518255", "Nexian");
        dataMap.put("1206336", "Multilaser");
        dataMap.put("116780", "Ikall");
        dataMap.put("1153761", "Infix");
        dataMap.put("1094524", "i-Smart");
        dataMap.put("969871", "Ikall");
        dataMap.put("652075", "Ikall");
        dataMap.put("1161934", "Ikall");
        dataMap.put("98806", "Ikall");
        dataMap.put("4275", "ZTE");
        dataMap.put("4994", "BSNL");
        dataMap.put("696459", "I Grasp");
        dataMap.put("586334", "Ikall");
        dataMap.put("5920", "Yota");
        dataMap.put("3105", "BSNL");
        dataMap.put("1071537", "Micromax");
        dataMap.put("1132978", "Tecmax");
        dataMap.put("395843", "My Phone");
        dataMap.put("528656", "Vox");
        dataMap.put("1287243", "Ikall");
        dataMap.put("98752", "WHAM");
        dataMap.put("694137", "Alcatel");
        dataMap.put("1462830", "Ikall");
        dataMap.put("159184", "Ikall");
        dataMap.put("1369832", "Ikall");
        dataMap.put("512762", "Apple");
        dataMap.put("1407191", "iBall");
        dataMap.put("1118800", "Ikall");
        dataMap.put("1137074", "Ikall");
        dataMap.put("1459304", "Ikall");
        dataMap.put("718414", "Motorola");
        dataMap.put("1077970", "Microsoft");
        dataMap.put("546389", "Ikall");
        dataMap.put("369903", "Ikall");
        dataMap.put("5509", "i-Smart");
        dataMap.put("1202889", "HPL");
        dataMap.put("3617", "Zen");
        dataMap.put("543799", "Ikall");
        dataMap.put("1228897", "Microsoft");
        dataMap.put("1183208", "Ginger");
        dataMap.put("1469616", "Ikall");
        dataMap.put("969869", "Ikall");
        dataMap.put("1453095", "Ikall");
        dataMap.put("1274968", "Nevir");
        dataMap.put("1067110", "Ikall");
        dataMap.put("480476", "My Phone");
        dataMap.put("1370082", "Ikall");
        dataMap.put("686663", "Lava");
        dataMap.put("1571554", "iBall");
        dataMap.put("1385744", "Ikall");
        dataMap.put("1266512", "GreenBerry");
        dataMap.put("974386", "Ikall");
        dataMap.put("126269", "i-Smart");
        dataMap.put("4176", "Zen");
        dataMap.put("5801", "Xillion");
        dataMap.put("1347606", "Karbonn");
        dataMap.put("937910", "Ikall");
        dataMap.put("1256144", "Samsung");
        dataMap.put("1089670", "Auxus");
        dataMap.put("475772", "Ikall");
        dataMap.put("969868", "Ikall");
        dataMap.put("1468158", "Ikall");
        dataMap.put("1399309", "Ikall");
        dataMap.put("1593814", "Microsoft");
        dataMap.put("1597953", "Xolo");
        dataMap.put("1279820", "Ikall");
        dataMap.put("597055", "Ikall");
        dataMap.put("98866", "i-Smart");
        dataMap.put("1086448", "UNI");
        dataMap.put("99018", "Wishtel");
        dataMap.put("1252474", "Karbonn");
        dataMap.put("134506", "Vox");
        dataMap.put("1454224", "Celkon");
        dataMap.put("1013680", "Ikall");
        dataMap.put("505400", "Ikall");
        dataMap.put("935374", "Microsoft");
        dataMap.put("1113027", "Nokia");
        dataMap.put("654654", "Ikall");
        dataMap.put("1085088", "Ikall");
        dataMap.put("370719", "Ikall");
        dataMap.put("692271", "Sony");
        dataMap.put("1598216", "Karbonn");
        dataMap.put("1134929", "Ikall");
        dataMap.put("1046006", "LeEco");
        dataMap.put("745593", "Motorola");
        dataMap.put("539637", "WHAM");
        dataMap.put("1490120", "Ikall");
        dataMap.put("1397796", "Ikall");
        dataMap.put("396937", "Ikall");
        dataMap.put("5871", "MTS");
        dataMap.put("705705", "Samsung");
        dataMap.put("977057", "Ikall");
        dataMap.put("514063", "Ikall");
        dataMap.put("1064349", "Dynacon");
        dataMap.put("1145255", "Sony");
        dataMap.put("467085", "iBall");
        dataMap.put("1070922", "iBall");
        dataMap.put("662841", "Sony");
        dataMap.put("513855", "Ikall");
        dataMap.put("973819", "Motorola");
        dataMap.put("1138632", "i-Smart");
        dataMap.put("1407669", "Ikall");
        dataMap.put("1167407", "Ikall");
        dataMap.put("1482282", "Karbonn");
        dataMap.put("99290", "K-tel");
    }

    @Resource
    IProductService productService;
    @Resource
    ICmpSkuService cmpSkuService;

    /**
     * 根据sku的品牌标记品牌 - 手工标记
     */
    @RequestMapping(value = "/tag_brand_man", method = RequestMethod.GET)
    @ResponseBody
    public String tag_brand_man() {
        for (Map.Entry<String, String> kv : dataMap.entrySet()) {
            long id = Long.valueOf(kv.getKey());
            String brand = kv.getValue();

            PtmProductUpdater ptmProductUpdater = new PtmProductUpdater(id);
            ptmProductUpdater.getPo().setBrand(brand);
            productService.updateProduct(ptmProductUpdater);
        }

        return "ok";
    }

    /**
     * 根据sku的品牌标记品牌
     * 规则：查询sku的品牌，如果都相同，同时商品的品牌为空，则取该品牌作为商品的品牌
     *
     * @param cateId
     */
    @RequestMapping(value = "/tag_brand", method = RequestMethod.GET)
    @ResponseBody
    public void tag_brand(@RequestParam final long cateId) {
        ListAndProcessTask2<PtmProduct> productListAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        return productService.listPagedProducts(cateId, page, 1000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(o.getId());

                        if (ArrayUtils.hasObjs(cmpSkus)) {
                            String skuBrand = "";
                            for (PtmCmpSku cmpSku : cmpSkus) {
                                String skuBrand2 = cmpSku.getBrand();
                                if (!StringUtils.isEmpty(skuBrand2)) {
                                    skuBrand2 = skuBrand2.trim();

                                    if (StringUtils.isEmpty(skuBrand)) {
                                        skuBrand = skuBrand2;
                                        continue;
                                    } else {
                                        if (!skuBrand.equalsIgnoreCase(skuBrand2)) {
                                            print(o.getId() + "-diff sku brand," + skuBrand + "," + skuBrand2);
                                            return;
                                        }
                                    }
                                }
                            }

                            if (StringUtils.isEmpty(skuBrand)) {
                                print(o.getId() + "\t," + o.getBrand() + ",sku brand is null");
                                return;
                            }

                            String proBrand = o.getBrand();
                            if (!StringUtils.isEmpty(proBrand)) {
                                proBrand = proBrand.trim();
                                if (skuBrand.equalsIgnoreCase(proBrand)) {
                                    return;
                                } else {
                                    print(o.getId() + "," + o.getBrand() + "," + skuBrand + ",\tbrand diff!!!");
                                    return;
                                }
                            } else {
                                PtmProductUpdater ptmProductUpdater = new PtmProductUpdater(o.getId());
                                ptmProductUpdater.getPo().setBrand(skuBrand);
                                productService.updateProduct(ptmProductUpdater);
                            }

                        } else {
                            print(o.getId() + "\t no skus.");
                        }
                    }
                }
        );

        productListAndProcessTask2.setProcessorCount(10);
        productListAndProcessTask2.setQueueMaxSize(1500);

        productListAndProcessTask2.go();
    }

    /**
     * 1-将sku中
     *
     * @param cateId
     */
    @RequestMapping(value = "/clean_url_sku", method = RequestMethod.GET)
    @ResponseBody
    public void clean_url_sku(@RequestParam final long cateId) {

        final AtomicInteger delCount = new AtomicInteger(0);

        ListAndProcessTask2<PtmProduct> productListAndProcessTask2 = new ListAndProcessTask2<>(
                new IList() {
                    @Override
                    public PageableResult getData(int page) {
                        return productService.listPagedProducts(cateId, page, 1000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(o.getId());

                        for (PtmCmpSku cmpSku : cmpSkus) {
                            Set<String> urlSet = new HashSet<>();
                            boolean update = false;

                            PtmCmpSkuUpdater ptmCmpSkuUpdater = new PtmCmpSkuUpdater(cmpSku.getId());
                            if (cmpSku.getCategoryId() == null || cmpSku.getCategoryId() != 5) {
                                update = true;
                                ptmCmpSkuUpdater.getPo().setCategoryId(5L);
                            }

                            String cleanUrl = WebsiteHelper.getCleanUrl(cmpSku.getWebsite(), cmpSku.getUrl());
                            if (!cleanUrl.equalsIgnoreCase(cmpSku.getUrl())) {
                                update = true;
                                print(cmpSku.getUrl() + "\t" + cleanUrl);
                                ptmCmpSkuUpdater.getPo().setUrl(cleanUrl);
//                                cmpSkuService.updateCmpSku(cmpSku.getId(), cleanUrl, cmpSku.getColor(), cmpSku.getSize(), cmpSku.getPrice());
                            }

                            if (!urlSet.contains(cleanUrl)) {
                                urlSet.add(cleanUrl);
                                if (update) {
                                    cmpSkuService.updateCmpSku(ptmCmpSkuUpdater);
                                }
                            } else {
                                print("delete sku");
                                cmpSkuService.deleteCmpSku(cmpSku.getId());
                                delCount.addAndGet(1);
                            }
                        }
                    }
                }
        );

        productListAndProcessTask2.setProcessorCount(10);
        productListAndProcessTask2.setQueueMaxSize(1500);

        productListAndProcessTask2.go();
        print(String.format("delete sku count : %d", delCount.get()));

    }

    private void print(String str) {
        System.out.println(str);
    }
}