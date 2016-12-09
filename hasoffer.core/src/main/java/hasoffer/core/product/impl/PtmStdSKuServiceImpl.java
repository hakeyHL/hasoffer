package hasoffer.core.product.impl;

import hasoffer.base.enums.CategoryFilterParams;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.core.cache.CategoryCacheManager;
import hasoffer.core.persistence.dbm.mongo.MongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.search.ISearchService;
import hasoffer.core.utils.api.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by hs on 2016年11月28日.
 * Time 17:21
 */
@Service
public class PtmStdSKuServiceImpl implements IPtmStdSkuService {
    private static final String SOLR_GET_PTMSTDSKU_BY_MINID = " select t from PtmStdSku t where id >= ?0";
    private static final String API_GET_PTMSTDSKU_BY_SKUID = " select t from PtmStdSku t where id = ?0 and t.";
    Logger logger = LoggerFactory.getLogger(PtmStdSKuServiceImpl.class);
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
        Collections.sort(priceList, new Comparator<PtmStdPrice>() {
            @Override
            public int compare(PtmStdPrice o1, PtmStdPrice o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        });
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
        SrmProductSearchCount searchCount = searchService.findSearchCountByProductId(ptmStdSku1.getId());
        ptmStdSkuModel.setMinPrice(minPrice);
        ptmStdSkuModel.setMaxPrice(maxPrice);
        ptmStdSkuModel.setSearchCount(searchCount == null ? 0 : searchCount.getCount());
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
            List<PtmStdSkuParamNode> params = ptmStdSkuParamGroup.getParams();
            for (PtmStdSkuParamNode ptmStdSkuParamNode : params) {
                String name = ptmStdSkuParamNode.getName();
                if (compareIgnoreCase(name, CategoryFilterParams.BRAND)) {
                    ptmStdSkuModel.setBrand(ptmStdSkuParamNode.getValue());
                    continue;
                }
                if (compareIgnoreCase(name, CategoryFilterParams.RAM)) {
                    String ram = ptmStdSkuParamNode.getValue();
                    int numberFromString = ApiUtils.getNumberFromString(ram);
                    if (numberFromString != -1) {
                        //整数
                        if (ram.contains("GB")) {
                            ptmStdSkuModel.setRam(numberFromString * 1024);
                            setQueryRam(ptmStdSkuModel, numberFromString);
                        } else {
                            //MB
                            calcMBRam(ptmStdSkuModel, numberFromString);
                            ptmStdSkuModel.setRam(numberFromString);
                        }
                    } else {
                        //小数,只处理GB
                        String stringRam = ApiUtils.getStringNumberFromString(ram);
                        if (!stringRam.equals("")) {
                            if (ram.contains("GB")) {
                                int mbNumber = BigDecimal.valueOf(Float.parseFloat(stringRam)).multiply(BigDecimal.valueOf(1024)).intValue();
                                calcMBRam(ptmStdSkuModel, numberFromString);
                                ptmStdSkuModel.setRam(mbNumber);
                            }
                            //TODO MB如果有小数先不处理
                        }
                    }
                    continue;
                }
                if (compareIgnoreCase(name, CategoryFilterParams.NETWORK)) {
                    String netWorkString = ptmStdSkuParamNode.getValue();
                    if (netWorkString.contains("2")) {
                        ptmStdSkuModel.setNetwork("2G");
                    }
                    if (netWorkString.contains("3")) {
                        ptmStdSkuModel.setNetwork3G("3G");
                    }
                    if (netWorkString.contains("4")) {
                        ptmStdSkuModel.setNetwork4G("4G");
                    }
                    continue;
                }
                if (compareIgnoreCase(name, CategoryFilterParams.SCREEN_SIZE)) {
                    String screenSize = ptmStdSkuParamNode.getValue();
                    int inch = screenSize.indexOf("inch");
                    if (inch != -1) {
                        screenSize = screenSize.substring(0, inch).replaceAll(" ", "");
                        float size = Float.parseFloat(screenSize);
                        ptmStdSkuModel.setScreen_Size(size);
                        setQueryScreenSize(ptmStdSkuModel, size);
                    }
                    continue;
                }

                if (compareIgnoreCase(name, CategoryFilterParams.SCREEN_RESOLUTION)) {
                    ptmStdSkuModel.setScreen_Resolution(ptmStdSkuParamNode.getValue());
                    continue;
                }
                if (compareIgnoreCase(name, CategoryFilterParams.RESOLUTION)) {
                    String resolution = ptmStdSkuParamNode.getValue();
                    if (resolution.contains("Camera")) {
                        int mp = resolution.indexOf("MP");
                        if (mp != -1) {
                            float floatResolution = Float.parseFloat(resolution.substring(0, mp).replaceAll(" ", ""));
                            if (resolution.toLowerCase().contains("primary")) {
                                ptmStdSkuModel.setPrimary_Camera(floatResolution);
                                setQuerySecPriCamera(ptmStdSkuModel, floatResolution, true);
                            } else {
                                ptmStdSkuModel.setSecondary_Camera(floatResolution);
                                setQuerySecPriCamera(ptmStdSkuModel, floatResolution, false);
                            }
                        }
                    }
                    continue;
                }

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
                    continue;
                }

                if (compareIgnoreCase(name, CategoryFilterParams.OPERATING_SYSTEM)) {
                    ptmStdSkuModel.setOperating_System(ptmStdSkuParamNode.getValue());
                    continue;
                }

                if (compareIgnoreCase(name, CategoryFilterParams.INTERNAL_MEMORY)) {
                    String internalMemory = ptmStdSkuParamNode.getValue();
                    int numberFromString = ApiUtils.getNumberFromString(internalMemory);
                    ptmStdSkuModel.setInternal_Memory(numberFromString);
                    setQueryInternalMemory(ptmStdSkuModel, numberFromString);
                    continue;
                }

                if (compareIgnoreCase(name, CategoryFilterParams.EXPANDABLE_MEMORY)) {
                    String expandableMemory = ptmStdSkuParamNode.getValue();
                    ptmStdSkuModel.setExpandable_Memory(ApiUtils.getNumberFromString(expandableMemory));
                    continue;
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
        //暂时去掉Less than 1GB
        //TODO 这儿不合理
        if (internalMemory <= 2) {
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
}
