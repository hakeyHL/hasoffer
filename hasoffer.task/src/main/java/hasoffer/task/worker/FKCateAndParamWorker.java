package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.ptm.PtmCategory2;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
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
        final String DESCRIPTION_INFO = "//div[@class='productSpecs specSection']/table";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        //��ȡ������
        List<TagNode> catePathList = getSubNodesByXPath(root, CATE_PATH, new ContentParseException("cate path not found for [" + sku.getId() + "]"));

        //��ȡ��������catePath�ĳ��ȣ�ȡǰ5λ���߸�С
        int cateSize = catePathList.size();
        cateSize = cateSize > 6 ? 6 : cateSize;
        if (catePathList.size() == cateSize) {
            cateSize = cateSize - 1;
        }

        long parentId = 0;

        for (int i = 0; i < cateSize; i++) {

            if (i == 0) {//�ų������еĵ�һ��home
                continue;
            }

            try {

                TagNode pathNode = getSubNodeByXPath(catePathList.get(i), "/a", new ContentParseException("path not found"));

                //��ȡ��Ŀ����
                String pathString = StringUtils.filterAndTrim(pathNode.getText().toString(), null);

                //�����Ŀ�Ƿ����
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

        //��sku������Ŀ��Ϣ
        sku.setCategoryId(parentId);

        //��ȡ�����ڵ�
        List<TagNode> infoNodeList = getSubNodesByXPath(root, DESCRIPTION_INFO, new ContentParseException("description section not found for [" + sku.getId() + "]"));
        //������װ�����ļ�ֵ��
        Map<String, String> infoMap = new HashMap<String, String>();

        for (TagNode node : infoNodeList) {

            //��ȡ�����������Ϣ����䵽map������
            getInfo(node, infoMap, sku);//ex    "name1":"value1"

        }

        String jsonDescription = JSONUtil.toJSON(infoMap);

        //��������Ϣ�־û���mongodb
        PtmCmpSkuDescription skuDescription = new PtmCmpSkuDescription();
        skuDescription.setId(sku.getId());
        skuDescription.setJsonDescription(jsonDescription);
        mdm.save(skuDescription);
        logger.debug("save description success for [" + sku.getId() + "]");
    }

    private void getInfo(TagNode node, Map<String, String> infoMap, PtmCmpSku sku) throws ContentParseException {

        //���������Ϊ���
        List<TagNode> infoNodeList = getSubNodesByXPath(node, "//tbody/tr", new ContentParseException("description not found for [" + sku.getId() + "]"));

        //��ȡÿ�����������е�����
        for (int i = 1; i < infoNodeList.size(); i++) {

            TagNode tagNode = infoNodeList.get(i);

            List<TagNode> paramNodeList = getSubNodesByXPath(tagNode, "//td", new ContentParseException("info not found for [" + sku.getId() + "]"));

            //������Ǽ�ֵ����ʽ�ģ�����
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