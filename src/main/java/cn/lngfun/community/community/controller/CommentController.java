package cn.lngfun.community.community.controller;

import cn.lngfun.community.community.dto.CommentCreateDTO;
import cn.lngfun.community.community.dto.CommentDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.CommentTypeEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.model.Comment;
import cn.lngfun.community.community.model.User;
import cn.lngfun.community.community.service.CommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/comment")
    @ResponseBody
    public Object post(@RequestBody CommentCreateDTO commentCreateDTO,
                       HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        if(commentCreateDTO == null || StringUtils.isBlank(commentCreateDTO.getContent())) {
            return ResultDTO.errorOf(CustomizeErrorCode.CONTENT_IS_EMPTY);
        }

        Comment comment = new Comment();
        comment.setParentId(commentCreateDTO.getParentId());
        comment.setContent(commentCreateDTO.getContent());
        comment.setType(commentCreateDTO.getType());
        comment.setGmtCreate(System.currentTimeMillis());
        comment.setGmtModified(comment.getGmtCreate());
        comment.setCommentator(user.getId());
        comment.setLikeCount(0L);
        comment.setCommentCount(0);
        commentService.insert(comment);

        return ResultDTO.okOf();
    }

    @GetMapping("/comment/{id}")
    @ResponseBody
    public ResultDTO<List> comments (@PathVariable(name = "id") Long id) {
        List<CommentDTO> commentDTOList = commentService.listByTargetId(id, CommentTypeEnum.COMMENT);
        return ResultDTO.okOf(commentDTOList);
    }
}
