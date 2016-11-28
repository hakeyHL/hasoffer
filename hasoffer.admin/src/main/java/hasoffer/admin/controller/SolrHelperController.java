package hasoffer.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by hs on 2016年11月28日.
 * Time 09:38
 */
@Controller
@RequestMapping("solr")
public class SolrHelperController {
    @ResponseBody
    @RequestMapping("importNewStructData2SolrByMinId")
    public String importNewStructData2Solr(Long minId) {
        //按照最小id以上查询sku表
        return "ok";
    }
}
