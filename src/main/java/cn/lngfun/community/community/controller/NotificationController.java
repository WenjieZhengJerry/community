package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.NotificationDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.NotificationTypeEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    /**
     * 跳转至问题
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/{id}")
    public String redirect(@PathVariable(name = "id") Long id,
                           HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        //设为已读
        NotificationDTO notificationDTO = notificationService.read(id, user);

        if (NotificationTypeEnum.REPLY_QUESTION.getType() == notificationDTO.getType() ||
                NotificationTypeEnum.REPLY_COMMENT.getType() == notificationDTO.getType()) {
            return "redirect:/question/" + notificationDTO.getOuterId();
        } else {
            return "redirect:/";
        }
    }

    /**
     * 设为全部已读
     *
     * @param request
     * @return
     */
    @PostMapping
    @ResponseBody
    public Object readAll(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        notificationService.readAll(user.getId());
        return ResultDTO.okOf();
    }

    /**
     * 删除全部已读
     *
     * @param request
     * @return
     */
    @DeleteMapping
    @ResponseBody
    public Object deleteRead(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        notificationService.deleteRead(user.getId());
        return ResultDTO.okOf();
    }
}
