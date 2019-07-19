package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.PagingDTO;
import cn.lngfun.community.community.dto.QuestionDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.exception.CustomizeException;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.Question;
import cn.lngfun.community.community.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;


    public PagingDTO list(Integer page, Integer size) {
        PagingDTO pagingDTO = new PagingDTO();
        Integer totalCount = questionMapper.count();
        Integer totalPage;
        //计算totalPage
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        //边界控制
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }
        pagingDTO.setPaging(totalPage, page);//装填页面元素

        Integer offset = size * (page - 1);
        List<Question> questions = questionMapper.list(offset, size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        pagingDTO.setQuestions(questionDTOList);//装填页面数据

        return pagingDTO;
    }

    public PagingDTO list(Long userId, Integer page, Integer size) {
        PagingDTO pagingDTO = new PagingDTO();
        Integer totalCount = questionMapper.countByUserId(userId);
        Integer totalPage;
        //计算totalPage
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        //边界控制
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }
        pagingDTO.setPaging(totalPage, page);//装填页面元素

        Integer offset = size * (page - 1);
        List<Question> questions = questionMapper.listByUserId(userId, offset, size);
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Question question : questions) {
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        pagingDTO.setQuestions(questionDTOList);//装填页面数据

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

    public void incView(Long id) {
        questionMapper.incView(id);
    }
}
