package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.ThdFetchTaskVo;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.thd.ThdFetchTask;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.task.ITaskService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/2/24.
 */
@Controller
@RequestMapping(value = "/t")
public class TaskController {

    @Resource
    ITaskService taskService;
    @Resource
    ICategoryService categoryService;

    /**
     * 跳转到添加task页面的方法
     * @return
     */
    @RequestMapping(value="/toCreate",method = RequestMethod.GET)
    public ModelAndView toCreate(){
        ModelAndView modelAndView = new ModelAndView("task/createTask");
        return modelAndView;
    }

    //todo  添加修改task状态的方法
    @RequestMapping(value = "/create",method = RequestMethod.POST)
    public ModelAndView create(ThdFetchTask thdFetchTask,@RequestParam(defaultValue = "0") int category3){
        ModelAndView modelAndView = new ModelAndView("redirect:/t/list");

        String urlTemplate = thdFetchTask.getUrlTemplate();
        String[] subStrs1 = urlTemplate.split("start=");
        urlTemplate = subStrs1[0]+"start=startNum";

        String[] subStrs2 = urlTemplate.split("\\.");
        String webSiteString = subStrs2[1];
        Website website = Enum.valueOf(Website.class, webSiteString.trim().toUpperCase());
        thdFetchTask.setWebsite(website);

        thdFetchTask.setPtmCateId(category3);

        taskService.createTask(thdFetchTask);

        return modelAndView;
    }


    @RequestMapping(value="/list",method = RequestMethod.GET)
    public ModelAndView list(){
        ModelAndView modelAndView = new ModelAndView("task/listTask");

        List<ThdFetchTask> thdFetchTasks  = taskService.listFetchTask();
        List<ThdFetchTaskVo> thdFetchTaskVoList = new ArrayList<ThdFetchTaskVo>();

        if(thdFetchTasks!=null && thdFetchTasks.size()!=0){
            for(ThdFetchTask thdFetchTask:thdFetchTasks){

                PtmCategory category = categoryService.getCategory(thdFetchTask.getPtmCateId());
                ThdFetchTaskVo thdFetchTaskVo = new ThdFetchTaskVo();

                thdFetchTaskVo.setPtmCateId(thdFetchTask.getPtmCateId());
                thdFetchTaskVo.setCategoryName(category.getName());
                thdFetchTaskVo.setCount(thdFetchTask.getSize());
                thdFetchTaskVo.setPriority(thdFetchTask.getPriority());
                thdFetchTaskVo.setTaskStatus(thdFetchTask.getTaskStatus().name());
                thdFetchTaskVo.setWebsite(thdFetchTask.getWebsite().name());

                thdFetchTaskVoList.add(thdFetchTaskVo);
            }
        }

        modelAndView.addObject("thdFetchTaskList",thdFetchTaskVoList);

        return modelAndView;
    }

    @RequestMapping(value="/startTask",method = RequestMethod.GET)
    public ModelAndView startTask(@PathVariable long id){
        ModelAndView modelAndView = new ModelAndView("redirect:/t/list");

        ThdFetchTask thdFetchTask = taskService.findThdFetchTaskById(id);

        //调用fetch，启动抓取

        return modelAndView;
    }

}
