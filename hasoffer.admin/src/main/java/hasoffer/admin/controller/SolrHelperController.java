package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by hs on 2016年11月28日.
 * Time 09:38
 */
@Controller
@RequestMapping("solr")
public class SolrHelperController {
    @Resource
    IPtmStdSkuService iPtmStdSkuService;
    @Resource
    IPtmStdPriceService ptmStdPriceService;
    Logger logger = LoggerFactory.getLogger(SolrHelperController.class);

    @ResponseBody
    @RequestMapping("importNewStructData2SolrByMinId")
    public String importNewStructData2Solr(final Long minId) {
        //按照最小id以上查询sku表
        ListProcessTask<PtmStdSku> processTask = new ListProcessTask<>(new ILister() {
            @Override
            public PageableResult getData(int page) {
                return iPtmStdSkuService.getPtmStdSkuListByMinId(minId, page, 200);
            }

            @Override
            public boolean isRunForever() {
                return false;
            }

            @Override
            public void setRunForever(boolean runForever) {

            }
        }, new IProcessor<PtmStdSku>() {
            @Override
            public void process(PtmStdSku o) {
                try {
                    iPtmStdSkuService.importPtmStdSku2Solr(o);
                } catch (Exception e) {
                    logger.error("ERROR OCCUR  , id is {}", o.getId() + "\t" + e.getMessage());
                }
            }
        });
        processTask.setProcessorCount(10);
        processTask.setQueueMaxSize(200);
        processTask.go();
        return "ok";
    }

    @ResponseBody
    @RequestMapping("importStdSku2SolrBySkuId")
    public String importNewStructData2SolrT(final Long stdSkuId) {
        if (stdSkuId > 0) {
            PtmStdSku ptmStdSku = iPtmStdSkuService.getStdSkuById(stdSkuId);
            if (ptmStdSku != null) {
                iPtmStdSkuService.importPtmStdSku2Solr(ptmStdSku);
            }

        }
        return "ok";
    }

    @ResponseBody
    @RequestMapping("importstdprice2solrbyminid")
    public String importSkuPrice2Solr(@RequestParam(defaultValue = "0") final Long minId) {

        ListProcessTask<PtmStdPrice> processTask = new ListProcessTask<>(new ILister() {
            @Override
            public PageableResult getData(int page) {
                return ptmStdPriceService.getPagedPtmStdPriceByMinId(minId, page, 500);
            }

            @Override
            public boolean isRunForever() {
                return false;
            }

            @Override
            public void setRunForever(boolean runForever) {

            }
        }, new IProcessor<PtmStdPrice>() {
            @Override
            public void process(PtmStdPrice o) {
                try {
                    ptmStdPriceService.importPtmStdPrice2Solr(o);
                } catch (Exception e) {
                    logger.error("ERROR OCCUR  , id is {}", o.getId() + "\t" + e.getMessage());
                }
            }
        });
        processTask.setProcessorCount(10);
        processTask.setQueueMaxSize(200);
        processTask.go();
        return "ok";
    }
}
