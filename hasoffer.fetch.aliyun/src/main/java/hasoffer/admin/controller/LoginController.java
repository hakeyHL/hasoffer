package hasoffer.admin.controller;

import hasoffer.admin.po.SysAdmin;
import hasoffer.admin.service.IAdminService;
import hasoffer.common.context.Context;
import hasoffer.common.context.StaticContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

    @Resource
    IAdminService adminService;

    @RequestMapping(value = "/prelogin", method = RequestMethod.GET)
    public String prelogin(HttpServletRequest request) {
        return "system/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SysAdmin admin = (SysAdmin) Context.currentContext().get(StaticContext.USER);

        if (admin != null) {
            adminService.logout(admin);
        }

        return "system/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();

        String uname = request.getParameter("name");
        String password = request.getParameter("password");

        if (StringUtils.isEmpty(uname) || StringUtils.isEmpty(password)) {
            mav.setViewName("system/login");
            mav.addObject("error", "用户名/密码不能为空");
            return mav;
        }

        try {
            SysAdmin admin = adminService.login(uname, password);

            response.addCookie(new Cookie(StaticContext.USER_KEY, admin.getUkey()));
            //logger.debug(uname + " login success.");

            mav.setViewName("redirect:/layout/showIndex?userName=" + uname);
            return mav;
        } catch (LoginException e) {
            mav.setViewName("system/login");
            mav.addObject("error", e.getMessage());
            return mav;
        }
    }
}
