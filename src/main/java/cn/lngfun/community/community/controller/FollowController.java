package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @PostMapping("/follow")
    @ResponseBody
    public Object follow(@RequestParam(name = "id") Long userId,
                         @RequestParam(name = "type") String type,
                         HttpServletRequest request) {
        //判断登录状态
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        if ("follow".equals(type)) {
            //关注
            return followService.follow(userId, user.getId());
        } else {
            //取消关注
            return followService.unfollow(userId, user.getId());
        }
    }
}
