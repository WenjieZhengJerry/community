package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.cache.TagCache;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.model.Question;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PublishController {

    @Autowired
    private QuestionService questionService;

    /**
     * 编辑问题
     *
     * @param id
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/publish/{id}")
    public String edit(@PathVariable(name = "id") Long id, Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        //判断登录状态
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }

        QuestionDTO question = questionService.findById(id, user);
        //判断非法id，只有基础类型能用 == 比较，封装类型统一用equals
        if (!user.getId().equals(question.getCreator())) {
            throw new CustomizeException(CustomizeErrorCode.INVALID_QUESTION_ID);
        }

        model.addAttribute("title", question.getTitle());
        model.addAttribute("description", question.getDescription());
        model.addAttribute("tag", question.getTag());
        model.addAttribute("id", question.getId());
        model.addAttribute("tags", TagCache.get());
        model.addAttribute("categoryType", question.getCategoryType());

        return "publish";
    }

    /**
     * 跳转到发布页面
     *
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/publish")
    public String publish(Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        //判断登录状态
        if (user == null) {
            throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
        }
        model.addAttribute("tags", TagCache.get());
        return "publish";
    }

    /**
     * 发布或编辑后保存问题
     *
     * @param title
     * @param description
     * @param tag
     * @param id
     * @param request
     * @param model
     * @return
     */
    @PostMapping("/publish")
    public String doPublish(@RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "tag", required = false) String tag,
                            @RequestParam(value = "categoryType", required = false) Integer categoryType,
                            @RequestParam(value = "id", required = false) Long id,
                            HttpServletRequest request, Model model) {
        //前端参数校验
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("tag", tag);
        model.addAttribute("tags", TagCache.get());
        model.addAttribute("categoryType", categoryType);

        if (title == null || title == "" || StringUtils.isBlank(title)) {
            model.addAttribute("error", "标题不能为空");
            return "publish";
        }
        if (description == null || description == "" || StringUtils.isBlank(description)) {
            model.addAttribute("error", "问题补充不能为空");
            return "publish";
        }
        if (tag == null || tag == "" || StringUtils.isBlank(tag)) {
            model.addAttribute("error", "标签不能为空");
            return "publish";
        }
        if (StringUtils.split(tag, ",").length > 5) {
            model.addAttribute("error", "标签数量不得大于5个");
            return "publish";
        }
        if (categoryType == null || categoryType < 1 || categoryType > 5) {
            model.addAttribute("error", "分类选择错误");
            return "publish";
        }

        String invalid = TagCache.filterInvalid(tag);
        if (StringUtils.isNoneBlank(invalid)) {
            model.addAttribute("error", "检测到非法标签" + invalid);
            return "publish";
        }

        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            model.addAttribute("error", "用户未登录");
            return "publish";
        }

        Question question = new Question();
        question.setTitle(title);
        question.setDescription(description);
        question.setTag(tag);
        question.setCategoryType(categoryType);
        question.setCreator(user.getId());
        question.setId(id);

        questionService.createOrUpdate(question);

        model.addAttribute("title", null);
        model.addAttribute("description", null);
        model.addAttribute("tag", null);
        model.addAttribute("categoryType", null);
        return "redirect:/";
    }
}
