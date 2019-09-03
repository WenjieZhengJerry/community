package cn.lngfun.community.community.model;

import lombok.Data;

@Data
public class Like {
    private Long id;
    private Long parentId;
    private Long userId;
    private Long gmtCreate;
    private Integer type;
}
