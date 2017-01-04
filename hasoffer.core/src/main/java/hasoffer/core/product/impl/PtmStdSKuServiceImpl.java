package hasoffer.core.product.impl;

import hasoffer.base.enums.CategoryFilterParams;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.cache.CategoryCacheManager;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.search.ISearchService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.ApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hs on 2016年11月28日.
 * Time 17:21
 */
@Service
public class PtmStdSKuServiceImpl implements IPtmStdSkuService {
    private static final String SOLR_GET_PTMSTDSKU_BY_MINID = " select t from PtmStdSku t where id >= ?0";
    private static final String API_GET_PTMSTDSKU_BY_SKUID = " select t from PtmStdSku t where id = ?0 and t.";
    @Resource
    CategoryCacheManager categoryCacheManager;
    @Resource
    IPtmStdPriceService iPtmStdPriceService;
    @Resource
    ISearchService searchService;
    @Resource
    MongoDbManager mongoDbManager;
    @Resource
    private IDataBaseManager dbm;
    @Resource
    private PtmStdSkuIndexServiceImpl ptmStdSkuIndexServicel;

    @Override
    public PtmStdSku getStdSkuById(Long id) {
        return dbm.get(PtmStdSku.class, id);
    }

    @Override
    public PageableResult<PtmStdSku> getPtmStdSkuListByMinId(Long minId, int page, int pageSize) {
        return dbm.queryPage(SOLR_GET_PTMSTDSKU_BY_MINID, page, pageSize, Arrays.asList(minId));
    }

    @Override
    public void importPtmStdSku2Solr(PtmStdSku ptmStdSku) {
        //导入sku(product)到solr
        if (ptmStdSku == null) {
            return;
        }
        PtmStdSku ptmStdSku1 = dbm.get(PtmStdSku.class, ptmStdSku.getId());
        if (ptmStdSku1 == null) {
            //delete it from solr ,if it exist .
            ptmStdSkuIndexServicel.remove(ptmStdSku.getId() + "");
            return;
        }
        PtmStdSkuModel ptmStdSKuModel = getPtmStdSKuModel(ptmStdSku);
        if (ptmStdSKuModel == null) {
            ptmStdSkuIndexServicel.remove(ptmStdSku.getId() + "");
        } else {
            ptmStdSkuIndexServicel.createOrUpdate(ptmStdSKuModel);
        }
    }

    @Override
    public List<PtmStdPrice> listStdPrice(long ptmStdSkuId) {
        return dbm.query("SELECT t FROM PtmStdPrice t WHERE t.stdSkuId = ?0 ", Arrays.asList(ptmStdSkuId));
    }

    @Override
    public List<String> getPtmStdSkuBrandList() {
        List<String> brandList = dbm.query("select distinct(t.brand) from PtmStdSku t ");
        return brandList;
    }

    @Override
    public List<PtmStdPrice> getSimilaryPricesByPriceAndRating(PtmStdSku ptmStdSku) {

        return null;
    }

