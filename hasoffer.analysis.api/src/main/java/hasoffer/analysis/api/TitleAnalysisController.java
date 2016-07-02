package hasoffer.analysis.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chevy on 2016/7/2.
 */
@Controller
@RequestMapping(value = "/analysis")
public class TitleAnalysisController {

    @RequestMapping(value = "/title", method = RequestMethod.POST)
    public ModelAndView analysisTitle(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();

        String title = request.getParameter("title");


        return mav;
    }
}
