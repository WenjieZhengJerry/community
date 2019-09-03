package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.LikeDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.LikeTypeAndOptionEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.CommentService;
import cn.lngfun.community.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class likeController {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @PostMapping("/likeOrDislike")
    @ResponseBody
    public Object likeOrDislike (@RequestBody LikeDTO likeDTO,
                                 HttpServletRequest request) {
        //登录判断
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }
        //判断点赞类型
        if (likeDTO.getType().equals(LikeTypeAndOptionEnum.QUESTION.getType())) {
            return questionService.likeOrDislike(likeDTO, user.getId());
        } else {
            return commentService.likeOrDislike(likeDTO, user.getId());
        }
    }
}
