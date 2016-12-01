package hasoffer.core.product.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PriceNode;
import hasoffer.core.persistence.mongo.PtmStdPriceHistoryPrice;
import hasoffer.core.persistence.po.ptm.PtmStdImage;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.updater.PtmStdPriceUpdater;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.solr.PtmStdPriceIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdPriceModel;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by hs on 2016年11月29日.
 * Time 10:50
 */
@Service
public class PtmStdPriceServiceImpl implements IPtmStdPriceService {
    private static final String API_PTMSTDPRICE_GET_PRICELIST_BY_SKUID = "SELECT t  from PtmStdPrice t where t.stdSkuId=?0 and t.skuStatus=?1";
    private static final String API_PTMSTDPRICE_GET_PRICELIST_BY_MINID = "SELECT t  from PtmStdPrice t where t.id >=?0 ";
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    PtmStdPriceIndexServiceImpl ptmStdPriceIndexService;
    private Logger logger = LoggerFactory.getLogger(PtmStdPriceServiceImpl.class);

    @Override
    public List<PtmStdPrice> getPtmStdPriceList(Long id, SkuStatus skuStatus) {
        return dbm.query(API_PTMSTDPRICE_GET_PRICELIST_BY_SKUID, Arrays.asList(id, skuStatus));
    }

    @Override
    public PtmStdPrice getPtmStdPriceById(long id) {
        return dbm.get(PtmStdPrice.class, id);
    }

    @Override
    public PageableResult<PtmStdPrice> getPagedPtmStdPriceList(Long id, SkuStatus skuStatus, int page, int pageSize) {
        return dbm.queryPage(API_PTMSTDPRICE_GET_PRICELIST_BY_SKUID, page, pageSize, Arrays.asList(id, skuStatus));
    }

    @Override
    public PageableResult<PtmStdPrice> getPagedPtmStdPriceByMinId(Long minId, int page, int pageSize) {
        return dbm.queryPage(API_PTMSTDPRICE_GET_PRICELIST_BY_MINID, page, pageSize, Arrays.asList(minId));
    }

