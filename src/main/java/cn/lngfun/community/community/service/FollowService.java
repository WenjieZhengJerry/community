package cn.lngfun.community.community.service;

import cn.lngfun.community.community.dto.FollowDTO;
import cn.lngfun.community.community.dto.ResultDTO;
import cn.lngfun.community.community.exception.CustomizeErrorCode;
import cn.lngfun.community.community.mapper.FollowMapper;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.mapper.UserMapper;
import cn.lngfun.community.community.model.Follow;
import cn.lngfun.community.community.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FollowService {
    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 根据当前用户id列出他的关注列表
     *
     * @param followerId
     * @return
     */
    public List<FollowDTO> list(Long followerId) {
        List<FollowDTO> followDTOS = new ArrayList<>();
        List<Follow> follows = followMapper.findById(followerId);

        if (follows.size() == 0) {
            return new ArrayList<>();
        }

        for (Follow follow : follows) {
            FollowDTO followDTO = new FollowDTO();
            User user = userMapper.findById(follow.getUserId());
            Integer questionCount = questionMapper.countByUserId(follow.getUserId());
            Integer followerCount = followMapper.countFollowerById(follow.getUserId());
            user.setPassword(null);
            followDTO.setUser(user);
            followDTO.setQuestionCount(questionCount);
            followDTO.setFollowerCount(followerCount);
            followDTOS.add(followDTO);
        }

        return followDTOS;
    }

    /**
     * 关注
     *
     * @param userId
     * @param followerId
     * @return
     */
    public Object follow(Long userId, Long followerId) {
        if (userMapper.findById(userId) == null) {
            //查无此人
            return ResultDTO.errorOf(CustomizeErrorCode.USER_NOT_FOUND);
        }
        if (followMapper.isFollowed(userId, followerId) != null) {
            //避免重复关注
            return ResultDTO.errorOf(CustomizeErrorCode.FOLLOW_IS_EXIST);
        }
        //装填数据，关注
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowerId(followerId);
        follow.setGmtCreate(System.currentTimeMillis());
        followMapper.insert(follow);
        return ResultDTO.okOf();
    }

    /**
     * 取消关注
     *
     * @param userId
     * @param followerId
     * @return
     */
    public Object unfollow(Long userId, Long followerId) {
        if (userMapper.findById(userId) == null) {
            //查无此人
            return ResultDTO.errorOf(CustomizeErrorCode.USER_NOT_FOUND);
        }
        //取消关注
        followMapper.delete(userId, followerId);
        return ResultDTO.okOf();
    }

    /**
     * 判断是否关注
     *
     * @param userId
     * @param followerId
     * @return
     */
    public Follow isFollowed(Long userId, Long followerId) {
        return followMapper.isFollowed(userId, followerId);
    }
}
