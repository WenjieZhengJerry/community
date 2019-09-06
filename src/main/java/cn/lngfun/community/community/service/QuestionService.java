package cn.lngfun.community.community.service;

import cn.lngfun.community.community.cache.CategoryCache;
import cn.lngfun.community.community.dto.LikeDTO;
import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.enums.LikeTypeAndOptionEnum;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.mapper.CommentMapper;
import cn.lngfun.community.community.mapper.LikeMapper;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.Like;
import cn.lngfun.community.community.model.Question;
import cn.lngfun.community.community.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeMapper likeMapper;


    private List<QuestionDTO> selectQuestions(PagingDTO<QuestionDTO> pagingDTO, Integer page, Integer size, Long userId, String search, String tag, Integer categoryType) {
        //计算从第几页开始
        Integer offset = pagingDTO.calculateOffset(page, size);
        //获取这一页的所有问题
        List<Question> questions;
        if (StringUtils.isNotBlank(search)) {
            questions = questionMapper.listBySearch(offset, size, search);
        } else {
            //无搜索条件
            if (StringUtils.isNotBlank(tag)) {
                //有标签条件
                questions = questionMapper.listByTag(offset, size, tag);
            } else if(categoryType != null) {
                //有分类条件
                if (categoryType == 0) {
                    questions = userId != null ? questionMapper.listByUserId(userId, offset, size) : questionMapper.list(offset, size);
                } else {
                    questions = questionMapper.listByCategory(offset, size, categoryType);
                }
            } else {
                //正常无条件
                questions = userId != null ? questionMapper.listByUserId(userId, offset, size) : questionMapper.list(offset, size);
            }
        }

        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setCategoryName(CategoryCache.getCategoryName(questionDTO.getCategoryType()));
            questionDTO.setCategoryColor(CategoryCache.getCategoryColor(questionDTO.getCategoryType()));
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        return questionDTOList;
    }

    /**
     * 列出所有问题列表
     *
     * @param page
     * @param size
     * @return
     */
    public PagingDTO list(Integer page, Integer size, String search, String tag, Integer categoryType) {
        PagingDTO<QuestionDTO> pagingDTO = new PagingDTO<>();
        //计算问题总数
        if (StringUtils.isNotBlank(search)) {
            //有搜索条件
            pagingDTO.setTotalCount(questionMapper.countBySearch(search));
        } else {
            //无搜索条件
            if (StringUtils.isNotBlank(tag)) {
                //有标签条件
                pagingDTO.setTotalCount(questionMapper.countByTag(tag));
            } else if (categoryType != null) {
                //有分类条件
                if (categoryType == 0) {
                    //按全部计数
                    pagingDTO.setTotalCount(questionMapper.count());
                } else {
                    //按分类计数
                    pagingDTO.setTotalCount(questionMapper.countByCategory(categoryType));
                }
            } else {
                //正常无条件
                pagingDTO.setTotalCount(questionMapper.count());
            }
        }

        pagingDTO.setData(selectQuestions(pagingDTO, page, size, null, search, tag, categoryType));//装填页面数据

        return pagingDTO;
    }

    /**
     * 通过userId列出问题列表
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public PagingDTO list(Long userId, Integer page, Integer size) {
        PagingDTO<QuestionDTO> pagingDTO = new PagingDTO<>();
        pagingDTO.setTotalCount(questionMapper.countByUserId(userId));

        pagingDTO.setData(selectQuestions(pagingDTO, page, size, userId, null, null, null));//装填页面数据

        return pagingDTO;
    }

    /**
     * 通过问题id查找问题
     *
     * @param id
     * @param user
     * @return
     */
    public QuestionDTO findById(Long id, User user) {
        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        questionDTO.setCategoryName(CategoryCache.getCategoryName(questionDTO.getCategoryType()));
        questionDTO.setCategoryColor(CategoryCache.getCategoryColor(questionDTO.getCategoryType()));
        User dbUser = userMapper.findById(question.getCreator());
        questionDTO.setUser(dbUser);
        if (user != null) {
            List<Like> likes = likeMapper.findByParentId(question.getId());
            for (Like like : likes) {
                if (like.getUserId().equals(user.getId())) {
                    questionDTO.setLiked(true);
                    break;
                }
            }
        }

        return questionDTO;
    }

    /**
     * 新建提问
     *
     * @param question
     */
    public void createOrUpdate(Question question) {
        if (question.getId() == null) {
            //创建
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            questionMapper.insert(question);
        } else {
            //更新
            question.setGmtModified(System.currentTimeMillis());
            int updated = questionMapper.update(question);
            if (updated != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    /**
     * 查询相关问题
     *
     * @param questionQueryDTO
     * @return
     */
    public List<QuestionDTO> selectRelated(QuestionDTO questionQueryDTO) {
        if (StringUtils.isBlank(questionQueryDTO.getTag())) {
            return new ArrayList<>();
        }

        String[] tags = StringUtils.split(questionQueryDTO.getTag(), ",");
        String regexpTags = Arrays.stream(tags).collect(Collectors.joining("|"));

        List<Question> questions = questionMapper.selectRelated(questionQueryDTO.getId(), regexpTags);
        List<QuestionDTO> questionDTOList = questions.stream().map(q -> {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());

        return questionDTOList;
    }

    /**
     * 浏览数加一
     *
     * @param id
     */
    public void incView(Long id) {
        questionMapper.incView(id);
    }

    /**
     * 查询热门话题
     *
     * @return
     */
    public List<Question> selectHotIssue() {
        return questionMapper.selectHotIssue();
    }

    /**
     * 删除提问
     *
     * @param questionId
     * @param userId
     * @return
     */
    @Transactional
    public Object deleteQuestion(Long questionId, Long userId) {
        Question question = questionMapper.findById(questionId);

        if (question == null) {
            //问题没有找到
            return ResultDTO.errorOf(CustomizeErrorCode.QUESTION_NOT_FOUND);
        } else if (!userId.equals(question.getCreator())) {
            //判断该问题的发布者是否和当前用户一致，防止有人通过问题id直接删除别人的问题
            return ResultDTO.errorOf(CustomizeErrorCode.DELETE_QUESTION_FAIL);
        } else {
            //删除成功
            questionMapper.deleteQuestion(questionId);
            likeMapper.deleteByParentId(questionId);
            commentService.deleteCommentByParentId(questionId);
            return ResultDTO.okOf();
        }
    }

    /**
     * 点赞或取消点赞
     *
     * @param likeDTO
     * @param userId
     * @return
     */
    @Transactional
    public Object likeOrDislike(LikeDTO likeDTO, Long userId) {
        if (questionMapper.findById(likeDTO.getParentId()) == null) {
            //要点赞的问题不存在
            return ResultDTO.errorOf(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }

        if (likeDTO.getOption().equals(LikeTypeAndOptionEnum.LIKE.getType())) {
            //点赞
            questionMapper.likeComment(likeDTO.getParentId());
            Like like = new Like();
            like.setParentId(likeDTO.getParentId());
            like.setType(likeDTO.getType());
            like.setUserId(userId);
            like.setGmtCreate(System.currentTimeMillis());
            likeMapper.like(like);
        } else {
            //取消点赞
            questionMapper.dislikeComment(likeDTO.getParentId());
            likeMapper.dislike(userId, likeDTO.getParentId());
        }

        return ResultDTO.okOf();
    }
}
