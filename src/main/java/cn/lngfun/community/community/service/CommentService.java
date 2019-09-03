package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.CommentDTO;
import cn.lngfun.community.community.dto.LikeDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.CommentTypeEnum;
import cn.lngfun.community.community.enums.LikeTypeAndOptionEnum;
import cn.lngfun.community.community.enums.NotificationStatusEnum;
import cn.lngfun.community.community.enums.NotificationTypeEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.mapper.*;
import cn.lngfun.community.community.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Transactional
    public void insert(Comment comment, User commentator) {
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }

        if (comment.getType() == null || !CommentTypeEnum.isExist(comment.getType())) {
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }

        if (comment.getType() == CommentTypeEnum.COMMENT.getType()) {
            //回复评论
            Comment dbComment = commentMapper.findById(comment.getParentId());
            if (dbComment == null) {
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            //查找到对应的问题
            Question question = questionMapper.findById(dbComment.getParentId());
            if (question == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            //插入评论
            commentMapper.insert(comment);
            //评论数加一
            commentMapper.incCommentCount(dbComment.getId());
            //添加通知
            createNotify(comment, dbComment.getCommentator(), commentator.getName(), question.getTitle(), NotificationTypeEnum.REPLY_COMMENT, question.getId());
        } else {
            //回复问题
            Question question = questionMapper.findById(comment.getParentId());
            if (question == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            //插入评论
            commentMapper.insert(comment);
            //评论数加一
            questionMapper.incCommentCount(question.getId());
            //添加通知
            createNotify(comment, question.getCreator(), commentator.getName(), question.getTitle(), NotificationTypeEnum.REPLY_QUESTION, question.getId());
        }
    }

    public void createNotify(Comment comment, Long receiver, String notifierName, String outerTitle, NotificationTypeEnum notificationType, Long outerId) {
        //自己回复自己的问题或评论时不需要通知
        if (receiver.equals(comment.getCommentator())) {
            return;
        }

        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setNotifier(comment.getCommentator());
        notification.setOuterId(outerId);
        notification.setReceiver(receiver);
        notification.setType(notificationType.getType());
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notification.setNotifierName(notifierName);
        notification.setOuterTitle(outerTitle);
        notificationMapper.insert(notification);
    }

    public List<CommentDTO> listByTargetId(Long id, CommentTypeEnum type, User user) {
        List<Comment> comments = commentMapper.findByParentId(id, type.getType());

        if (comments.size() == 0) {
            return new ArrayList<>();
        }


        //获取去重评论人
        Set<Long> commentators = comments.stream().map(comment -> comment.getCommentator()).collect(Collectors.toSet());

        //获取评论人信息并转换成 Map
        List<User> users = new ArrayList<>();
        for (Long commentator : commentators) {
            User dbUser = userMapper.findById(commentator);
            users.add(dbUser);
        }

        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(uid -> uid.getId(), mUser -> mUser));

        //转换comment为commentDTO
        List<CommentDTO> commentDTOList = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            commentDTO.setUser(userMap.get(comment.getCommentator()));
            if (user != null) {
                List<Like> likes = likeMapper.findByParentId(comment.getId());
                for (Like like : likes) {
                    if (like.getUserId().equals(user.getId())) {
                        commentDTO.setLiked(true);
                        break;
                    }
                }
            }
            return commentDTO;
        }).collect(Collectors.toList());

        return commentDTOList;
    }

    @Transactional
    public void deleteCommentByParentId(Long questionId) {
        //找出这个问题的所有评论
        List<Comment> commentsFromQuestion = commentMapper.findByParentId(questionId, CommentTypeEnum.QUESTION.getType());
        //找出这些评论的子评论并删除
        for (Comment parentComment : commentsFromQuestion) {
            List<Comment> commentsFromComment = commentMapper.findByParentId(parentComment.getId(), CommentTypeEnum.COMMENT.getType());
            for (Comment childComment: commentsFromComment) {
                commentMapper.deleteCommentById(childComment.getId());
            }
            //删完子评论删父评论
            commentMapper.deleteCommentById(parentComment.getId());
            //删除点赞记录
            likeMapper.deleteByParentId(parentComment.getId());
            /*//问题回复量减一
            questionMapper.decCommentCount(questionId);*/
        }
    }

    @Transactional
    public Object likeOrDislike(LikeDTO likeDTO, Long userId) {
        if (commentMapper.findById(likeDTO.getParentId()) == null) {
            //要点赞的评论不存在
            return ResultDTO.errorOf(CustomizeErrorCode.COMMENT_NOT_FOUND);
        }

        if (likeDTO.getOption().equals(LikeTypeAndOptionEnum.LIKE.getType())) {
            //点赞
            commentMapper.likeComment(likeDTO.getParentId());
            Like like = new Like();
            like.setParentId(likeDTO.getParentId());
            like.setType(likeDTO.getType());
            like.setUserId(userId);
            like.setGmtCreate(System.currentTimeMillis());
            likeMapper.like(like);
        } else {
            //取消点赞
            commentMapper.dislikeComment(likeDTO.getParentId());
            likeMapper.dislike(userId, likeDTO.getParentId());
        }

        return ResultDTO.okOf();
    }
}