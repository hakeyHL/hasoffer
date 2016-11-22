package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import hasoffer.core.product.solr.ProductModel2;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/5/17
 * Function :
 */
@Controller
@RequestMapping(value = "/feedback")
public class FeedbackController {
    //    private static final HttpSolrServer httpSolrServer = new HttpSolrServer("http://127.0.0.1:8983/solr/hasofferproduct2");
//    private static final HttpSolrServer httpSolrServer = new HttpSolrServer("http://52.220.120.233:8983/solr/hasofferproduct2");
    private static final HttpSolrServer httpSolrServer = new HttpSolrServer("http://54.169.176.10:8983/solr/hasofferproduct2");
    private Logger logger = LoggerFactory.logger(FeedbackController.class);

    public static void main(String[] args) throws SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set(CommonParams.Q, "iphone");
        solrQuery.set(CommonParams.FL, "id", "title", "searchCount", "storeCount", "rating", "review", "minPrice", "maxPrice");
        solrQuery.set("qf", "title^2");
        solrQuery.setStart(0);
        solrQuery.setRows(20);
        solrQuery.set("defType", "edismax");
        solrQuery.set("bf", "sum(searchCount,sum(storeCount,review))");
        QueryResponse query = httpSolrServer.query(solrQuery);
        resultShow(query);
    }

    /**
     * @param response
     * @return void
     * @throws
     * @描述：查询结果显示类
     */
    private static void resultShow(QueryResponse response) {

        int time = response.getQTime();
        System.out.println("响应时间:" + time + "ms");

        SolrDocumentList results = response.getResults();
        long numFound = results.getNumFound();
        System.out.println("总数量:" + numFound);
        List<ProductModel2> convert = convert(results);
        System.out.println("out : " + JSON.toJSONString(convert, true));
        /*for (SolrDocument doc : results) {
            System.out.println("id:" + doc.getFieldValue("id").toString());
            System.out.println("title:" + doc.getFieldValue("title").toString());
            System.out.println("searchCount:" + doc.getFieldValue("searchCount").toString());
            System.out.println("storeCount:" + doc.getFieldValue("storeCount").toString());
            System.out.println("rating:" + doc.getFieldValue("rating").toString());
            System.out.println("review:" + doc.getFieldValue("review").toString());
            System.out.println("minPrice:" + doc.getFieldValue("minPrice").toString());
            System.out.println("maxPrice:" + doc.getFieldValue("maxPrice").toString());
            System.out.println();
        }*/
    }

    private static List<ProductModel2> convert(SolrDocumentList results) {
        List<ProductModel2> list = new ArrayList<ProductModel2>();
        Iterator<SolrDocument> iter = results.iterator();
        while (iter.hasNext()) {
            SolrDocument sd = iter.next();
            try {
                ProductModel2 productModel2 = new ProductModel2();
                for (Map.Entry<String, Object> kv : sd.entrySet()) {
                    BeanUtils.setProperty(productModel2, kv.getKey(), kv.getValue());
                }
                list.add(productModel2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView feedback(HttpServletRequest request) {
        //type 0. 代表用户卸载反馈
        //content代表反馈内容
        //pos代表具体不爽原因 ,list...
        String type = request.getParameter("type");
        String pos = request.getParameter("pos");
        String content = request.getParameter("content");

        logger.debug(type + "\t" + pos);
        logger.debug(content);

        ModelAndView mav = new ModelAndView();

        mav.addObject("result", "OK");

        return mav;
    }
}
