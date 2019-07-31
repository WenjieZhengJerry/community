package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.model.Notification;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.NotificationService;
import cn.lngfun.community.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/profile/{action}")
    public String profile(@PathVariable(name = "action") String action, Model model, HttpServletRequest request,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "5") Integer size) {

        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if(user == null) {
            return "redirect:/";
        }

        if("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的问题");

            PagingDTO pagingDTO = questionService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        }else if("replies".equals(action)) {
            model.addAttribute("section", "replies");
            model.addAttribute("sectionName", "最新回复");

            PagingDTO pagingDTO = notificationService.list(user.getId(), page, size);
            model.addAttribute("pagingDTO", pagingDTO);
        }

        return "profile";
    }

}
