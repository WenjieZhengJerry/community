package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.model.Question;
import cn.lngfun.community.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "5") Integer size,
                        @RequestParam(name = "search", required = false) String search) {

        PagingDTO pagingDTO = questionService.list(page, size, search);
        List<Question> hotIssues = questionService.selectHotIssue();

        model.addAttribute("pagingDTO", pagingDTO);
        model.addAttribute("hotIssues", hotIssues);
        model.addAttribute("search", search);

        return "index";
    }
}
