package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.CommentDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.CommentTypeEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.CommentService;
import cn.lngfun.community.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question (@PathVariable(name = "id") Long id, Model model) {
        QuestionDTO questionDTO = questionService.findById(id);
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(id, CommentTypeEnum.QUESTION);
        //浏览量加1
        questionService.incView(id);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);

        return "question";
    }

    @PostMapping("/deleteQuestion")
    @ResponseBody
    public Object deleteQuestion (@RequestParam(name = "id") Long questionId, HttpServletRequest request) {
        //判断是否登录
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        Integer resultCode = questionService.deleteQuestion(questionId, user.getId());
        if (resultCode == CustomizeErrorCode.QUESTION_NOT_FOUND.getCode()) {
            //如果没有这个问题则无法删除
            return ResultDTO.errorOf(CustomizeErrorCode.QUESTION_NOT_FOUND);
        } else if (resultCode == CustomizeErrorCode.DELETE_QUESTION_FAIL.getCode()) {
            //如果返回false则是当前用户删除的不是自己的问题
            return ResultDTO.errorOf(CustomizeErrorCode.DELETE_QUESTION_FAIL);
        } else {
            //删除成功
            return ResultDTO.okOf();
        }
    }

}
