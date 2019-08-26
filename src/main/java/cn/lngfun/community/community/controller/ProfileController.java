package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.model.Notification;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.NotificationService;
import cn.lngfun.community.community.service.QuestionService;
import cn.lngfun.community.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{action}")
    public String profile(@PathVariable(name = "action") String action, Model model, HttpServletRequest request,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {

        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }

        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");

            PagingDTO pagingDTO = questionService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        } else if ("replies".equals(action)) {
            model.addAttribute("section", "replies");
            model.addAttribute("sectionName", "最新回复");

            PagingDTO pagingDTO = notificationService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        } else if ("information".equals(action)) {
            model.addAttribute("section", "information");
            model.addAttribute("sectionName", "我的资料");
        } else if ("setting".equals(action)) {
            model.addAttribute("section", "setting");
            model.addAttribute("sectionName", "设置");
        }

        return "profile";
    }

    @PostMapping("/editProfile")
    public String eidtProfile(Model model, HttpServletRequest request,
                              @RequestParam(name = "name") String name,
                              @RequestParam(name = "bio") String bio,
                              @RequestParam(name = "company") String company,
                              @RequestParam(name = "email") String email,
                              @RequestParam(name = "blog") String blog,
                              @RequestParam(name = "location") String location) {
        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }

        user.setName(name);
        user.setBio(bio);
        user.setCompany(company);
        user.setEmail(email);
        user.setBlog(blog);
        user.setLocation(location);
        user.setGmtModified(System.currentTimeMillis());
        userService.updateProfile(user);

        model.addAttribute("section", "information");
        model.addAttribute("sectionName", "我的资料");

        return "profile";
    }

    @GetMapping("/people")
    public String people(@RequestParam(name = "page", defaultValue = "1") Integer page,
                         @RequestParam(name = "size", defaultValue = "10") Integer size,
                         @RequestParam(name = "id") Long id,
                         Model model,
                         HttpServletRequest request) {

        User user = userService.findUserById(id);
        //找不到的用户
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.USER_NOT_FOUND);
        }
        //判断点击的人是否为登录后的自己
        if (user.equals(request.getSession().getAttribute("user"))) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");

            PagingDTO pagingDTO = questionService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);

            return "profile";
        }

        PagingDTO questions = questionService.list(id, page, size);
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        model.addAttribute("pagingDTO", questions);

        return "people";
    }

}
