package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.Question;
import cn.lngfun.community.community.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    private List<QuestionDTO> selectQuestions(PagingDTO<QuestionDTO> pagingDTO, Integer totalCount, Integer page, Integer size) {
        //计算从第几页开始
        Integer offset = pagingDTO.calculateOffset(totalCount, page, size);
        //获取这一页的所有问题
        List<Question> questions = questionMapper.list(offset, size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        return questionDTOList;
    }

    /**
     * 列出所有问题列表
     * @param page
     * @param size
     * @return
     */
    public PagingDTO list(Integer page, Integer size) {
        PagingDTO<QuestionDTO> pagingDTO = new PagingDTO<>();
        Integer totalCount = questionMapper.count();

        pagingDTO.setData(selectQuestions(pagingDTO, totalCount, page, size));//装填页面数据

        return pagingDTO;
    }

    /**
     * 通过userId列出问题列表
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public PagingDTO list(Long userId, Integer page, Integer size) {
        PagingDTO<QuestionDTO> pagingDTO = new PagingDTO<>();
        Integer totalCount = questionMapper.countByUserId(userId);

        pagingDTO.setData(selectQuestions(pagingDTO, totalCount, page, size));//装填页面数据

        return pagingDTO;
    }

    public QuestionDTO findById(Long id) {
        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        User user = userMapper.findById(question.getCreator());
        questionDTO.setUser(user);

        return questionDTO;
    }

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

    public void incView(Long id) {
        questionMapper.incView(id);
    }

    public List<Question> selectHotIssue() {
        return questionMapper.selectHotIssue();
    }
}
