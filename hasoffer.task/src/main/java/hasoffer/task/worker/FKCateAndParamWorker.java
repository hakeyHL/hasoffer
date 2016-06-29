package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static hasoffer.base.utils.http.XPathUtils.getSubNodesByXPath;

/**
 * Created on 2016/6/20.
 */
public class FKCateAndParamWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FKCateAndParamWorker.class);
    private final String Q_CATEGORY_BYNAME = "SELECT t FROM PtmCategory2 t WHERE t.name = ?0 ";

    private ListAndProcessWorkerStatus<PtmCmpSku> ws;
    private IDataBaseManager dbm;
    private IMongoDbManager mdm;
    private ICategoryService categoryService;

    public FKCateAndParamWorker(IDataBaseManager dbm, IMongoDbManager mdm, ListAndProcessWorkerStatus<PtmCmpSku> ws, ICategoryService categoryService) {
        this.dbm = dbm;
        this.mdm = mdm;
        this.ws = ws;
        this.categoryService = categoryService;
    }

    @Override
    public void run() {
        while (true) {

            PtmCmpSku sku = ws.getSdQueue().poll();

            if (sku == null) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.debug("flipkart category and param fetch get null sleep 3 seconds");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            String url = sku.getUrl();

            //for test
//            url = "http://www.flipkart.com/ap-pulse-solid-women-s-round-neck-pink-t-shirt/p/itme8arfjjawfkxv?pid=TSHE8ARFKUCKH4EH";

            try {
                createCateAndGetParam(url, sku);
            } catch (Exception e) {
                logger.debug(e.toString());
            }
        }
    }

    private void createCateAndGetParam(String url, PtmCmpSku sku) throws HttpFetchException, ContentParseException {

        final String CATE_PATH = "//div[@data-tracking-id='product_breadCrumbs']/ul/li";
        final String DESCRIPTION_INFO1 = "//div[@class='productSpecs specSection']/table";
        final String DESCRIPTION_INFO2 = "//div[@id='veiwMoreSpecifications']/table";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        //获取导航栏
//        List<TagNode> catePathList = getSubNodesByXPath(root, CATE_PATH, new ContentParseException("cate path not found for [" + sku.getId() + "]"));
//
//        if (catePathList.size() == 0) {//获取当行个数为0，任务抓取失败
//            this.ws.getSdQueue().add(sku);
//            return;
//        }
//
//        //获取导航栏中catePath的长度，取前5位或者更小
//        int cateSize = catePathList.size();
//        cateSize = cateSize > 6 ? 6 : cateSize;
//        if (catePathList.size() == cateSize) {
//            cateSize = cateSize - 1;
//        }
//
//        long parentId = 0;
//
//        for (int i = 0; i < cateSize; i++) {
//
//            if (i == 0) {//排除导航中的第一个home
//                continue;
//            }
//
//            try {
//
//                TagNode pathNode = getSubNodeByXPath(catePathList.get(i), "/a", new ContentParseException("path not found"));
//
//                //获取类目名称
//                String pathString = StringUtils.filterAndTrim(pathNode.getText().toString(), null);
//
//                //检查类目是否存在
//                PtmCategory2 category = dbm.querySingle(Q_CATEGORY_BYNAME, Arrays.asList(pathString));
//
//                if (category != null) {
//                    parentId = category.getId();
//                } else {
//                    category = new PtmCategory2();
//
//                    category.setName(pathString);
//                    category.setLevel(i);
//                    category.setParentId(parentId);
//
//                    category = categoryService.createAppCategory(category);
//                    parentId = category.getId();
//                }
//
//            } catch (ContentParseException exception) {
//                break;
//            }
//
//        }
//
//        //给sku关联类目信息
//        PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());
//        updater.getPo().setCategoryId(parentId);
//        dbm.update(updater);

        //用来封装描述的键值对
        Map<String, String> infoMap = new HashMap<String, String>();

        //获取描述节点
        List<TagNode> infoNodeList = getSubNodesByXPath(root, DESCRIPTION_INFO1, new ContentParseException("description section not found for [" + sku.getId() + "]"));
        for (TagNode node : infoNodeList) {
            //获取具体的描述信息，填充到map集合中
            getInfo1(node, infoMap, sku);//ex    "name1":"value1"
        }

        if (infoNodeList.size() == 0) {
            List<TagNode> descNodeList = getSubNodesByXPath(root, DESCRIPTION_INFO2, new ContentParseException("description section not found for [" + sku.getId() + "]"));
            for (TagNode descNode : descNodeList) {
                getInfo1(descNode, infoMap, sku);
            }
        }
        
        String jsonDescription = JSONUtil.toJSON(infoMap);

        //将描述信息持久化到mongodb
        PtmCmpSkuDescription skuDescription = new PtmCmpSkuDescription();
        skuDescription.setId(sku.getProductId());
        skuDescription.setJsonDescription(jsonDescription);
        mdm.save(skuDescription);
        logger.debug("save description success for [" + sku.getProductId() + "]");
    }

    private void getInfo1(TagNode node, Map<String, String> infoMap, PtmCmpSku sku) throws ContentParseException {

        //描述区域分为多块
        List<TagNode> infoNodeList = getSubNodesByXPath(node, "//tbody/tr", new ContentParseException("description not found for [" + sku.getId() + "]"));

        //获取每块描述区域中的内容
        for (int i = 1; i < infoNodeList.size(); i++) {

            TagNode tagNode = infoNodeList.get(i);

            List<TagNode> paramNodeList = getSubNodesByXPath(tagNode, "//td", new ContentParseException("info not found for [" + sku.getId() + "]"));

            //如果不是键值对形式的，跳过
            if (paramNodeList.size() != 2) {
                logger.debug("parse error for [" + sku.getId() + "]");
                continue;
            }

            String name = paramNodeList.get(0).getText().toString();
            String value = StringUtils.filterAndTrim(paramNodeList.get(1).getText().toString(), null);

            infoMap.put(name, value);
        }
    }
}