    @Override
    public List<PtmStdPrice> getPtmstdPriceListByUrlKey(String urlKey) {
        return dbm.query("SELECT t FROM PtmStdPrice t WHERE t.urlKey = ?0 ", Arrays.asList(urlKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePtmStdPriceBySpiderFetchedProduct(long stdPriceId, FetchedProduct fetchedProduct) {

        //fetchedProduct为null
        if (fetchedProduct == null) {
            return;
        }

        //根据id，获得该商品信息
        PtmStdPrice ptmStdPrice = dbm.get(PtmStdPrice.class, stdPriceId);
        if (ptmStdPrice == null) {
            return;
        }

        //保存新抓来的价格
        if (fetchedProduct.getPrice() != 0.0f) {
            saveHistoryPrice(stdPriceId, TimeUtils.nowDate(), fetchedProduct.getPrice());
        }

        PtmStdPriceUpdater ptmStdPriceUpdater = new PtmStdPriceUpdater(stdPriceId);

        //更新逻辑如下
//        1.如果状态为OFFSALE，更新status和updateTime
//        2.如果状态为OUTSTOCK，不更新价格
//        3.如果状态为ONSALE，且价格大于0，更新价格
//        4.在2和3状态下的
//                4.1如果title不为空，且和原来数据不一致，更新title
//        5.如果原来的website为空，且新抓的website不为空，更新website
//---        6.如果新抓的skutitle不为空，且和原来的不一样，更新skutitle
//        7.如果新抓的（只更新onsale的数据）
//---        commentsNumber;//评论数大于0，更新该值
//---        ratings;//星级，该值大于0，更新该值
//        shipping = -1;//邮费，该值大于0，更新该值
//---        supportPayMethod;//支付方式，不为空，且和原来的字符串不一致，更新该值
//---        returnDays;//如果该值大于0，更新
//---        8.brand,model,如果新抓的不为null且和原来的不一样，更新

//---     7.最终设置更新时间
//---        deliveryTime;//送达时间 ex: 1-3   app2.0---暂定为5

        if (SkuStatus.OFFSALE.equals(fetchedProduct.getSkuStatus())) {//如果OFFSALE

            ptmStdPriceUpdater.getPo().setSkuStatus(SkuStatus.OFFSALE);

        } else {
            //如果售空了修改状态
            if (SkuStatus.OUTSTOCK.equals(fetchedProduct.getSkuStatus())) {

                ptmStdPriceUpdater.getPo().setSkuStatus(SkuStatus.OUTSTOCK);

            } else {

                ptmStdPriceUpdater.getPo().setSkuStatus(SkuStatus.ONSALE);

//                //更新 commentsNumber
//                long commentsNumber = fetchedProduct.getCommentsNumber();
//                if (commentsNumber > 0) {
//                    ptmCmpSkuUpdater.getPo().setCommentsNumber(commentsNumber);
//                }

//                //更新ratings
//                int ratings = fetchedProduct.getRatings();
//                if (ratings > 0) {
//                    ptmCmpSkuUpdater.getPo().setRatings(ratings);
//                }

                //更新 shipping
                float shipping = fetchedProduct.getShipping();
                if (shipping > 0) {
                    ptmStdPriceUpdater.getPo().setShippingFee(shipping);
                }

//                //更新 supportPayMethod
//                String supportPayMethod = fetchedProduct.getSupportPayMethod();
//                if (!StringUtils.isEmpty(supportPayMethod) && !StringUtils.isEqual(supportPayMethod, cmpSku.getSupportPayMethod())) {
//                    ptmCmpSkuUpdater.getPo().setSupportPayMethod(supportPayMethod);
//                }

//                //更新 returnDays
//                int returnDays = fetchedProduct.getReturnDays();
//                if (returnDays > 0) {
//                    ptmCmpSkuUpdater.getPo().setReturnDays(returnDays);
//                }
            }

            //更新 price
            //策略更新  2016-08-24由于outstock商品返回前台，所以现在offsale不更新价格，别的状态都要更新价格
            float price = fetchedProduct.getPrice();
            if (price > 0) {
                if (ptmStdPrice.getPrice() != fetchedProduct.getPrice()) {
                    ptmStdPriceUpdater.getPo().setPrice(price);
                }
            }

            //更新原价
            //策略    sku中的原价为0，且新抓的原价不为0
//            float oriPrice = fetchedProduct.getOriPrice();
//            if (ptmstd.getOriPrice() == 0.0 && oriPrice != 0.0) {
//                ptmCmpSkuUpdater.getPo().setOriPrice(oriPrice);
//            }

            if (!StringUtils.isEmpty(fetchedProduct.getTitle())) {
                if (StringUtils.isEmpty(ptmStdPrice.getTitle()) || !StringUtils.isEqual(ptmStdPrice.getTitle(), fetchedProduct.getTitle())) {
                    ptmStdPriceUpdater.getPo().setTitle(fetchedProduct.getTitle());
                }
            }

            //更新skutitle,只要新旧不一样就更新
//            if (!StringUtils.isEqual(cmpSku.getSkuTitle(), fetchedProduct.getSubTitle())) {
//                ptmCmpSkuUpdater.getPo().setSkuTitle(fetchedProduct.getSubTitle());
//            }
        }

        if (ptmStdPrice.getWebsite() == null) {
            Website website = fetchedProduct.getWebsite();
            if (website != null) {
                ptmStdPriceUpdater.getPo().setWebsite(fetchedProduct.getWebsite());
            }
        }

        //更新brand
//        if (!StringUtils.isEmpty(fetchedProduct.getBrand()) && !StringUtils.isEqual(fetchedProduct.getBrand(), cmpSku.getBrand())) {
//            ptmCmpSkuUpdater.getPo().setBrand(fetchedProduct.getBrand());
//        }

        //更新model
//        if (!StringUtils.isEmpty(fetchedProduct.getModel()) && !StringUtils.isEqual(fetchedProduct.getModel(), cmpSku.getModel())) {
//            ptmCmpSkuUpdater.getPo().setModel(fetchedProduct.getModel());
//        }

        ptmStdPriceUpdater.getPo().setUpdateTime(TimeUtils.nowDate());

        //更新 deliveryTime
//        String deliveryTime = fetchedProduct.getDeliveryTime();
//        String deliveryTime = "1-5";
//        ptmCmpSkuUpdater.getPo().setDeliveryTime(deliveryTime);

        dbm.update(ptmStdPriceUpdater);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPtmStdPriceImage(long stdPriceId, FetchedProduct fetchedProduct) {

        List<PtmStdImage> stdImageList = dbm.query("SELECT t FROM PtmStdImage t WHERE t.stdPriceId = ?0 ", Arrays.asList(stdPriceId));

        if (stdImageList == null || stdImageList.size() == 0) {

            PtmStdImage ptmStdImage = new PtmStdImage();
            ptmStdImage.setStdPriceId(stdPriceId);

            String imageUrl = fetchedProduct.getImageUrl();
            if (StringUtils.isEmpty(imageUrl)) {
                return;
            }
            ptmStdImage.setOriImageUrl(imageUrl);

            dbm.create(ptmStdImage);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUrlKey(long stdPriceId) {

        PtmStdPrice ptmStdPrice = dbm.get(PtmStdPrice.class, stdPriceId);

        PtmStdPriceUpdater updater = new PtmStdPriceUpdater(stdPriceId);

        updater.getPo().setUrlKey(HexDigestUtil.md5(ptmStdPrice.getUrl()));

        dbm.update(updater);

    }

    @Override
    public void importPtmStdPrice2Solr(PtmStdPrice ptmStdPrice) {
        //导入sku(product)到solr
        if (ptmStdPrice == null) {
            return;
        }
        PtmStdPrice ptmStdPrice1 = dbm.get(PtmStdPrice.class, ptmStdPrice.getId());
        if (ptmStdPrice1 == null) {
            //delete it from solr ,if it exist .
            ptmStdPriceIndexService.remove(ptmStdPrice.getId() + "");
            return;
        }
        PtmStdPriceModel ptmStdPriceModel = getPtmStdPriceModel(ptmStdPrice1);
        if (ptmStdPriceModel == null) {
            ptmStdPriceIndexService.remove(ptmStdPrice.getId() + "");
        } else {
            ptmStdPriceIndexService.createOrUpdate(ptmStdPriceModel);
        }
    }

    public void saveHistoryPrice(long id, Date time, float price) {
        saveHistoryPrice(id, Arrays.asList(new PriceNode(time, price)));
    }

    public void saveHistoryPrice(Long sid, List<PriceNode> priceNodes) {
        if (ArrayUtils.isNullOrEmpty(priceNodes)) {
            return;
        }
        final int PRICE_HISTORY_SIZE = 90;

        Set<PriceNode> priceNodeSet = new LinkedHashSet<>();
        priceNodeSet.addAll(priceNodes);

        PtmStdPriceHistoryPrice historyPrice = mdm.queryOne(PtmStdPriceHistoryPrice.class, sid);
        if (historyPrice != null) {
            priceNodeSet.addAll(historyPrice.getPriceNodes());
        }

        List<PriceNode> priceNodes1 = new ArrayList<>();
        priceNodes1.addAll(priceNodeSet);

        if (historyPrice == null) {
            historyPrice = new PtmStdPriceHistoryPrice(sid, priceNodes1);
        }

        historyPrice.setPriceNodes(priceNodes1);

        // 排序
        Collections.sort(priceNodes1, new Comparator<PriceNode>() {
            @Override
            public int compare(PriceNode o1, PriceNode o2) {
                if (o1.getPriceTimeL() > o2.getPriceTimeL()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        if (priceNodes1.size() > PRICE_HISTORY_SIZE) {
            historyPrice.setPriceNodes(priceNodes1.subList(priceNodes1.size() - PRICE_HISTORY_SIZE, priceNodes1.size()));
        }

        mdm.save(historyPrice);
    }

    protected PtmStdPriceModel getPtmStdPriceModel(PtmStdPrice ptmStdPrice) {
        //获取sku列表然后转成model
        if (SkuStatus.ONSALE.equals(ptmStdPrice.getSkuStatus())) {
            return new PtmStdPriceModel(ptmStdPrice);
        }
        return null;
    }
}
