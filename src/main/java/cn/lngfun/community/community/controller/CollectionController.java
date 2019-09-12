package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CollectionController {
    @Autowired
    private CollectionService collectionService;

    /**
     * 通过id收藏问题
     *
     * @param questionId
     * @param request
     * @return
     */
    @PostMapping("/collectQuestion")
    @ResponseBody
    public Object collectQuestion(@RequestParam(name = "id") Long questionId,
                                  @RequestParam(name = "type") String type,
                                  HttpServletRequest request) {
        //判断是否登录
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        if ("collect".equals(type)) {
            //收藏
            return collectionService.collectQuestion(questionId, user.getId());
        } else if ("uncollect".equals(type)) {
            //取消收藏
            return collectionService.unCollectQuestion(questionId, user.getId());
        } else {
            //错误的路径
            return ResultDTO.errorOf(CustomizeErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
