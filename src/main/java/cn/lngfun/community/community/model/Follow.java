package cn.lngfun.community.community.model;

import lombok.Data;

@Data
public class Follow {
    private Long id;
    private Long userId;
    private Long followerId;
    private Long gmtCreate;
}
