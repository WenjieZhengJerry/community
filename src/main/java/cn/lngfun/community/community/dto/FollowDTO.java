package cn.lngfun.community.community.dto;

import cn.lngfun.community.community.model.User;
import lombok.Data;

@Data
public class FollowDTO {
    private Integer questionCount;
    private Integer followerCount;
    private User user;
}
