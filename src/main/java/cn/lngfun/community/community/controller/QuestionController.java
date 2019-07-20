package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.CommentDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.service.CommentService;
import cn.lngfun.community.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
        List<CommentDTO> comments = commentService.listByQuestionId(id);
        //浏览量加1
        questionService.incView(id);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);

        return "question";
    }

}
