package hasoffer.timer.msp.worker;

import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.fetch.sites.mysmartprice.MspHelper;
import hasoffer.timer.msp.vo.MspUnCmpModel;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2016/2/19.
 * todo
 *  1.开启抓取线程前需要更新列表页的productCount，该方法还有点问题，需要改进
 *  2.由于网路问题，抓取数据建议一次抓取一个category下的数据，分页请求大小降低到1
 *  3.为了效率提升，避免资源浪费，将存储list<MspUnCmpModel>放到共享队列里面，添加算法要优化，尽量保证将解析完成的MspUnCmpModel第一时间加入消费者队列
 */

public class FetchProductTagNodesWorker implements Runnable {

    private final static String AJAX_PRODUCTS_QUERY = "http://www.mysmartprice.com/fashion/filters/filter_get_revamp?recent=0&q=filter%2F&subcategory=$cateIdentifier&start=$start&rows=$rowCount&page_name=";
    private final static String PRODUCT_SECTION = "//div[@class='grid-item product']";
    private final static String PRODUCT_NAME_PATH = "/a[@class='info']/div[@class='title']";
    public static int aliveThreadCount = 0;
    public static ConcurrentLinkedQueue<MspCategory> mspCategoryQueue;
    /**
     * 将ajax得到的产品MspUnCmpModel集合变成共享队列，已提升效率
     */
//    public static ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue;

    public static ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue;
    private Logger logger = LoggerFactory.getLogger(FetchProductTagNodesWorker.class);
    private List<String> sourceIdList;

    private FetchProductTagNodesWorker() {

    }

    public FetchProductTagNodesWorker(ConcurrentLinkedQueue<MspCategory> categoryListQueue,ConcurrentLinkedQueue<MspUnCmpModel> mspUnCmpModelQueue,List<String> sourceIdList) {
        this.mspCategoryQueue = categoryListQueue;
        this.mspUnCmpModelQueue = mspUnCmpModelQueue;
        this.sourceIdList = sourceIdList;
        aliveThreadCount++;
    }

    @Override
    public void run() {

        while (true) {
            logger.debug( "------------------FETCH MspUnCmpProduct start------------------------");
            MspCategory category = mspCategoryQueue.poll();
            //如果没有元素了就停止当前线程
            if (category == null) {
                FetchProductTagNodesWorker.aliveThreadCount--;
                break;
            }

            try {
                logger.debug( "------------------FETCH MspUnCmpProduct getSecTagNodes ------------------------");
                List<TagNode> secNodes = getSecTagNodes(category);
                logger.debug( "------------------FETCH MspUnCmpProduct getMspUnCmpModelByTagNode ------------------------");
                getMspUnCmpModelByTagNode(category, secNodes,sourceIdList);

                logger.debug( "------------------FETCH MspUnCmpProduct add success------------------------");

            } catch (Exception e) {
                logger.debug( "------------------FETCH MspUnCmpProduct fail------------------------");
                logger.debug(e.getMessage());
                mspCategoryQueue.add(category);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 去除重复sourceId的MspUnCmpModel
     * @param mspUnCmpModel
     * @param sourceIdList
     * @return  返回true包含，返回false不包含，如果元素为null，返回null
     */
    private Boolean removeRepeatSourceId(MspUnCmpModel mspUnCmpModel,List<String> sourceIdList) {

        if(mspUnCmpModel!=null){
            if(sourceIdList.contains(mspUnCmpModel.getSourceId())){
                return true;
            }else{
                return false;
            }
        }

        return null;
    }

    /**
     *
     * @param category
     * @param secNodes
     * @param sourceIdList
     * @throws XPatherException
     */
    private void getMspUnCmpModelByTagNode(MspCategory category, List<TagNode> secNodes,List<String> sourceIdList) throws XPatherException {

        List<MspUnCmpModel> mspUnCmpModelList = new ArrayList<MspUnCmpModel>();

        if (secNodes != null && secNodes.size() > 0) {
            for (TagNode node : secNodes) {
                String offerUrl = "";
                TagNode nameNode = HtmlUtils.getFirstNodeByXPath(node, PRODUCT_NAME_PATH);
                if (nameNode == null) {
                    System.out.println("name node null");
                } else {
                    offerUrl = nameNode.getParent().getAttributeByName("href");
                    if (!offerUrl.contains("//")) {
                        offerUrl = "http://www.mysmartprice.com/" + offerUrl;
                    }
                }
                String sourceId = MspHelper.getProductIdByUrl(offerUrl);

                MspUnCmpModel mspUnCmpModel = new MspUnCmpModel();
                mspUnCmpModel.setCategoryId(category.getId());
                mspUnCmpModel.setSourceId(sourceId);
                mspUnCmpModel.setTagNode(node);

                if(!removeRepeatSourceId(mspUnCmpModel,sourceIdList)){
                    mspUnCmpModelQueue.add(mspUnCmpModel);
                }
            }
        }
    }

    /**
     * ajax获取列表页的list<TagNode>
     * @param category
     * @return
     * @throws HttpFetchException
     * @throws XPatherException
     */
    private List<TagNode> getSecTagNodes(MspCategory category) throws HttpFetchException, XPatherException {

        List<TagNode> tagNodeList =new ArrayList<TagNode>();

        String[] subStrs = category.getUrl().trim().split("/");
        String cateIdentify = subStrs[subStrs.length - 1];

        int productCount = category.getProCount();
        int pageCount = (productCount+4)/5;
        int start  = 0;
        int end = 0;

        //由于分页大小限制，选择500个商品一个分页
        for(int i=0;i<pageCount;i++){

            start = i*5;
            end = (i+1)*5;
            if(i==pageCount-1){
                end = productCount;
            }

            String url = AJAX_PRODUCTS_QUERY.replace("$cateIdentifier", cateIdentify).replace("$start", start + "").replace("$rowCount", end + "");
            TagNode root = HtmlUtils.getUrlRootTagNode(url);
            List<TagNode> subNodesByXPath = HtmlUtils.getSubNodesByXPath(root, PRODUCT_SECTION);

            tagNodeList.addAll(subNodesByXPath);
        }

        return tagNodeList;
    }

}
