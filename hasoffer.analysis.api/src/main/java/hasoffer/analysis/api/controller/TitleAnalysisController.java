package hasoffer.analysis.api.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.analysis.ITagService;
import hasoffer.core.analysis.LingHelper;
import hasoffer.core.bo.match.TagMatchResult;
import hasoffer.core.bo.match.TagType;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.match.TagMatched;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by chevy on 2016/7/2.
 */
@Controller
@RequestMapping(value = "/analysis")
public class TitleAnalysisController {

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ITagService tagService;

    private Logger logger = LoggerFactory.getLogger(TitleAnalysisController.class);

    @RequestMapping(value = "/t", method = RequestMethod.POST)
    public ModelAndView analysisTitle(HttpServletRequest request) {

        String title = request.getParameter("title");

//        Map<String, List<String>> tagMap = LingHelper.analysis(title);

        Map<String, List<TagMatchResult>> tagMap = LingHelper.analysis2(title);

        ModelAndView mav = new ModelAndView();
        mav.addObject("tagMap", tagMap);
        mav.addObject("title", title);

        return mav;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public
    @ResponseBody
    String matchSkuTitles() {

        ListAndProcessTask2<PtmCmpSku> listAndProcessTask2 = new ListAndProcessTask2<PtmCmpSku>(new IList() {
            @Override
            public PageableResult getData(int page) {
                return dbm.queryPage("select t from PtmCmpSku t where t.title is not null", page, 2000);
//                return dbm.queryPage("select t from PtmProduct t where t.title is not null", page, 2000);
            }

            @Override
            public boolean isRunForever() {
                return false;
            }

            @Override
            public void setRunForever(boolean runForever) {

            }
        }, new IProcess<PtmCmpSku>() {
            @Override
            public void process(PtmCmpSku o) {
                String title = o.getTitle();
                if (StringUtils.isEmpty(title)) {
                    return;
                }

                Map<String, List<String>> tagMap = LingHelper.analysis(title);

                List<String> brands = tagMap.get(TagType.BRAND.name());
                String brandStr = StringUtils.arrayToString(brands);

                List<String> models = tagMap.get(TagType.MODEL.name());
                String modelStr = StringUtils.arrayToString(models);

                TagMatched tm = new TagMatched(o.getId(), title, brandStr, modelStr);
                tagService.saveTagMatched(tm);
            }
        });

        listAndProcessTask2.go();

        return "ok";
    }
}
