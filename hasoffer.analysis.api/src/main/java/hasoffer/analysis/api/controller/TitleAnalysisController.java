package hasoffer.analysis.api.controller;

import hasoffer.core.analysis.LingHelper;
import hasoffer.core.bo.match.TagMatchResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by chevy on 2016/7/2.
 */
@Controller
@RequestMapping(value = "/analysis")
public class TitleAnalysisController {

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
}
