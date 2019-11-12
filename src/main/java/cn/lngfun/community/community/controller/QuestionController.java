package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.CommentDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.CommentTypeEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.provider.Tools;
import cn.lngfun.community.community.service.CommentService;
import cn.lngfun.community.community.service.FollowService;
import cn.lngfun.community.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FollowService followService;

    /**
     * 通过id访问问题
     *
     * @param id
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/{id}")
    public String question(@PathVariable(name = "id") Long id, Model model, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        QuestionDTO questionDTO = questionService.findById(id, currentUser);
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(id, CommentTypeEnum.QUESTION, (User) request.getSession().getAttribute("user"));
        //判断是否为同一ip，是的话就不增加浏览量
        String ip = Tools.getIpAddress(request);
        if (!ip.equals(request.getSession().getAttribute("question-" + id + "-isViewed"))) {
            //浏览量加1
            questionService.incView(id);
            request.getSession().setAttribute("question-" + id + "-isViewed", ip);
        }
        //如果用户已登录，判断现在登录的用户是否关注了问题发布者
        if (currentUser != null && followService.isFollowed(questionDTO.getCreator(), currentUser.getId()) != null) {
            model.addAttribute("isFollowed", true);
        }

        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);

        return "question";
    }

    /**
     * 通过id删除问题
     *
     * @param questionId
     * @param request
     * @return
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public Object deleteQuestion(@PathVariable(name = "id") Long questionId, HttpServletRequest request) {
        //判断是否登录
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        return questionService.deleteQuestion(questionId, user.getId());
    }

}