    public PtmStdSkuModel getPtmStdSKuModel(PtmStdSku ptmStdSku1) {
        PtmStdSkuModel ptmStdSkuModel = new PtmStdSkuModel(ptmStdSku1);
        //  递归获取类目树类目
        List<PtmCategory> routerCategoryList = categoryCacheManager.getRouterCategoryList(ptmStdSku1.getCategoryId());
        setCategoryList(ptmStdSkuModel, routerCategoryList);
        //  price 列表
        List<PtmStdPrice> priceList = iPtmStdPriceService.getPtmStdPriceList(ptmStdSku1.getId(), SkuStatus.ONSALE);
        //符合条件的sku筛选
        Set<Website> websiteSet = new HashSet<>();
        //最低价,高价
        int totalCommentNumber = 0;
        int tempRatingNumber = 0;
        if (priceList != null && priceList.size() > 0) {
            Iterator<PtmStdPrice> iterator = priceList.iterator();
            while (iterator.hasNext()) {
                PtmStdPrice next = iterator.next();
                if (next.getPrice() <= 0) {
                    iterator.remove();
                }
                if (next.getWebsite() != null) {
                    websiteSet.add(next.getWebsite());
                }
                totalCommentNumber += next.getCommentsNumber();
                tempRatingNumber += next.getRatings() * next.getCommentsNumber();
            }
        } else {
            return null;
        }
        if (websiteSet.size() < 1) {
            System.out.println("site size <1");
            return null;
        }
        //按价格排序
        ApiUtils.getSortedStdPriceListByClicCountAsc(priceList);
        PtmStdSkuDetail ptmStdSkuDetail = mongoDbManager.queryOne(PtmStdSkuDetail.class, ptmStdSku1.getId());
        if (ptmStdSkuDetail != null) {
            List<PtmStdSkuParamGroup> paramGroups = ptmStdSkuDetail.getParamGroups();
            setStdModel(paramGroups, ptmStdSkuModel);
        }
        float minPrice = priceList.get(0).getPrice();
        float maxPrice = priceList.get(priceList.size() - 1).getPrice();
        int ratingNumber = ApiUtils.returnNumberBetween0And5(BigDecimal.valueOf(tempRatingNumber).divide(BigDecimal.valueOf(totalCommentNumber == 0 ? 1 : totalCommentNumber), 0, BigDecimal.ROUND_HALF_UP).longValue());
        ptmStdSkuModel.setRating(ratingNumber);
        ptmStdSkuModel.setReview(totalCommentNumber);
//        SrmProductSearchCount searchCount = searchService.findSearchCountByProductId(ptmStdSku1.getId());
        ptmStdSkuModel.setMinPrice(minPrice);
        ptmStdSkuModel.setMaxPrice(maxPrice);
//        ptmStdSkuModel.setSearchCount(searchCount == null ? 0 : searchCount.getCount());
        ptmStdSkuModel.setStoreCount(websiteSet.size());
        return ptmStdSkuModel;
    }

    private void setCategoryList(PtmStdSkuModel ptmStdSkuModel, List<PtmCategory> routerCategoryList) {
        if (routerCategoryList != null && routerCategoryList.size() > 0) {
            //cate1
            ptmStdSkuModel.setCate1(routerCategoryList.get(0).getId());
            ptmStdSkuModel.setCate1Name(routerCategoryList.get(0).getName());
            if (routerCategoryList.size() > 1) {
                ptmStdSkuModel.setCate2(routerCategoryList.get(1).getId());
                ptmStdSkuModel.setCate2Name(routerCategoryList.get(1).getName());
            }
            if (routerCategoryList.size() > 2) {
                ptmStdSkuModel.setCate3(routerCategoryList.get(2).getId());
                ptmStdSkuModel.setCate3Name(routerCategoryList.get(2).getName());
            }
        }
    }

    private boolean compareIgnoreCase(String name, CategoryFilterParams params) {
        name = name.toLowerCase().replaceAll(" ", "");
        String paramString = params.name().toLowerCase();
        if (paramString.contains("_")) {
            paramString = paramString.replaceAll("_", "");
        }
        return name.equals(paramString);
    }

