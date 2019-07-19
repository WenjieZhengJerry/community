package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/question/{id}")
    public String question (@PathVariable(name = "id") Long id, Model model) {
        QuestionDTO questionDTO = questionService.findById(id);
        //浏览量加1
        questionService.incView(id);
        model.addAttribute("question", questionDTO);

        return "question";
    }

}
