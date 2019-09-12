package cn.lngfun.community.community.service;

import cn.lngfun.community.community.cache.CategoryCache;
import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.mapper.CollectionMapper;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.Collection;
import cn.lngfun.community.community.model.Question;
import cn.lngfun.community.community.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {
    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 收藏问题
     *
     * @param questionId
     * @param userId
     * @return
     */
    @Transactional
    public Object collectQuestion(Long questionId, Long userId) {
        if (questionMapper.findById(questionId) == null) {
            //问题没有找到
            return ResultDTO.errorOf(CustomizeErrorCode.QUESTION_NOT_FOUND);
        } else if (collectionMapper.isCollected(questionId, userId) != null) {
            //避免重复收藏
            return ResultDTO.errorOf(CustomizeErrorCode.COLLECTION_IS_EXIST);
        } else {
            //收藏该问题
            Collection collection = new Collection();
            collection.setUserId(userId);
            collection.setQuestionId(questionId);
            collection.setGmtCreate(System.currentTimeMillis());
            collectionMapper.collectQuestion(collection);
            //增加该问题的收藏数
            questionMapper.incCollection(questionId);

            return ResultDTO.okOf();
        }
    }

    /**
     * 取消收藏
     *
     * @param questionId
     * @param userId
     * @return
     */
    @Transactional
    public Object unCollectQuestion(Long questionId, Long userId) {
        if (questionMapper.findById(questionId) == null) {
            //问题没有找到
            return ResultDTO.errorOf(CustomizeErrorCode.QUESTION_NOT_FOUND);
        } else {
            //取消收藏
            collectionMapper.unCollectQuestion(questionId, userId);
            questionMapper.decCollection(questionId);
            return ResultDTO.errorOf(CustomizeErrorCode.SUCCESS);
        }
    }

    /**
     * 列出一页收藏
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public PagingDTO list(Long userId, Integer page, Integer size) {
        PagingDTO<QuestionDTO> pagingDTO = new PagingDTO<>();
        pagingDTO.setTotalCount(collectionMapper.countByUserId(userId));
        //计算从第几页开始
        Integer offset = pagingDTO.calculateOffset(page, size);
        List<Collection> collections = collectionMapper.list(userId, offset, size);
        //根据收藏的问题id逐一查找问题并填充到DTO里
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Collection collection : collections) {
            Question question = questionMapper.findById(collection.getQuestionId());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            User user = userMapper.findById(question.getCreator());
            questionDTO.setCategoryName(CategoryCache.getCategoryName(questionDTO.getCategoryType()));
            questionDTO.setCategoryColor(CategoryCache.getCategoryColor(questionDTO.getCategoryType()));
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        pagingDTO.setData(questionDTOList);
        return pagingDTO;
    }

    /**
     * 通过userId计算出该用户收藏了多少个问题
     *
     * @param userId
     * @return
     */
    public Integer countByUserId(Long userId) {
        return collectionMapper.countByUserId(userId);
    }
}