    private void setStdModel(List<PtmStdSkuParamGroup> ptmStdSkuParamGroups, PtmStdSkuModel ptmStdSkuModel) {
//        try {
        for (PtmStdSkuParamGroup ptmStdSkuParamGroup : ptmStdSkuParamGroups) {
            String groupName = ptmStdSkuParamGroup.getName();
            List<PtmStdSkuParamNode> params = ptmStdSkuParamGroup.getParams();
            for (PtmStdSkuParamNode ptmStdSkuParamNode : params) {
                String name = ptmStdSkuParamNode.getName();
                //General---> launch Date,brand,model,操作系统
                switch (groupName) {
                    case "General":
                        setGeneral(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Design":
                        setDesign(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Display":
                        setDisplays(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Performance":
                        setPerformance(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Storage":
                        setStorage(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Main Camera":
                        setMainCamera(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Front Camera":
                        setFontCamera(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Battery":
                        setBattery(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Network & Connectivity":
                        setNetworkConnectivity(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Multimedia":
                        setMultimedia(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    case "Special Features":
                        setSpecialFeatures(ptmStdSkuModel, ptmStdSkuParamNode, name);
                        break;
                    default:
                }
            }
        }
      /*  } catch (Exception e) {
            System.out.println("exception occur while set model std "+e.getMessage());
            return;
        }*/
    }

    private void setQueryScreenSize(PtmStdSkuModel ptmStdSkuModel, float size) {
        if (size <= 3) {
            ptmStdSkuModel.setQueryScreenSize("Less than 3 inch");
        } else if (3 <= size && size <= 3.5) {
            ptmStdSkuModel.setQueryScreenSize("3-3.5inch");
        } else if (3.5 <= size && size <= 4) {
            ptmStdSkuModel.setQueryScreenSize("3.5-4inch");
        } else if (4 <= size && size <= 4.5) {
            ptmStdSkuModel.setQueryScreenSize("4-4.5inch");
        } else if (4.5 <= size && size <= 5) {
            ptmStdSkuModel.setQueryScreenSize("4.5-5inch");
        } else if (5 <= size && size <= 5.5) {
            ptmStdSkuModel.setQueryScreenSize("5-5.5inch");
        } else {
            ptmStdSkuModel.setQueryScreenSize("5.5inch&More");
        }
    }

    private void setQuerySecPriCamera(PtmStdSkuModel ptmStdSkuModel, float size, boolean primary) {
        //0-1.9MP、2-2.9MP、3-4.9MP、5-7.9MP、8MP&Above
        if (primary) {
            if (size <= 1.9) {
                ptmStdSkuModel.setQueryPrimaryCamera("0-1.9MP");
            } else if (size <= 2.9) {
                ptmStdSkuModel.setQueryPrimaryCamera("2-2.9MP");
            } else if (size <= 4.9) {
                ptmStdSkuModel.setQueryPrimaryCamera("3-4.9MP");
            } else if (size <= 7.9) {
                ptmStdSkuModel.setQueryPrimaryCamera("5-7.9MP");
            } else {
                ptmStdSkuModel.setQueryPrimaryCamera("8MP&Above");
            }
        } else {
            if (size <= 1.9) {
                ptmStdSkuModel.setQuerySecondaryCamera("0-1.9MP");
            } else if (size <= 2.9) {
                ptmStdSkuModel.setQuerySecondaryCamera("2-2.9MP");
            } else if (size <= 4.9) {
                ptmStdSkuModel.setQuerySecondaryCamera("3-4.9MP");
            } else if (size <= 7.9) {
                ptmStdSkuModel.setQuerySecondaryCamera("5-7.9MP");
            } else {
                ptmStdSkuModel.setQuerySecondaryCamera("8MP&Above");
            }
        }

    }

    private void calcMBRam(PtmStdSkuModel ptmStdSkuModel, int numberFromString) {
        if (numberFromString >= 1024) {
            numberFromString = numberFromString / 1024;
            setQueryRam(ptmStdSkuModel, numberFromString);
        } else if (numberFromString >= 512) {
            ptmStdSkuModel.setQueryRam("512MB-1GB");
        } else if (numberFromString <= 512) {
            ptmStdSkuModel.setQueryRam("Less than 512MB");
        }
    }

    private void setQueryBatteryCapacity(PtmStdSkuModel ptmStdSkuModel, int capacity) {
        //1000-1999mAh、2000-2999mAh、3000-3999mAh、Less than 1000 mAh、More than 4000 mAh
        if (capacity >= 1000) {
            if (capacity <= 1999) {
                ptmStdSkuModel.setQueryBatteryCapacity("1000-1999mAh");
            } else if (capacity <= 2999) {
                ptmStdSkuModel.setQueryBatteryCapacity("2000-2999mAh");
            } else if (capacity <= 3999) {
                ptmStdSkuModel.setQueryBatteryCapacity("3000-3999mAh");
            } else {
                ptmStdSkuModel.setQueryBatteryCapacity("More than 4000 mAh");
            }
        } else {
            ptmStdSkuModel.setQueryBatteryCapacity("Less than 1000 mAh");
        }
    }

    private void setQueryRam(PtmStdSkuModel ptmStdSkuModel, int numberFromString) {
        if (numberFromString <= 2) {
            ptmStdSkuModel.setQueryRam("1GB-2GB");
        } else if (2 <= numberFromString && numberFromString <= 3) {
            ptmStdSkuModel.setQueryRam("2GB-3GB");
        } else if (3 <= numberFromString && numberFromString <= 4) {
            ptmStdSkuModel.setQueryRam("3GB-4GB");
        } else if (4 <= numberFromString) {
            ptmStdSkuModel.setQueryRam("4GB&More");
        }
    }

    private void setQueryInternalMemory(PtmStdSkuModel ptmStdSkuModel, int internalMemory) {
        //1GB-2GB、128GB、16GB、2GB-4GB、256GB&Above、32GB、4GB、64GB、8GB、Less than 1GB
        internalMemory = internalMemory / 1024;
        if (internalMemory < 1) {
            ptmStdSkuModel.setQueryInternalMemory("Less than 1GB");
            return;
        } else if (internalMemory <= 2) {
            ptmStdSkuModel.setQueryInternalMemory("1GB-2GB");
            return;
        } else if (2 <= internalMemory && internalMemory <= 4) {
            ptmStdSkuModel.setQueryInternalMemory("2GB-4GB");
            return;
        } else if (internalMemory >= 256) {
            ptmStdSkuModel.setQueryInternalMemory("256GB&Above");
            return;
        }
        switch (internalMemory) {
            case 4:
                ptmStdSkuModel.setQueryInternalMemory("4GB");
                break;
            case 8:
                ptmStdSkuModel.setQueryInternalMemory("8GB");
                break;
            case 16:
                ptmStdSkuModel.setQueryInternalMemory("16GB");
                break;
            case 32:
                ptmStdSkuModel.setQueryInternalMemory("32GB");
                break;
            case 64:
                ptmStdSkuModel.setQueryInternalMemory("64GB");
                break;
            case 128:
                ptmStdSkuModel.setQueryInternalMemory("128GB");
                break;
            default:
        }
    }

    private void setOpeartingSystem(String opreatingSystem, PtmStdSkuModel ptmStdSkuModel) {
        if (StringUtils.isNotEmpty(opreatingSystem)) {
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Android".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Android");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Bada".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Bada");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Blackberry".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Blackberry");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Blackberry OS".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Blackberry OS");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Brew".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Brew");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Firefox".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Firefox");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("iOS".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("iOS");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Linux".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Linux");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Nokia".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Nokia");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Asha".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Asha");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Nokia X Software".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Nokia X Software");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Propreitory".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Propreitory");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Sailfish".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Sailfish");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Symbian".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Symbian");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Tizen".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Tizen");
                return;
            }
            if (opreatingSystem.replaceAll(" ", "").toLowerCase().contains("Windows".replaceAll(" ", "").toLowerCase())) {
                ptmStdSkuModel.setOperating_System("Windows");
                return;
            }
        }
    }

    /**
     * 设置General 组数据
     *
     * @param ptmStdSkuModel
     * @param ptmStdSkuParamNode
     * @param name
     */
    public void setGeneral(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
        if (compareIgnoreCase(name, CategoryFilterParams.Brand)) {
            ptmStdSkuModel.setBrand(ptmStdSkuParamNode.getValue());
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Model)) {
            ptmStdSkuModel.setModel(ptmStdSkuParamNode.getValue());
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Launch_Date)) {
            String launchDateString = ptmStdSkuParamNode.getValue();
            //February 3, 2016 (Official)
            if (launchDateString.contains(" (Official)")) {
                launchDateString = launchDateString.replace(" (Official)", "");
            }
            try {
                Date launchDate = simpleDateFormat.parse(launchDateString);
                ptmStdSkuModel.setLaunch_Date(launchDate);
            } catch (Exception e) {
                return;
            }
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Operating_System)) {
            String opreatingSystem = ptmStdSkuParamNode.getValue();
            setOpeartingSystem(opreatingSystem, ptmStdSkuModel);
            return;
        }
        if (name.equals("SIM Slot(s)")) {
            ptmStdSkuModel.setSIM_Slot(ptmStdSkuParamNode.getValue());
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.SIM_Size)) {
            ptmStdSkuModel.setSIM_Size(ptmStdSkuParamNode.getValue().split(",")[0]);
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Network)) {
            String netWorkString = ptmStdSkuParamNode.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (netWorkString.contains("2")) {
                stringBuilder.append("2G");
            }
            if (netWorkString.contains("3")) {
                stringBuilder.append(ConstantUtil.SOLR_DEFAULT_MULTIVALUEDVALUE_FIELD_SPLIT);
                stringBuilder.append("3G");
            }
            if (netWorkString.contains("4")) {
                stringBuilder.append(ConstantUtil.SOLR_DEFAULT_MULTIVALUEDVALUE_FIELD_SPLIT);
                stringBuilder.append("4G");
            }
            ptmStdSkuModel.setNetwork_Support(stringBuilder.toString());
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Fingerprint_Sensor)) {
            ptmStdSkuModel.setFingerprint_Sensor(ptmStdSkuParamNode.getValue());
        }
    }

    public void setDesign(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //Weight
        if (compareIgnoreCase(name, CategoryFilterParams.Weight)) {
            int numberFromString = ApiUtils.getNumberFromString(ptmStdSkuParamNode.getValue());
            ptmStdSkuModel.setWeight(numberFromString);
        }
    }

    public void setDisplays(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //Screen_Resolution  Screen_Size queryScreenSize  Pixel_Density  Touch_Screen  Screen_to_Body_Ratio
        if (compareIgnoreCase(name, CategoryFilterParams.Screen_Size)) {
            String screenSize = ptmStdSkuParamNode.getValue();
            int inch = screenSize.indexOf("inch");
            if (inch != -1) {
                screenSize = screenSize.substring(0, inch).replaceAll(" ", "");
                float size = Float.parseFloat(screenSize);
                ptmStdSkuModel.setScreen_Size(size);
                setQueryScreenSize(ptmStdSkuModel, size);
            }
            return;
        }

        if (compareIgnoreCase(name, CategoryFilterParams.Screen_Resolution)) {
            String screenResolution = ptmStdSkuParamNode.getValue();
            //处理一下
            if (StringUtils.isNotEmpty(screenResolution)) {
                //"Screen Resolution"中分为五类分别是 4096x2160（4K）、 2048x1536（2K）、1920x1080（Full HD）、1280x720（HD）、High PPI Display
                if (screenResolution.replaceAll(" ", "").toLowerCase().contains("1920x1080") || screenResolution.replaceAll(" ", "").toLowerCase().contains("1080x1920")) {
                    ptmStdSkuModel.setScreen_Resolution("1920x1080 (Full HD)");
                } else if (screenResolution.replaceAll(" ", "").toLowerCase().contains("1280x720") || screenResolution.replaceAll(" ", "").toLowerCase().contains("720x1280")) {
                    ptmStdSkuModel.setScreen_Resolution("1280x720 (HD)");
                } else {
                    ptmStdSkuModel.setScreen_Resolution("Others");
                }
            }
            return;
        }

        if (compareIgnoreCase(name, CategoryFilterParams.Pixel_Density)) {
            String value = ptmStdSkuParamNode.getValue();
            int numberFromString = ApiUtils.getNumberFromString(value);
            if (numberFromString > 0) {
                ptmStdSkuModel.setPixel_Density(numberFromString);
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Touch_Screen)) {
            ptmStdSkuModel.setTouch_Screen(ptmStdSkuParamNode.getValue());

        }
        if (compareIgnoreCase(name, CategoryFilterParams.Screen_to_Body_Ratio)) {
            String value = ptmStdSkuParamNode.getValue();
            if (StringUtils.isNotEmpty(value) && value.contains("%")) {
                value = value.substring(0, value.indexOf("%")).replaceAll(" ", "");
                ptmStdSkuModel.setScreen_to_Body_Ratio(Float.parseFloat(value));
            }
        }
    }

    public void setPerformance(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //Processor  Graphics  queryRam RAM
        if (compareIgnoreCase(name, CategoryFilterParams.RAM)) {
            String ram = ptmStdSkuParamNode.getValue();
            int numberFromString = ApiUtils.getNumberFromString(ram);
            if (numberFromString != -1) {
                //整数
                if (ram.contains("GB")) {
                    ptmStdSkuModel.setRAM(numberFromString * 1024);
                    setQueryRam(ptmStdSkuModel, numberFromString);
                } else {
                    //MB
                    calcMBRam(ptmStdSkuModel, numberFromString);
                    ptmStdSkuModel.setRAM(numberFromString);
                }
            } else {
                //小数,只处理GB
                String stringRam = ApiUtils.getStringNumberFromString(ram);
                if (!stringRam.equals("")) {
                    if (ram.contains("GB")) {
                        int mbNumber = BigDecimal.valueOf(Float.parseFloat(stringRam)).multiply(BigDecimal.valueOf(1024)).intValue();
                        calcMBRam(ptmStdSkuModel, numberFromString);
                        ptmStdSkuModel.setRAM(mbNumber);
                    }
                    //TODO MB如果有小数先不处理
                }
            }
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Processor)) {
            ptmStdSkuModel.setProcessor(ptmStdSkuParamNode.getValue());
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Graphics)) {
            ptmStdSkuModel.setGraphics(ptmStdSkuParamNode.getValue());
        }
    }

    public void setStorage(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //queryInternalMemory  InternalMemory  Expandable_Memory
        if (compareIgnoreCase(name, CategoryFilterParams.Internal_Memory)) {
            String internalMemory = ptmStdSkuParamNode.getValue();
            int numberFromString = ApiUtils.getNumberFromString(internalMemory);
            if (numberFromString != -1) {
                //整数
                if (internalMemory.contains("GB")) {
                    numberFromString = numberFromString * 1024;
                    ptmStdSkuModel.setInternal_Memory(numberFromString);
                } else {
                    //MB
                    ptmStdSkuModel.setInternal_Memory(numberFromString);
                }
            } else {
                //小数,只处理GB
                String stringRam = ApiUtils.getStringNumberFromString(internalMemory);
                if (!stringRam.equals("")) {
                    if (internalMemory.contains("GB")) {
                        numberFromString = BigDecimal.valueOf(Float.parseFloat(stringRam)).multiply(BigDecimal.valueOf(1024)).intValue();
                        ptmStdSkuModel.setInternal_Memory(numberFromString);
                    }
                    //TODO MB如果有小数先不处理
                }
            }
            //传过去MB单位的
            setQueryInternalMemory(ptmStdSkuModel, numberFromString);
            return;
        }

        if (compareIgnoreCase(name, CategoryFilterParams.Expandable_Memory)) {
            String expandableMemory = ptmStdSkuParamNode.getValue();
            ptmStdSkuModel.setExpandable_Memory(ApiUtils.getNumberFromString(expandableMemory));
            return;
        }
    }

    public void setMainCamera(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //Sensor queryPrimaryCamera  Autofocus Aperture  Flash Image_Resolution  Camera_Features  Video_Recording
        if (compareIgnoreCase(name, CategoryFilterParams.RESOLUTION)) {

            String resolution = ptmStdSkuParamNode.getValue();
            int mp = resolution.indexOf("MP");
            if (mp != -1) {
                float floatResolution = Float.parseFloat(resolution.substring(0, mp).replaceAll(" ", ""));
                ptmStdSkuModel.setPrimary_Camera(floatResolution);
                setQuerySecPriCamera(ptmStdSkuModel, floatResolution, true);
            }
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Autofocus)) {
            if ("No".equals(ptmStdSkuParamNode.getValue())) {
                ptmStdSkuModel.setAutofocus("No");
            } else {
                ptmStdSkuModel.setAutofocus("yes");
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Aperture)) {
            ptmStdSkuModel.setAperture(ptmStdSkuParamNode.getValue());
        }

        if (compareIgnoreCase(name, CategoryFilterParams.Flash)) {
            if ("No".equals(ptmStdSkuParamNode.getValue())) {
                ptmStdSkuModel.setFlash("No");
            } else {
                ptmStdSkuModel.setFlash("yes");
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Image_Resolution)) {
            ptmStdSkuModel.setImage_Resolution(ptmStdSkuParamNode.getValue());
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Camera_Features)) {
            ptmStdSkuModel.setCamera_Features(ptmStdSkuParamNode.getValue());
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Video_Recording)) {
            ptmStdSkuModel.setVideo_Recording(ptmStdSkuParamNode.getValue());
        }
    }

    public void setFontCamera(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //querySecondaryCamera secondaryAutofocus SecondaryFlash
        if (compareIgnoreCase(name, CategoryFilterParams.RESOLUTION)) {
            String resolution = ptmStdSkuParamNode.getValue();
            int mp = resolution.indexOf("MP");
            if (mp != -1) {
                float floatResolution = Float.parseFloat(resolution.substring(0, mp).replaceAll(" ", ""));
                ptmStdSkuModel.setPrimary_Camera(floatResolution);
                setQuerySecPriCamera(ptmStdSkuModel, floatResolution, false);
            }
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Autofocus)) {
            if ("No".equals(ptmStdSkuParamNode.getValue())) {
                ptmStdSkuModel.setSecondaryAutofocus("No");
            } else {
                ptmStdSkuModel.setSecondaryAutofocus("yes");
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.SecondaryFlash)) {
            if ("No".equals(ptmStdSkuParamNode.getValue())) {
                ptmStdSkuModel.setSecondaryFlash("No");
            } else {
                ptmStdSkuModel.setSecondaryFlash("yes");
            }
        }

    }

    public void setBattery(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //queryBatteryCapacity  Type  User_Replaceable  Quick_Charging
        if (compareIgnoreCase(name, CategoryFilterParams.CAPACITY)) {
            String batteryCapacity = ptmStdSkuParamNode.getValue();
            if (batteryCapacity.toLowerCase().contains("mah")) {
                int mAh = batteryCapacity.indexOf("mAh");
                if (mAh != -1) {
                    batteryCapacity = batteryCapacity.substring(0, mAh).replaceAll(" ", "");
                    int numberFromString = ApiUtils.getNumberFromString(batteryCapacity);
                    ptmStdSkuModel.setBattery_Capacity(numberFromString);
                    setQueryBatteryCapacity(ptmStdSkuModel, numberFromString);
                }
            }
            return;
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Type)) {
            ptmStdSkuModel.setType(ptmStdSkuParamNode.getValue());
        }
        if (compareIgnoreCase(name, CategoryFilterParams.User_Replaceable)) {
            ptmStdSkuModel.setUser_Replaceable(ptmStdSkuParamNode.getValue());
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Quick_Charging)) {
            ptmStdSkuModel.setQuick_Charging(ptmStdSkuParamNode.getValue());
        }
    }

    public void setNetworkConnectivity(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //VoLTE  WiFi  Bluetooth  GPS  NFC
        if (compareIgnoreCase(name, CategoryFilterParams.VoLTE)) {
            ptmStdSkuModel.setVoLTE(ptmStdSkuParamNode.getValue());
        }
        if (name.equals("Wi-Fi")) {
            String value = ptmStdSkuParamNode.getValue();
            if (value.equals("No")) {
                ptmStdSkuModel.setWiFi("No");
            } else {
                ptmStdSkuModel.setWiFi("yes");
            }

        }
        if (compareIgnoreCase(name, CategoryFilterParams.Bluetooth)) {
            String value = ptmStdSkuParamNode.getValue();
            if (value.equals("No")) {
                ptmStdSkuModel.setBluetooth("No");
            } else {
                ptmStdSkuModel.setBluetooth("yes");
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.GPS)) {
            String value = ptmStdSkuParamNode.getValue();
            if (value.equals("No")) {
                ptmStdSkuModel.setGPS("No");
            } else {
                ptmStdSkuModel.setGPS("yes");
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.NFC)) {
            String value = ptmStdSkuParamNode.getValue();
            if (value.equals("No")) {
                ptmStdSkuModel.setNFC("No");
            } else {
                ptmStdSkuModel.setNFC("yes");
            }
        }
    }

    public void setMultimedia(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //FM_Radio  Loudspeaker  Audio_Jack
        if (compareIgnoreCase(name, CategoryFilterParams.FM_Radio)) {
            String value = ptmStdSkuParamNode.getValue();
            if (value.equals("No")) {
                ptmStdSkuModel.setFM_Radio("No");
            } else {
                ptmStdSkuModel.setFM_Radio("yes");
            }
        }
        if (compareIgnoreCase(name, CategoryFilterParams.Loudspeaker)) {
            String value = ptmStdSkuParamNode.getValue();
            if (value.equals("No")) {
                ptmStdSkuModel.setLoudspeaker("No");
            } else {
                ptmStdSkuModel.setLoudspeaker("yes");
            }
        }

        if (compareIgnoreCase(name, CategoryFilterParams.Audio_Jack)) {
            ptmStdSkuModel.setAudio_Jack(ptmStdSkuParamNode.getValue());
        }
    }

    public void setSpecialFeatures(PtmStdSkuModel ptmStdSkuModel, PtmStdSkuParamNode ptmStdSkuParamNode, String name) {
        //Other_Sensors
        if (compareIgnoreCase(name, CategoryFilterParams.Other_Sensors)) {
            ptmStdSkuModel.setOther_Sensors(ptmStdSkuParamNode.getValue());
        }
    }
}
