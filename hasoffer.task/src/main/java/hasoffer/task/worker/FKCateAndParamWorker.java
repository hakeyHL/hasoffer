package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory2;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodesByXPath;

/**
 * Created on 2016/6/20.
 */
public class FKCateAndParamWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(MysqlListWorker.class);
    private final String Q_CATEGORY_BYNAME = "SELECT t FROM PtmCategory2 t WHERE t.name = ?0 ";

    private ListAndProcessWorkerStatus<PtmCmpSku> ws;
    private IDataBaseManager dbm;
    private ICategoryService categoryService;

    public FKCateAndParamWorker(IDataBaseManager dbm, ListAndProcessWorkerStatus<PtmCmpSku> ws, ICategoryService categoryService) {
        this.dbm = dbm;
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

            try {
                createCateAndGetParam(url, sku);
            } catch (Exception e) {
                logger.debug(e.toString());
            }


        }
    }

    private void createCateAndGetParam(String url, PtmCmpSku sku) throws HttpFetchException, ContentParseException {

        final String CATE_PATH = "//div[@data-tracking-id='product_breadCrumbs']/ul/li";
        final String DESCRIPTION_INFO = "//div[@class='productSpecs specSection']/table";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        List<TagNode> catePathList = getSubNodesByXPath(root, CATE_PATH, new ContentParseException("cate path not found for [" + sku.getId() + "]"));

        int cateSize = catePathList.size() - 2;
        cateSize = 5 > cateSize ? cateSize : 5;
        long parentId = 0;

        for (int i = 0; i < cateSize + 1; i++) {

            if (i == 0) {
                continue;
            }

            try {

                TagNode pathNode = getSubNodeByXPath(catePathList.get(i), "/a", new ContentParseException("path not found"));

                String pathString = StringUtils.filterAndTrim(pathNode.getText().toString(), null);

                PtmCategory2 category = dbm.querySingle(Q_CATEGORY_BYNAME, Arrays.asList(pathString));

                if (category != null) {
                    parentId = category.getId();
                } else {
                    category = new PtmCategory2();

                    category.setName(pathString);
                    category.setLevel(i);
                    category.setParentId(parentId);

                    category = categoryService.createAppCategory(category);
                    parentId = category.getId();
                }

            } catch (ContentParseException exception) {
                break;
            }

        }

        sku.setCategoryId(parentId);

        List<TagNode> infoNodeList = getSubNodesByXPath(root, DESCRIPTION_INFO, new ContentParseException("description section not found for [" + sku.getId() + "]"));
        Map<String, String> infoMap = new HashMap<String, String>();

        for (TagNode node : infoNodeList) {

            getInfo(node, infoMap);//ex    "name1":"value1"

        }

        String jsonDescription = JSONUtil.toJSON(infoMap);

        PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

        updater.getPo().setJsonDescription(jsonDescription);

        dbm.update(updater);
        logger.debug("udpate success for [" + sku.getId() + "]");
    }

    private void getInfo(TagNode node, Map<String, String> infoMap) throws ContentParseException {

        List<TagNode> infoNodeList = getSubNodesByXPath(node, "//tbody/tr", new ContentParseException("description not found"));

        for (int i = 1; i < infoNodeList.size(); i++) {

            TagNode tagNode = infoNodeList.get(i);

            List<TagNode> paramNodeList = getSubNodesByXPath(tagNode, "//td", new ContentParseException("info not found"));

            if (paramNodeList.size() != 2) {
                logger.debug("parse error");
                return;
            }

            String name = paramNodeList.get(0).getText().toString();
            String value = StringUtils.filterAndTrim(paramNodeList.get(1).getText().toString(), null);

            infoMap.put(name, value);
        }
    }
}
